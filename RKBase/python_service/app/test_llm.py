import os
from pathlib import Path

from dotenv import load_dotenv

# 从 python_service/.env 读取 API 密钥
load_dotenv(Path(__file__).resolve().parent.parent / ".env")
if not os.getenv("DASHSCOPE_API_KEY"):
    raise RuntimeError("未找到 DASHSCOPE_API_KEY，请检查 python_service/.env 文件")

from llama_index.llms.dashscope import DashScope
from llama_index.core import Settings

# 测试1：直接调用 complete 方法
print("="*50)
print("测试1：直接调用 LLM complete 方法")
print("="*50)
llm = DashScope(model_name="qwen-plus", api_key=os.getenv("DASHSCOPE_API_KEY"), temperature=0.3)

try:
    response = llm.complete("Python是谁发明的？请用一句话回答。")
    print(f"complete 返回类型: {type(response)}")
    print(f"complete 返回内容: [{response}]")
    print(f"complete text属性: [{response.text}]")
except Exception as e:
    print(f"complete 调用失败: {e}")

# 测试2：调用 chat 方法（LlamaIndex query_engine 内部实际用的是这个）
print("\n" + "="*50)
print("测试2：调用 LLM chat 方法")
print("="*50)
from llama_index.core.llms import ChatMessage

try:
    messages = [
        ChatMessage(role="system", content="你是一个乐于助人的助手。"),
        ChatMessage(role="user", content="Python是谁发明的？请用一句话回答。")
    ]
    response = llm.chat(messages)
    print(f"chat 返回类型: {type(response)}")
    print(f"chat 返回内容: [{response}]")
    print(f"chat message.content: [{response.message.content}]")
except Exception as e:
    print(f"chat 调用失败: {e}")

print("\n测试完成")

# ========================================
# 测试3：直接用 dashscope 原生 SDK 调用
# ========================================
import dashscope
print(f"\n{'='*50}")
print("测试3：dashscope 原生 SDK 调用")
print(f"{'='*50}")

# 先确认环境变量里有没有 key
import os
api_key = os.getenv("DASHSCOPE_API_KEY")
print(f"API Key 是否设置: {bool(api_key)}")
if api_key:
    print(f"API Key 前缀: {api_key[:10]}...")

dashscope.api_key = api_key

response = dashscope.Generation.call(
    model="qwen-turbo",  # 先用最基础的模型
    messages=[
        {"role": "user", "content": "Python是谁发明的？请用一句话回答。"}
    ],
    result_format="message"
)

print(f"状态码: {response.status_code}")
print(f"输出: {response.output}")
print(f"完整响应: {response}")