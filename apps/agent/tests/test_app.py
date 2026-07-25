from fastapi.testclient import TestClient

from purple_nexus_agent.main import app


def test_get_service_status() -> None:
    """验证 API 在没有模型或外部基础设施的情况下响应"""
    with TestClient(app) as client:
        response = client.get("/")

    assert response.status_code == 200
    assert response.json() == {"service": "purple-nexus-agent", "status": "ok"}
