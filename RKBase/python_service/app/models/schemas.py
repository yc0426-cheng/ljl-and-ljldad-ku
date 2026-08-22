"""接口的请求/响应数据格式（FastAPI 的"合同"）。

定义清楚后，Spring Boot 主后端就知道怎么和本服务对接。
"""
from pydantic import BaseModel


class QueryRequest(BaseModel):
    """问答接口的请求体。"""
    question: str


class SourceItem(BaseModel):
    """一条参考来源。"""
    file_name: str
    text: str
    score: float = 0.0


class QueryResponse(BaseModel):
    """问答接口的响应体。"""
    answer: str
    sources: list[SourceItem]


class DocumentResponse(BaseModel):
    """上传/删除文档接口的响应体。"""
    file_name: str
    message: str
    total_chunks: int


class DocumentListItem(BaseModel):
    """文档列表中的一条。"""
    file_name: str
    size: int
    mtime: float


class DocumentListResponse(BaseModel):
    """文档列表接口的响应体。"""
    items: list[DocumentListItem]


class ReloadResponse(BaseModel):
    """重索引接口的响应体。"""
    message: str
    total_chunks: int
