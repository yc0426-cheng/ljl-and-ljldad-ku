"""健康检查接口：供 Spring Boot 主后端探活，不鉴权。"""
from fastapi import APIRouter

router = APIRouter()


@router.get("/health")
def health():
    return {"status": "ok"}
