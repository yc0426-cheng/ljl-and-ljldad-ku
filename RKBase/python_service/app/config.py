"""全局配置：从 .env 读取，代码与配置分离。

所有配置项都可以在 python_service/.env 中覆盖，改参数不用动代码。
"""
import os
import sys
from pathlib import Path

from dotenv import load_dotenv
from pydantic_settings import BaseSettings, SettingsConfigDict

# Windows 中文系统默认输出编码是 GBK，无法打印 emoji（如🔍），
# 在日志/管道环境下会直接崩溃。强制 stdout 用 UTF-8。
if sys.stdout and hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
if sys.stderr and hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

# 项目根目录 = python_service/（config.py 在 app/ 子目录下，向上两级）
PROJECT_ROOT = Path(__file__).resolve().parent.parent

# 先加载 .env，再设置离线模式。
# 关键：HF_HUB_OFFLINE 必须在导入 llama_index 之前设置，否则启动时会联网检查
# huggingface.co 而卡住（模型已缓存在本地，无需联网）。
ENV_FILE = PROJECT_ROOT / ".env"
load_dotenv(ENV_FILE)
os.environ.setdefault("HF_HUB_OFFLINE", "1")


class Settings(BaseSettings):
    """RAG 知识库配置，全部可用 .env 覆盖。"""

    # --- API 密钥 ---
    dashscope_api_key: str = ""   # 通义千问密钥（必填）
    api_key: str = ""             # 服务访问密钥（简单鉴权），留空则所有接口不鉴权

    # --- 路径 ---
    # 默认指向 app/data 和 app/chroma_db（在项目根目录下解析成绝对路径，
    # 这样无论从哪个目录启动服务都能找到）
    data_dir: str = str(PROJECT_ROOT / "app" / "data")         # 知识文档目录
    chroma_path: str = str(PROJECT_ROOT / "app" / "chroma_db") # 向量数据库路径
    collection_name: str = "my_md_knowledge"  # ChromaDB 集合名
    index_meta_file: str = ""          # 索引元数据文件，留空则用 chroma_path 下的默认名

    # --- 模型 ---
    embed_model_name: str = "BAAI/bge-small-zh-v1.5"  # 本地嵌入模型
    llm_model_name: str = "qwen-plus"                 # 大语言模型
    llm_temperature: float = 0.3                      # 回答随机性（越低越严谨）

    # --- 切分参数（后期优化点）---
    chunk_size: int = 512
    chunk_overlap: int = 50

    # --- 检索参数（后期优化点）---
    top_k: int = 5
    # 相似度阈值：低于该值的片段不采纳。0 表示不启用（默认）。
    min_score: float = 0.0
    # 命令行模式默认使用的用户ID（多用户下 CLI 归属的账号）
    default_user_id: int = 1

    model_config = SettingsConfigDict(
        env_file=".env", env_file_encoding="utf-8", extra="ignore"
    )

    def resolve_index_meta_file(self, user_id: int = None) -> str:
        """返回索引元数据文件的完整路径。

        指定 user_id 时按用户隔离（index_meta_<user_id>.json），
        否则使用默认文件（兼容旧的全局用法）。
        """
        if self.index_meta_file:
            return self.index_meta_file
        if user_id is not None:
            return os.path.join(self.chroma_path, f"index_meta_{user_id}.json")
        return os.path.join(self.chroma_path, "index_meta.json")

    def user_data_dir(self, user_id: int) -> str:
        """返回指定用户的文档数据目录：<data_dir>/<user_id>/"""
        return os.path.join(self.data_dir, str(user_id))

    def user_collection_name(self, user_id: int) -> str:
        """返回指定用户的 ChromaDB 集合名：<collection_name>_<user_id>"""
        return f"{self.collection_name}_{user_id}"


settings = Settings()
