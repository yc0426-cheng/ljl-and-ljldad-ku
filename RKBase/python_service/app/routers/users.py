"""用户级 RAG 接口：问答 / 文档管理，均需 API Key 鉴权。

所有接口都按 user_id 隔离：每个用户拥有独立的数据目录与向量集合，
互不干扰。
"""
import os

from fastapi import APIRouter, Depends, HTTPException, Request, UploadFile

from ..models.schemas import (
    DocumentListResponse,
    DocumentListItem,
    DocumentResponse,
    QueryRequest,
    QueryResponse,
    ReloadResponse,
    SourceItem,
)
from .auth import verify_api_key

router = APIRouter(dependencies=[Depends(verify_api_key)])


def _check_user_id(user_id: int) -> int:
    """校验用户ID合法性（必须是正整数）。"""
    if user_id <= 0:
        raise HTTPException(status_code=400, detail="user_id 不合法")
    return user_id


def _safe_file_name(file_name: str) -> str:
    """校验文件名安全：仅保留文件名部分，且只允许 .md 后缀。"""
    base = os.path.basename(file_name)
    if base != file_name:
        raise HTTPException(status_code=400, detail="文件名不合法（不允许包含路径）")
    if not base.lower().endswith(".md"):
        raise HTTPException(status_code=400, detail="仅支持 .md 格式的文档")
    if not base:
        raise HTTPException(status_code=400, detail="文件名不能为空")
    return base


@router.post("/api/v1/users/{user_id}/query", response_model=QueryResponse)
def query(user_id: int, req: QueryRequest, request: Request):
    """提问：{question} → {answer, sources}"""
    _check_user_id(user_id)
    rag = request.app.state.rag_service
    answer, nodes = rag.query(req.question, user_id)
    sources = [
        SourceItem(
            file_name=node.node.metadata.get("file_name", "未知文件"),
            text=node.text,
            score=float(node.score),
        )
        for node in nodes
    ]
    return QueryResponse(answer=answer, sources=sources)


@router.post("/api/v1/users/{user_id}/documents", response_model=DocumentResponse)
async def upload_document(user_id: int, file: UploadFile, request: Request):
    """上传 .md 文档到指定用户的知识库并重建索引。"""
    _check_user_id(user_id)
    file_name = _safe_file_name(file.filename)
    content = await file.read()
    rag = request.app.state.rag_service
    total_chunks = rag.upload_document(user_id, file_name, content)
    return DocumentResponse(
        file_name=file_name,
        message=f"文档 {file_name} 已上传并索引",
        total_chunks=total_chunks,
    )


@router.delete("/api/v1/users/{user_id}/documents/{file_name}", response_model=DocumentResponse)
def delete_document(user_id: int, file_name: str, request: Request):
    """删除指定用户知识库中的文档并重建索引。"""
    _check_user_id(user_id)
    safe_name = _safe_file_name(file_name)
    rag = request.app.state.rag_service
    try:
        total_chunks = rag.delete_document(user_id, safe_name)
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail=f"文档 {safe_name} 不存在")
    return DocumentResponse(
        file_name=safe_name,
        message=f"文档 {safe_name} 已删除并更新索引",
        total_chunks=total_chunks,
    )


@router.get("/api/v1/users/{user_id}/documents", response_model=DocumentListResponse)
def list_documents(user_id: int, request: Request):
    """列出指定用户知识库中的文档。"""
    _check_user_id(user_id)
    rag = request.app.state.rag_service
    items = rag.list_documents(user_id)
    return DocumentListResponse(items=[DocumentListItem(**item) for item in items])


@router.post("/api/v1/users/{user_id}/reload", response_model=ReloadResponse)
def reload_documents(user_id: int, request: Request):
    """重新扫描指定用户的文档目录并更新索引。"""
    _check_user_id(user_id)
    rag = request.app.state.rag_service
    rag.build_index(user_id)
    return ReloadResponse(
        message="索引已重新扫描并更新",
        total_chunks=rag.count_chunks(user_id),
    )
