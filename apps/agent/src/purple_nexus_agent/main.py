from fastapi import FastAPI

app = FastAPI(title="Purple Nexus Agent")


@app.get("/")
async def get_service_status() -> dict[str, str]:
    """
    获取服务状态信息

    Returns:
        dict[str, str]: 包含服务状态的字典
    """
    return {"service": "purple-nexus-agent", "status": "ok"}
