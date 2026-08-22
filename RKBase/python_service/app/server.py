"""FastAPI 服务入口。

启动方式（在 python_service 目录下）：
    uvicorn app.server:app --reload
然后浏览器访问 http://127.0.0.1:8000/docs 查看接口说明书。
"""
from contextlib import asynccontextmanager

from fastapi import FastAPI

from .config import settings
from .routers import health, users
from .services.rag_service import RAGService


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 服务启动时：只加载模型 + 连接向量库（一次性）。
    # 每个用户的索引在首次访问时懒加载构建，不在此处预建全局索引。
    rag = RAGService(settings)
    rag.init_components()
    app.state.rag_service = rag
    yield


app = FastAPI(
    title="AI成长型学习仓库 - RAG知识库服务",
    description="基于私有文档的精准问答与知识检索服务（多用户隔离版）",
    version="1.0.0",
    lifespan=lifespan,
)

app.include_router(health.router)
app.include_router(users.router)
