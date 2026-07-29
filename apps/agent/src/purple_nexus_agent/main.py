from collections.abc import AsyncGenerator
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request, Response, status
from qdrant_client import AsyncQdrantClient
from qdrant_client.http.exceptions import ApiException as QdrantApiException
from redis.asyncio import Redis
from redis.exceptions import RedisError

from purple_nexus_agent.config import AgentSettings

# 健康检查属于高频、轻量请求，不能长时间占用事件循环。
# 当前个人开发环境统一给 Redis 和 Qdrant 3 秒网络超时，
# 暂时不增加额外环境变量或复杂的分依赖超时配置。
DEPENDENCY_TIMEOUT_SECONDS = 3


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None]:
    """
    创建并管理 Agent 配置和外部服务客户端。

    启动阶段：
        1. 读取并校验 AgentSettings。
        2. 创建可复用的异步 Redis 客户端。
        3. 创建可复用的异步 Qdrant 客户端。
        4. 将配置和客户端保存到 app.state。

    运行阶段：
        所有请求共享同一组客户端和连接池，不重复创建网络资源。

    失败：
        配置无效时 AgentSettings 会阻止应用启动。
        当前客户端构造过程不执行网络请求，因此依赖是否可用
        将由后续 readiness 检查判断。

    关闭阶段：
        finally 保证 FastAPI 正常关闭或运行阶段出现异常时，
        两个客户端都会收到关闭通知。
    """
    settings = AgentSettings()

    # Redis.from_url() 只根据 URL 创建客户端和连接池
    # socket_connect_timeout 限制建立连接的等待时间
    # socket_timeout 限制已连接后的命令响应等待时间
    # decode_responses 响应自动从 bytes 解码为 str
    redis_client = Redis.from_url(
        str(settings.redis_url),
        encoding="utf-8",
        decode_responses=True,
        socket_connect_timeout=DEPENDENCY_TIMEOUT_SECONDS,
        socket_timeout=DEPENDENCY_TIMEOUT_SECONDS,
    )

    # AsyncQdrantClient 当前只完成客户端配置，不会查询 Collection
    # SecretStr 必须显式取出原文，降低密钥被意外记录的风险
    qdrant_client = AsyncQdrantClient(
        url=str(settings.qdrant_url),
        api_key=settings.qdrant_api_key.get_secret_value(),
        timeout=DEPENDENCY_TIMEOUT_SECONDS,
    )

    app.state.settings = settings
    app.state.redis_client = redis_client
    app.state.qdrant_client = qdrant_client

    try:
        # yield 之前是启动阶段, 执行到这里之后开始接收请求
        yield
    finally:
        # 客户端按照统一生命周期关闭, 避免连接池残留
        await redis_client.aclose()
        await qdrant_client.close()


app = FastAPI(title="Purple Nexus Agent", lifespan=lifespan)


@app.get("/health/live")
async def get_liveness() -> dict[str, str]:
    """
    返回 Agent 进程的存活状态。

    该端点只证明 FastAPI 事件循环和 HTTP 路由仍能响应。
    它故意不读取 Redis、Qdrant 或其他外部服务，
    避免依赖短暂故障被误判为 Agent 进程死亡。

    Returns:
        dict[str, str]: 固定返回 UP 的最小存活状态。
    """
    return {"status": "UP"}


@app.get("/health/ready")
async def get_readiness(request: Request, response: Response) -> dict[str, str]:
    """
    检查 Agent 处理当前业务请求所需的外部依赖。

    输入：
        request 用于取得 lifespan 保存到 app.state 的共享客户端。
        response 用于在依赖失败时把 HTTP 状态设置为 503。

    处理：
        1. 使用 Redis PING 验证连接和认证。
        2. 读取 Qdrant Collection 列表，验证连接、认证和响应解析。

    输出：
        两项检查都成功时返回 HTTP 200 和 {"status": "UP"}。

    失败：
        Redis 或 Qdrant 出现已知客户端异常时，
        返回 HTTP 503 和 {"status": "DOWN"}。
        响应不包含地址、密钥、异常消息或堆栈。
    """
    redis_client: Redis = request.app.state.redis_client
    qdrant_client: AsyncQdrantClient = request.app.state.qdrant_client

    try:
        # PING 是 Redis 最轻量的只读连接检查，不会写入任何 Key
        await redis_client.ping()

        # 只读取 Collection 列表，不创建或修改向量数据
        await qdrant_client.get_collections()
    except (RedisError, QdrantApiException):
        response.status_code = status.HTTP_503_SERVICE_UNAVAILABLE
        return {"status": "DOWN"}

    return {"status": "UP"}


@app.get("/")
async def get_service_status() -> dict[str, str]:
    """
    返回 Agent 服务的最小脚手架状态。

    当前根路径只用于保留 D1-03 的原有行为。
    标准健康状态请使用 /health/live 和 /health/ready。
    """
    return {
        "service": "purple-nexus-agent",
        "status": "ok",
    }
