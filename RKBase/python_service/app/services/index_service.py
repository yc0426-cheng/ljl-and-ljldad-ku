"""索引构建/更新：扫描 data 目录、维护元数据、构建或增量更新向量索引。

从原命令行版 main.py 迁移而来，逻辑不变，只是把常量改为从 settings 读取。
"""
import json
import os

# 必须先导入 config：config 会加载 .env 并设置 HF_HUB_OFFLINE（离线模式），
# 该设置必须在导入 llama_index 之前生效，否则启动会卡在联网检查上。
from ..config import settings

from llama_index.core import (SimpleDirectoryReader, StorageContext, VectorStoreIndex)
from llama_index.core.node_parser import SentenceSplitter


def load_index_meta(index_meta_file):
    """加载索引元数据（记录每个文件的修改时间）。"""
    if os.path.exists(index_meta_file):
        with open(index_meta_file, "r", encoding="utf-8") as f:
            return json.load(f)
    return {}


def save_index_meta(meta, index_meta_file):
    """保存索引元数据。"""
    os.makedirs(os.path.dirname(index_meta_file), exist_ok=True)
    with open(index_meta_file, "w", encoding="utf-8") as f:
        json.dump(meta, f, ensure_ascii=False, indent=2)


def scan_data_dir(data_dir):
    """扫描 data 目录，返回 {文件名: 修改时间} 字典。"""
    file_info = {}
    if not os.path.exists(data_dir):
        return file_info
    for fname in os.listdir(data_dir):
        if fname.endswith(".md"):
            fpath = os.path.join(data_dir, fname)
            file_info[fname] = os.path.getmtime(fpath)
    return file_info


def list_data_dir(data_dir):
    """列出目录下的文档信息：[{file_name, size, mtime}]。"""
    items = []
    if not os.path.exists(data_dir):
        return items
    for fname in sorted(os.listdir(data_dir)):
        fpath = os.path.join(data_dir, fname)
        if os.path.isfile(fpath) and fname.endswith(".md"):
            items.append({
                "file_name": fname,
                "size": os.path.getsize(fpath),
                "mtime": os.path.getmtime(fpath),
            })
    return items


def build_index(vector_store, chroma_collection, data_dir=None, index_meta_file=None):
    """加载 data 目录下的 markdown 文档，构建或增量更新向量索引。

    data_dir / index_meta_file 缺省时回退到全局配置（兼容旧的单用户用法）。
    目录为空时清空该集合残留向量、保存空元数据并返回 None（新用户空库）。
    """
    storage_context = StorageContext.from_defaults(vector_store=vector_store)

    if data_dir is None:
        data_dir = settings.data_dir
    if index_meta_file is None:
        index_meta_file = settings.resolve_index_meta_file()

    # 扫描 data 目录中的当前文件
    current_files = scan_data_dir(data_dir)

    if not current_files:
        # 空目录：清空该集合可能残留的向量，保存空元数据，返回 None
        print(f"目录 {data_dir} 下暂无文档，清空残留索引...")
        try:
            results = chroma_collection.get()
            if results and results["ids"]:
                chroma_collection.delete(ids=results["ids"])
        except Exception as e:
            print(f"   ⚠️ 清空残留索引时出错: {e}")
        save_index_meta({}, index_meta_file)
        return None

    # 加载上次索引的元数据
    indexed_meta = load_index_meta(index_meta_file)

    # 判断哪些文件是新增或修改过的
    new_files = []
    updated_files = []
    for fname, mtime in current_files.items():
        if fname not in indexed_meta:
            new_files.append(fname)
        elif mtime > indexed_meta[fname].get("mtime", 0):
            updated_files.append(fname)

    # 判断哪些文件已被删除
    deleted_files = [fname for fname in indexed_meta if fname not in current_files]

    # 情况1：没有任何变化，直接加载已有索引
    if not new_files and not updated_files and not deleted_files and chroma_collection.count() > 0:
        print(f"检测到已有索引（{chroma_collection.count()}个片段），文档无变化，直接加载...")
        index = VectorStoreIndex.from_vector_store(
            vector_store=vector_store,
            storage_context=storage_context
        )
        return index

    # 情况2：首次构建（索引为空）
    if chroma_collection.count() == 0:
        print(f"未检测到已有索引，开始构建新索引...")
        print(f"正在读取 {data_dir} 目录下的markdown文档...")
        documents = SimpleDirectoryReader(
            input_dir=data_dir,
            required_exts=[".md"],
            filename_as_id=True
        ).load_data()
        print(f"   成功加载 {len(documents)} 个文档")

        text_splitter = SentenceSplitter(
            chunk_size=settings.chunk_size,
            chunk_overlap=settings.chunk_overlap
        )

        print("正在处理文档：切分→向量化→存入数据库...")
        index = VectorStoreIndex.from_documents(
            documents,
            storage_context=storage_context,
            transformations=[text_splitter],
            show_progress=True
        )

        # 保存索引元数据
        new_meta = {fname: {"mtime": mtime} for fname, mtime in current_files.items()}
        save_index_meta(new_meta, index_meta_file)

        print(f"索引构建完成，共生成 {chroma_collection.count()} 个文档片段\n")
        return index

    # 情况3：增量更新（有新增/修改/删除的文件）
    print("\n" + "=" * 50)
    print("📂 检测到文档变化，开始增量更新索引...")
    print("=" * 50)

    # 3a. 删除已移除文件的索引
    if deleted_files:
        print(f"\n🗑️  删除已移除文件的索引: {deleted_files}")
        for fname in deleted_files:
            # 删除该文件相关的所有向量（通过文件名匹配）
            try:
                results = chroma_collection.get(where={"file_name": fname})
                if results and results["ids"]:
                    chroma_collection.delete(ids=results["ids"])
                    print(f"   已删除 {fname} 的 {len(results['ids'])} 个片段")
            except Exception as e:
                print(f"   ⚠️ 删除 {fname} 时出错: {e}")
            # 从元数据中移除
            indexed_meta.pop(fname, None)

    # 3b. 处理新增和修改的文件
    files_to_process = new_files + updated_files
    if files_to_process:
        print(f"\n📄 需要处理的文件: {files_to_process}")

        text_splitter = SentenceSplitter(
            chunk_size=settings.chunk_size,
            chunk_overlap=settings.chunk_overlap
        )

        for fname in files_to_process:
            fpath = os.path.join(data_dir, fname)
            print(f"\n   处理: {fname} ...")

            # 先删除该文件的旧索引（针对修改的文件）
            if fname in updated_files:
                try:
                    results = chroma_collection.get(where={"file_name": fname})
                    if results and results["ids"]:
                        chroma_collection.delete(ids=results["ids"])
                        print(f"   已清除旧索引（{len(results['ids'])}个片段）")
                except Exception as e:
                    print(f"   ⚠️ 清除旧索引时出错: {e}")

            # 读取新文档
            docs = SimpleDirectoryReader(
                input_files=[fpath],
                filename_as_id=True
            ).load_data()

            # 切分+向量化+存入
            nodes = text_splitter.get_nodes_from_documents(docs)
            # 为每个节点设置文件名元数据，确保可溯源
            for node in nodes:
                node.metadata["file_name"] = fname

            index = VectorStoreIndex.from_vector_store(
                vector_store=vector_store,
                storage_context=storage_context
            )
            index.insert_nodes(nodes)
            print(f"   ✅ 已索引 {len(nodes)} 个片段")

            # 更新元数据
            indexed_meta[fname] = {"mtime": current_files[fname]}

    # 保存更新后的元数据
    save_index_meta(indexed_meta, index_meta_file)

    print(f"\n增量更新完成，当前索引共 {chroma_collection.count()} 个片段\n")

    # 重新加载完整索引
    index = VectorStoreIndex.from_vector_store(
        vector_store=vector_store,
        storage_context=storage_context
    )
    return index
