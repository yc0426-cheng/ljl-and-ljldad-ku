# 李佳霖与爸爸的 AI 学习库

## 项目介绍

这是一个用于记录、陪伴和监督李佳霖学习人工智能的成长型仓库，重点关注 AI Agent（智能体）与大语言模型相关知识。仓库将保存学习计划、实践代码、阅读笔记、问题思考和阶段总结，使每一次学习都有记录、有反馈，也能看到长期的进步。

本仓库不仅关注“如何使用 AI”，也重视理解 AI 的基本原理、能力边界和安全规范。学习过程中将通过讲解、提问、动手实践和复盘总结，逐步培养独立思考、主动探索和解决实际问题的能力。

## 主要学习内容

- 大语言模型的基本概念、工作方式与应用场景
- 提示词设计、上下文组织和模型结果评估
- AI Agent 的任务规划、工具调用、记忆与执行流程
- 使用 Python 开发简单的 AI 应用和智能体
- AI 使用中的事实核查、隐私保护、安全与责任意识
- 将所学知识应用到真实的小项目中

## 学习与监督方式

- 李佳霖负责完成学习任务、编写代码并记录自己的理解和问题。
- 家长负责协助制定阶段目标、检查学习进度并提供必要的引导。
- 每项学习内容尽量包含“目标、实践、结果、问题、复盘”五个部分。
- 监督以培养自主学习能力为目的，重视思考过程，不以代替完成任务为方式。
- 定期回顾学习记录，根据掌握情况调整后续计划。

## 使用语言与校验规范

目前仓库支持校验以下三类代码：

| 类型 | 常用文件格式 | 最低校验要求 |
| --- | --- | --- |
| Python | `.py` | 代码格式、语法检查和必要的测试 |
| Java | `.java` | 代码格式、编译检查和必要的测试 |
| 前端 | `.html`、`.css`、`.js`、`.ts`、`.vue` | 代码格式、语法或类型检查，以及必要的构建测试 |

- 提交代码时，应使用与编程语言对应的文件扩展名和代码格式。
- 使用上述范围之外的编程语言时，必须同时编写该语言对应的校验脚本，至少覆盖代码格式和语法或编译检查；具备测试条件时还应运行测试。
- 校验脚本应能够直接执行，并在发现错误时返回非零退出状态，便于人工检查和自动化工具判断结果。
- 不包含可执行代码的学习笔记、计划、总结和其他纯文本内容，统一使用 Markdown 格式并保存为 `.md` 文件。
- Markdown 中包含代码示例时，应使用带语言标识的代码块，例如 `python`、`java`、`javascript` 或 `typescript`。

### 统一代码校验脚本

校验脚本固定放在仓库的 `scripts/check_code.py`。它适用于以下位置：

- 在仓库根目录中执行时，可以使用相对路径调用，默认检查整个仓库。
- 在仓库的任意子目录或仓库外执行时，可以使用脚本的绝对路径调用。
- Python 代码或前端项目不在仓库根目录时，可以在命令末尾传入要检查的文件或目录。

首次执行 Python 校验前，需要在当前 Python 环境安装 `pylint`：

```bash
python -m pip install pylint
```

前端项目需要已经安装 `pnpm` 和项目依赖，并在 `package.json` 的 `scripts` 中提供 `lint`、`type-check`、`format`、`build` 四个命令。前端校验会依次运行：

```bash
pnpm lint
pnpm type-check
pnpm format
pnpm build
```

在仓库根目录检查全部 Python 文件：

```bash
python scripts/check_code.py python
```

检查指定的 Python 文件或目录：

```bash
python scripts/check_code.py python path/to/file_or_directory
```

在仓库根目录检查前端项目；如果目标目录中没有直接包含 `package.json`，脚本会查找其下的前端项目：

```bash
python scripts/check_code.py frontend
```

检查指定的前端项目目录：

```bash
python scripts/check_code.py frontend path/to/frontend_project
```

一次检查目标中的 Python 和前端项目：

```bash
python scripts/check_code.py all
python scripts/check_code.py all path/to/project
```

从任意工作目录调用时，使用脚本和检查目标的绝对路径。例如：

```powershell
python D:\path\to\repository\scripts\check_code.py frontend D:\path\to\frontend-project
```

任意一项检查失败、依赖未安装、目标不存在或前端命令缺失时，脚本都会返回非零退出状态。

## Git 分支与合并规范

### 新建工作分支

刚加入项目或准备开始一项新任务时，应先更新本地 `main` 分支，再从 `main` 新建工作分支：

```bash
git checkout main
git pull origin main
git checkout -b feat/功能名
```

分支命名规则：

- 开发新功能：`feat/功能名`
- 修复已有问题：`fix/修复内容`
- 分支名称应简短、明确，能够说明本次工作的目的。

### 同步远程 main 分支

当前工作分支存在修改时，必须先选择提交或暂存，不能直接切换分支并拉取代码。

方式一：提交当前修改。

```bash
git add .
git commit -m "说明本次修改"
```

方式二：暂时不提交，使用带名称的 stash 保存修改。推荐使用：

```bash
git stash push -m "暂存内容名称"
```

旧版 Git 也可以使用：

```bash
git stash save "暂存内容名称"
```

保存当前工作后，依次更新 `main` 并将其合并到自己的工作分支：

```bash
git checkout main
git pull origin main
git checkout feat/功能名
git merge main
```

修复分支应将最后一条切换命令替换为对应的 `fix/修复内容` 分支。

如果之前使用了 stash，先查找对应名称和编号，再恢复修改：

```bash
git stash list
git stash pop "stash@{编号}"
```

恢复或合并时如发生冲突，应先解决冲突并完成代码校验，再继续提交和推送。

### 推送分支并创建合并请求

完成开发、校验和提交后，将当前工作分支推送到远程仓库：

```bash
git push -u origin feat/功能名
```

修复分支应推送对应的 `fix/修复内容`。随后在 GitHub 创建 Pull Request（合并请求）：

- base 分支选择远程 `main`。
- compare 分支选择当前工作分支对应的远程分支。
- 填写清晰的标题、修改说明和验证结果。
- 确认检查通过并完成评审后，再合并到远程 `main`。

## 预期成果

通过持续学习和实践，逐步建立对大语言模型与 AI Agent 的完整认识，能够独立完成基础项目，清楚说明自己的设计思路，并形成良好的学习记录、实验验证和复盘习惯。
