# RAG 知识库服务

基于 **Python / FastAPI** 的 RAG（检索增强生成）知识库服务，为「AI 成长型学习仓库」主项目提供私有文档的精准问答与知识检索能力。

- 知识文档放入 `data/<user_id>/` 目录（当前仅支持 `.md`），**每个用户独立**
- 使用 **llama-index** 做文档切分、向量化与检索
- 使用 **ChromaDB** 做向量存储（每个用户一个独立集合）
- 使用本地嵌入模型 `BAAI/bge-small-zh-v1.5` + 通义千问 `qwen-plus` 生成回答

> **⚠️ 重要**：`data/` 目录存放的是**私人知识文档**，已被 `.gitignore` 忽略，**不会上传到 GitHub**。在部署环境需要自行放入知识文档。

---

## 两种使用方式

### 1. 命令行问答（本地调试用）

```bash
cd python_service
python -m app.main
```

输入问题开始问答；输入 `reload` 重新扫描文档目录更新索引；输入 `exit` 退出。

### 2. HTTP 接口服务（对接 Spring Boot 主后端用）

```bash
cd python_service
uvicorn app.server:app --reload
```

启动后浏览器访问 **http://127.0.0.1:8000/docs** 查看接口说明书（Swagger），也可以直接调试接口。

---

## 环境准备

```bash
cd python_service
python -m venv .venv                  # 首次创建虚拟环境
.venv/Scripts/pip install -r requirements.txt
```

复制配置模板并填入真实密钥：

```bash
cp .env.example .env
# 编辑 .env，填入 DASHSCOPE_API_KEY（必填）、API_KEY（鉴权密钥）
```

---

## 接口清单（多用户版）

所有用户级接口把 `user_id` 写在路径里（`user_id` 为正整数），按用户隔离知识库。

| 方法 | 路径 | 用途 | 需鉴权 |
|------|------|------|--------|
| GET | `/health` | 健康检查（供主后端探活） | 否 |
| POST | `/api/v1/users/{user_id}/query` | 问答 | 是 |
| POST | `/api/v1/users/{user_id}/documents` | 上传 `.md` 文档并索引 | 是 |
| GET | `/api/v1/users/{user_id}/documents` | 查询文档列表 | 是 |
| DELETE | `/api/v1/users/{user_id}/documents/{文件名}` | 删除文档及索引 | 是 |
| POST | `/api/v1/users/{user_id}/reload` | 重新扫描文档目录并更新索引 | 是 |

除 `/health` 外，所有接口需在请求头带 `X-API-Key: <密钥>`（密钥在 `.env` 的 `API_KEY` 配置）。若 `API_KEY` 留空，则跳过鉴权（仅建议本地调试）。

### 调用示例（curl）

```bash
# 健康检查
curl http://127.0.0.1:8000/health

# 问答（user_id=1）
curl -X POST http://127.0.0.1:8000/api/v1/users/1/query \
  -H "Content-Type: application/json" -H "X-API-Key: 你的密钥" \
  -d '{"question":"宿舍档案里舍长是谁？"}'

# 上传文档（user_id=1）
curl -X POST http://127.0.0.1:8000/api/v1/users/1/documents \
  -H "X-API-Key: 你的密钥" \
  -F "file=@/path/to/新文档.md"

# 文档列表（user_id=1）
curl http://127.0.0.1:8000/api/v1/users/1/documents \
  -H "X-API-Key: 你的密钥"

# 删除文档（user_id=1）
curl -X DELETE http://127.0.0.1:8000/api/v1/users/1/documents/新文档.md \
  -H "X-API-Key: 你的密钥"

# 重新索引（user_id=1）
curl -X POST http://127.0.0.1:8000/api/v1/users/1/reload \
  -H "X-API-Key: 你的密钥"
```

---

## 配置项说明（`.env`）

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `DASHSCOPE_API_KEY` | 无 | 通义千问密钥（必填） |
| `API_KEY` | 空 | 服务访问密钥，空则所有接口不鉴权 |
| `DATA_DIR` | `./app/data` | 知识文档根目录（每个用户子目录 `<DATA_DIR>/<user_id>/`） |
| `CHROMA_PATH` | `./app/chroma_db` | 向量数据库路径 |
| `COLLECTION_NAME` | `my_md_knowledge` | ChromaDB 集合名前缀（实际 `my_md_knowledge_<user_id>`） |
| `EMBED_MODEL_NAME` | `BAAI/bge-small-zh-v1.5` | 嵌入模型 |
| `LLM_MODEL_NAME` | `qwen-plus` | 大语言模型 |
| `LLM_TEMPERATURE` | `0.3` | 回答随机性（越低越严谨） |
| `CHUNK_SIZE` | `512` | 文档切分大小（**优化点**） |
| `CHUNK_OVERLAP` | `50` | 切分重叠（**优化点**） |
| `TOP_K` | `5` | 检索片段数（**优化点**） |
| `MIN_SCORE` | `0` | 相似度阈值，低于该值不采纳；0 表示不启用（**优化点**） |
| `DEFAULT_USER_ID` | `1` | 命令行模式默认用户ID |

> **换嵌入模型提示**：更换 `EMBED_MODEL_NAME` 后，需要删除 `app/chroma_db/` 目录重新构建索引（不同模型的向量空间不兼容）。

---

## 项目结构

```
python_service/
├── .env                     # 本地配置（含真实密钥，不入库）
├── .env.example             # 配置模板（无真实密钥，提交到仓库）
├── requirements.txt         # 依赖清单
├── README.md                # 本文件
└── app/
    ├── config.py            # 配置管家（Pydantic Settings 从 .env 读取）
    ├── main.py              # 命令行问答入口
    ├── server.py            # FastAPI 服务入口
    ├── models/schemas.py    # 接口请求/响应格式
    ├── routers/             # 接口路由（auth / health / users）
    ├── services/            # 业务逻辑（rag_service / index_service）
    ├── data/                # 知识文档根目录（每个用户一个子目录）
    └── chroma_db/           # 向量数据库（自动生成，不入库）
```

## 安全说明

- 真实 API 密钥只写在 `.env`，该文件已被 `.gitignore` 忽略，**绝不提交到仓库**。
- `.env.example` 只包含占位符，可安全提交。
- 上传接口会校验文件名（禁止路径穿越、仅允许 `.md`）。
