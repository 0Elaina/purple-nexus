from unittest.mock import AsyncMock

import pytest
from fastapi.testclient import TestClient
from qdrant_client import AsyncQdrantClient
from redis.asyncio import Redis
from redis.exceptions import RedisError

from purple_nexus_agent.main import app


def set_test_environment(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    """
    为 FastAPI lifespan 准备测试专用配置。

    所有值只在当前测试进程内生效，不读取开发机的真实 .env，
    也不会连接对应地址。
    """
    monkeypatch.setenv("APP_ENV", "test")
    monkeypatch.setenv(
        "REDIS_URL",
        "redis://:test-password@localhost:6379/0",
    )

    # 测试不连接 Qdrant，使用 HTTPS 假地址避免 API Key 经 HTTP
    # 传输的安全警告干扰客户端生命周期测试。
    monkeypatch.setenv(
        "QDRANT_URL",
        "https://localhost:6333",
    )
    monkeypatch.setenv(
        "QDRANT_API_KEY",
        "test-qdrant-api-key",
    )
    monkeypatch.setenv(
        "AGENT_SERVICE_TOKEN",
        "test-agent-service-token",
    )


def test_get_service_status(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    """
    验证配置正确时，FastAPI 可以完成启动并响应根路径。

    测试只提供格式正确的假配置，不连接 Redis 或 Qdrant。
    """
    # Given：在 lifespan 启动前准备测试配置
    set_test_environment(monkeypatch)

    # When：进入上下文时执行 lifespan，配置校验成功后发送请求。
    with TestClient(app) as client:
        service_response = client.get("/")
        liveness_response = client.get("/health/live")

        # lifespan 应该已经创建并保存两个异步客户端。
        assert isinstance(app.state.redis_client, Redis)
        assert isinstance(
            app.state.qdrant_client,
            AsyncQdrantClient,
        )

    # Then：原有脚手架接口行为保持不变。
    assert service_response.status_code == 200
    assert service_response.json() == {
        "service": "purple-nexus-agent",
        "status": "ok",
    }

    # liveness: 不访问 Redis 或 Qdrant, 只验证 Agent 进程可以响应
    assert liveness_response.status_code == 200
    assert liveness_response.json() == {"status": "UP"}


def test_readiness_reports_up_when_dependencies_are_available(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    """
    验证 Redis 和 Qdrant 都能响应时，readiness 返回 UP。

    两个 AsyncMock 只模拟客户端返回结果，不发送真实网络请求。
    """
    # Given：准备可启动的配置和两个成功的异步依赖。
    set_test_environment(monkeypatch)

    redis_client = AsyncMock()
    redis_client.ping.return_value = True

    qdrant_client = AsyncMock()
    qdrant_client.get_collections.return_value = None

    with TestClient(app) as client:
        # lifespan 先创建真实客户端对象，但没有执行网络请求。
        # 测试在发送 readiness 请求前用可控制的假客户端替换它们。
        app.state.redis_client = redis_client
        app.state.qdrant_client = qdrant_client

        # When：请求 readiness。
        response = client.get("/health/ready")

    # Then：两个依赖都成功，因此返回 HTTP 200 和 UP。
    assert response.status_code == 200
    assert response.json() == {
        "status": "UP",
    }

    # 确认 readiness 确实执行了两个检查，不是固定返回成功。
    redis_client.ping.assert_awaited_once_with()
    qdrant_client.get_collections.assert_awaited_once_with()


def test_readiness_reports_down_but_liveness_stays_up(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    """
    验证 Redis 失败时 readiness 为 DOWN，但 liveness 仍为 UP。

    这里只选择 Redis 作为一个代表性依赖故障，
    不穷举连接拒绝、认证失败和超时等 SDK 已定义的全部情况。
    """
    # Given：让 Redis PING 模拟一个客户端连接错误。
    set_test_environment(monkeypatch)

    redis_client = AsyncMock()
    redis_client.ping.side_effect = RedisError("test Redis connection failure")

    qdrant_client = AsyncMock()

    with TestClient(app) as client:
        app.state.redis_client = redis_client
        app.state.qdrant_client = qdrant_client

        # When：分别请求 readiness 和 liveness。
        readiness_response = client.get("/health/ready")
        liveness_response = client.get("/health/live")

    # Then：依赖不可用，因此 readiness 返回 HTTP 503。
    assert readiness_response.status_code == 503
    assert readiness_response.json() == {
        "status": "DOWN",
    }

    # Agent 进程仍然可以响应，所以 liveness 不受影响。
    assert liveness_response.status_code == 200
    assert liveness_response.json() == {
        "status": "UP",
    }

    redis_client.ping.assert_awaited_once_with()

    # 当前实现按 Redis → Qdrant 顺序检查。
    # Redis 已失败后，无需继续发送 Qdrant 请求。
    qdrant_client.get_collections.assert_not_awaited()
