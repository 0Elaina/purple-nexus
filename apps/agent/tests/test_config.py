import pytest
from pydantic import ValidationError

from purple_nexus_agent.config import AgentSettings


def test_settings_accept_valid_input() -> None:
    """
    验证完整且格式正确的输入可以创建配置对象。

    测试通过构造函数直接传入假数据，不读取开发机的真实 .env，
    也不会连接 Redis 或 Qdrant。
    """
    # Given：准备格式正确的测试配置。
    settings = AgentSettings(
        _env_file=None,
        app_env="test",
        redis_url="redis://:test-password@localhost:6379/0",
        qdrant_url="http://localhost:6333",
        qdrant_api_key="test-qdrant-api-key",
        agent_service_token="test-agent-service-token",
    )

    # Then：确认普通字符串已经被转换成带类型的配置值。
    assert settings.app_env == "test"
    assert settings.redis_url.scheme == "redis"
    assert settings.redis_url.host == "localhost"
    assert settings.redis_url.port == 6379
    assert settings.qdrant_url.scheme == "http"
    assert settings.qdrant_url.host == "localhost"

    # SecretStr 默认隐藏原文，只有显式调用 get_secret_value()
    # 才能获得稍后需要传给客户端的真实值。
    assert settings.qdrant_api_key.get_secret_value() == "test-qdrant-api-key"
    assert settings.agent_service_token.get_secret_value() == "test-agent-service-token"


def test_settings_reject_missing_required_input(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    """
    验证所有必需配置都不存在时，配置对象会立即创建失败。

    monkeypatch 只在当前测试期间删除环境变量；
    测试结束后，pytest 会自动恢复原来的进程环境。
    """
    required_variables = (
        "APP_ENV",
        "REDIS_URL",
        "QDRANT_URL",
        "QDRANT_API_KEY",
        "AGENT_SERVICE_TOKEN",
    )

    # Given：确保测试不会意外读取操作系统中已有的同名变量。
    for variable_name in required_variables:
        monkeypatch.delenv(variable_name, raising=False)

    # When：禁用 .env 后创建配置对象。
    # Then：缺失必需配置时必须抛出 Pydantic ValidationError。
    with pytest.raises(ValidationError) as error_info:
        AgentSettings(_env_file=None)

    # 错误中应该明确指出五个缺失字段，而不是只报告模糊的启动失败。
    missing_fields = {
        str(error["loc"][0])
        for error in error_info.value.errors()
        if error["type"] == "missing"
    }

    assert missing_fields == {
        "app_env",
        "redis_url",
        "qdrant_url",
        "qdrant_api_key",
        "agent_service_token",
    }
