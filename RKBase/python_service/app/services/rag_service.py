"""RAG 核心服务：封装完整生命周期（初始化组件 → 多用户索引 → 问答）。

多用户模式：每个用户(user_id)拥有独立的数据目录、独立 ChromaDB 集合、
独立的索引元数据，互不干扰。索引在用户首次访问时懒加载构建。

命令行版和 FastAPI 服务都复用本类。
"""
# 必须先导入 index_service（它会先导入 config）：config 会加载 .env 并设置
# HF_HUB_OFFLINE（离线模式），必须在导入 llama_index 之前生效。
from . import index_service

import os

from llama_index.core import PromptTemplate, Settings, VectorStoreIndex
from llama_index.embeddings.huggingface import HuggingFaceEmbedding
from llama_index.llms.dashscope import DashScope
from llama_index.vector_stores.chroma import ChromaVectorStore
import chromadb

# 回答提示词：严格按用户提供的"回答规范"生成
QA_PROMPT_TMPL = """# 回答规范
1. 仅使用【参考资料】中的内容回答，禁止编造任何信息
2. 若参考资料中没有相关内容，直接输出"未查询到对应信息，请调整关键词重试"，无需额外解释
3. 优先引用相似度最高的片段作为核心依据
4. 核心答案放在最开头，补充细节后置
5. 所有关键数据、结论需标注来源，格式为（来源：[文档名称]）
6. 若参考资料不足以完整回答，请明确指出信息不完整
7. 输出语言简洁清晰，禁止使用模糊表述（如"可能""大概"）

# 参考资料
{context_str}

# 用户问题
{query_str}

# 回答
"""


class RAGService:
    """RAG 服务：负责加载模型、按用户构建索引、回答问题。"""

    def __init__(self, settings):
        self.settings = settings
        self.chroma_client = None
        # 用户库缓存：user_id -> {"data_dir", "index_meta_file", "collection",
        # "vector_store", "index"}
        self.libraries = {}

    def init_components(self):
        """初始化 embedding 模型、LLM、向量数据库客户端（全局只做一次）。"""
        s = self.settings
        print(f"开始初始化RAG系统组件. . .")

        # 配置本地 embedding 模型
        print(f"本地embedding模型:{s.embed_model_name} loading")
        embed_model = HuggingFaceEmbedding(
            model_name=s.embed_model_name,
            trust_remote_code=True
        )
        Settings.embed_model = embed_model

        # 配置 LLM
        print(f"配置LLM模型:{s.llm_model_name} loading")
        Settings.llm = DashScope(
            model_name=s.llm_model_name,
            api_key=s.dashscope_api_key,
            temperature=s.llm_temperature
        )

        # 初始化 ChromaDB 客户端（集合按用户懒加载创建）
        print(f"连接ChromaDB向量数据库:{s.chroma_path} ")
        self.chroma_client = chromadb.PersistentClient(path=s.chroma_path)

        print(f"所有组件初始化完成")

    def get_library(self, user_id: int) -> dict:
        """获取（必要时创建）指定用户的库。返回缓存的库字典。"""
        if user_id in self.libraries:
            return self.libraries[user_id]

        s = self.settings
        data_dir = s.user_data_dir(user_id)
        os.makedirs(data_dir, exist_ok=True)

        collection = self.chroma_client.get_or_create_collection(
            name=s.user_collection_name(user_id),
            metadata={"hnsw:space": "cosine"},
        )
        vector_store = ChromaVectorStore(chroma_collection=collection)

        lib = {
            "user_id": user_id,
            "data_dir": data_dir,
            "index_meta_file": s.resolve_index_meta_file(user_id),
            "collection": collection,
            "vector_store": vector_store,
            "index": None,
        }
        self.libraries[user_id] = lib
        return lib

    def build_index(self, user_id: int):
        """构建或增量更新指定用户的索引。"""
        lib = self.get_library(user_id)
        lib["index"] = index_service.build_index(
            lib["vector_store"],
            lib["collection"],
            lib["data_dir"],
            lib["index_meta_file"],
        )
        return lib

    def count_chunks(self, user_id: int) -> int:
        """返回指定用户当前索引的片段总数。"""
        return self.get_library(user_id)["collection"].count()

    def query(self, question: str, user_id: int):
        """查询指定用户的知识库：检索 → 过滤低分/去重 → 按规范模板调用 LLM。

        返回 (回答文本, 保留的节点列表)。
        """
        s = self.settings
        lib = self.get_library(user_id)

        # 首次访问：懒加载构建索引；空库时 index 仍为 None
        if lib["index"] is None:
            lib = self.build_index(user_id)
        index = lib["index"]
        if index is None:
            return "未查询到对应信息，请调整关键词重试", []

        # 1. 检索资料片段
        retriever = index.as_retriever(similarity_top_k=s.top_k)
        raw_nodes = retriever.retrieve(question)

        # 2. 过滤低分片段（min_score 启用时），并按来源文件去重保留最高分
        best_by_file = {}
        for node in raw_nodes:
            if node.score < s.min_score:
                continue
            file_name = node.node.metadata.get("file_name", "未知文件")
            if file_name not in best_by_file or node.score > best_by_file[file_name].score:
                best_by_file[file_name] = node
        nodes = sorted(best_by_file.values(), key=lambda n: n.score, reverse=True)

        # 3. 打印诊断信息
        print("\n" + "=" * 50)
        print(f"🔍 诊断信息（用户 {user_id}）：检索到的原始文本片段")
        print("=" * 50)
        if not nodes:
            print("未检索到任何相关片段！问题可能出在检索环节。")
        else:
            for i, node in enumerate(nodes):
                file_name = node.node.metadata.get("file_name", "未知文件")
                print(f"\n--- 片段 {i + 1} (相似度得分: {node.score:.4f}) 来源:{file_name} ---")
                print(node.text[:150] + "...")
        print("=" * 50)

        # 4. 没有相关资料 → 直接返回固定话术，不调用LLM（省token、符合规范）
        if not nodes:
            return "未查询到对应信息，请调整关键词重试", []

        # 5. 构造带来源的参考资料
        context_parts = []
        for i, node in enumerate(nodes, 1):
            file_name = node.node.metadata.get("file_name", "未知文件")
            context_parts.append(f"[片段{i}]（来源：[{file_name}]）\n{node.text}")
        context_str = "\n\n".join(context_parts)

        # 6. 按模板调用 LLM
        prompt = PromptTemplate(QA_PROMPT_TMPL).format(
            context_str=context_str,
            query_str=question,
        )
        response = Settings.llm.complete(prompt)
        return response.text, nodes

    def upload_document(self, user_id: int, file_name: str, content: bytes) -> int:
        """保存文档到用户目录并重建索引，返回片段总数。"""
        lib = self.get_library(user_id)
        os.makedirs(lib["data_dir"], exist_ok=True)
        file_path = os.path.join(lib["data_dir"], file_name)
        with open(file_path, "wb") as f:
            f.write(content)
        self.build_index(user_id)
        return lib["collection"].count()

    def delete_document(self, user_id: int, file_name: str) -> int:
        """删除用户目录下的文档并重建索引，返回片段总数。"""
        lib = self.get_library(user_id)
        file_path = os.path.join(lib["data_dir"], file_name)
        if not os.path.exists(file_path):
            raise FileNotFoundError(f"文档 {file_name} 不存在")
        os.remove(file_path)
        self.build_index(user_id)
        return lib["collection"].count()

    def list_documents(self, user_id: int) -> list:
        """列出用户目录下的文档信息。"""
        lib = self.get_library(user_id)
        return index_service.list_data_dir(lib["data_dir"])
