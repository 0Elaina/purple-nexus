from pydantic import AnyHttpUrl, Field, RedisDsn, SecretStr
from pydantic_settings import BaseSettings, SettingsConfigDict


class AgentSettings(BaseSettings):
    """
    集中读取并校验 Purple Nexus Agent 的运行配置。

    配置可以来自系统环境变量或当前工作目录下的 .env 文件。
    创建 AgentSettings 对象时, Pydantic 会立即完成类型转换：

    - Redis 地址必须符合 redis:// 或 rediss:// 格式。
    - Qdrant 地址必须是 HTTP 或 HTTPS URL。
    - 密钥和服务 Token 不能为空，并使用 SecretStr 避免意外打印原文。

    任意必需配置缺失或格式错误时，对象创建失败。
    这样可以让配置问题在应用启动阶段暴露，而不是等到第一次请求时才失败。
    """

    model_config = SettingsConfigDict(
        # 从 apps/agent 目录启动时, 读取本机的 .env
        env_file=".env",
        env_file_encoding="utf-8",
        # 环境变量名称不区分大小写, 但是项目统一使用大写名称
        case_sensitive=False,
        # 忽略不属于当前 AgentSettings 的额外变量
        # 避免以后向 .env 增加其他配置时破坏旧代码
        extra="ignore",
    )

    # 当前运行环境: 例如 local、test 或 production
    app_env: str = Field(min_length=1)

    # RedisDsn 会验证协议、主机、端口、密码和数据库编号的基本格式
    redis_url: RedisDsn

    # 使用 AnyHttpUrl 是因为 Qdrant 可能使用 localhost 或 内网 IP
    qdrant_url: AnyHttpUrl

    # SecretStr 在日志或 repr() 中显示为 ******
    # 后续传给 Qdrant 客户端时再显式读取真实值
    qdrant_api_key: SecretStr = Field(min_length=1)

    # Spring Boot 调用 Agent 时携带的内部服务凭据
    agent_service_token: SecretStr = Field(min_length=1)
