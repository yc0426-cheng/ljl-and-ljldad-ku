"""命令行交互版入口（本地调试用）。

启动方式（在 python_service 目录下）：
    python -m app.main
"""
from .config import settings
from .services.rag_service import RAGService


def main():
    # 初始化所有组件
    rag = RAGService(settings)
    rag.init_components()
    # 默认用户ID（多用户下 CLI 归属的账号，见 .env 的 DEFAULT_USER_ID）
    uid = settings.default_user_id
    # 预热默认用户的知识库索引（支持增量更新）
    rag.build_index(uid)

    # 交互式查询循环
    print("\n" + "=" * 50)
    print(f"当前用户: {uid}（可在 .env 的 DEFAULT_USER_ID 修改）")
    print("请尽情吩咐RAG知识库！主任~~ 输入 'exit' 或 'quit' 退出")
    print("💡 输入 'reload' 可重新扫描文档目录，更新索引")
    print("=" * 50 + "\n")

    while True:
        question = input("请输入你的问题: ").strip()
        if question.lower() in ['exit', 'quit', 'q']:
            print("用完就扔? 人渣!")
            break
        if not question:
            continue

        # 支持手动触发重新索引
        if question.lower() == 'reload':
            print("🔄 重新扫描文档目录...")
            rag.build_index(uid)
            print("✅ 索引更新完成，可以继续提问\n")
            continue

        try:
            answer, nodes = rag.query(question, uid)
            print("\n" + "=" * 50)
            print(f"💬 回答: {answer}")
            print("=" * 50)
            print("📄 参考来源:")
            for i, node in enumerate(nodes, 1):
                file_name = node.node.metadata.get("file_name", "未知文件")
                text_preview = node.node.text[:80] + "..." if len(node.node.text) > 80 else node.node.text
                print(f"  {i}. 【{file_name}】(得分:{node.score:.4f}) {text_preview}")
        except Exception as e:
            print(f"❌ 查询出错: {e}\n")


if __name__ == "__main__":
    main()
