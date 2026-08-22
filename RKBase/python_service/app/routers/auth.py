"""简单 API 密钥鉴权：请求需带 X-API-Key 请求头。

密钥在 .env 的 API_KEY 配置。若 API_KEY 为空，则跳过鉴权（方便本地调试）。
"""
from fastapi import Header, HTTPException

from ..config import settings


def verify_api_key(x_api_key: str = Header(default="")):
    """校验请求头的 API Key，不通过则返回 401。"""
    if settings.api_key and x_api_key != settings.api_key:
        raise HTTPException(status_code=401, detail="无效的 API Key")
    return True
