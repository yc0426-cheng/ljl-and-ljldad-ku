#!/usr/bin/env python3
"""按代码类型执行仓库约定的质量检查。"""

from __future__ import annotations

import argparse
import importlib.util
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path
from typing import Iterator, Sequence


REPOSITORY_ROOT = Path(__file__).resolve().parent.parent
EXCLUDED_DIRECTORIES = frozenset(
    {
        ".git",
        ".idea",
        ".mypy_cache",
        ".pnpm-store",
        ".pytest_cache",
        ".venv",
        ".vscode",
        "__pycache__",
        "build",
        "dist",
        "node_modules",
        "target",
        "venv",
    }
)
FRONTEND_SCRIPTS = ("lint", "type-check", "format", "build")

# 执行结果汇总：(步骤名称, 工作目录, 结果描述)，全部步骤结束后统一输出
CHECK_RESULTS: list[tuple[str, str, str]] = []


def record_result(step: str, working_directory: Path, outcome: str) -> None:
    """记录一个检查步骤的结果（含未真正起子进程的失败分支）。"""
    CHECK_RESULTS.append((step, str(working_directory), outcome))


def iter_project_files(root: Path) -> Iterator[Path]:
    """遍历项目文件，并跳过依赖、缓存和构建目录。"""
    for current_directory, directory_names, file_names in os.walk(str(root)):
        directory_names[:] = sorted(
            name for name in directory_names if name not in EXCLUDED_DIRECTORIES
        )
        current_path = Path(current_directory)
        for file_name in sorted(file_names):
            yield current_path / file_name


def find_python_files(target: Path) -> list[Path]:
    """查找目标文件或目录中的 Python 源文件。"""
    if target.is_file():
        return [target] if target.suffix.lower() == ".py" else []

    return [path for path in iter_project_files(target) if path.suffix.lower() == ".py"]


def find_frontend_projects(target: Path) -> list[Path]:
    """查找包含 package.json 的前端项目目录。"""
    if target.is_file():
        return [target.parent] if target.name == "package.json" else []

    direct_package = target / "package.json"
    if direct_package.is_file():
        return [target]

    return [
        path.parent for path in iter_project_files(target) if path.name == "package.json"
    ]


def run_command(command: Sequence[str], working_directory: Path) -> int:
    """运行命令并返回退出状态；结果同时记录到 CHECK_RESULTS 供最后汇总输出。"""
    display_command = subprocess.list2cmdline(list(command))
    print(f"\n[RUN] {display_command}")
    print(f"[DIR] {working_directory}")

    try:
        result = subprocess.run(
            command,
            cwd=working_directory,
            check=False,
        )
    except OSError as error:
        print(f"[ERROR] 无法启动命令：{error}", file=sys.stderr)
        record_result(display_command, working_directory, "无法启动")
        return 2

    if result.returncode == 0:
        print("[PASS] 检查通过")
        record_result(display_command, working_directory, "通过")
    else:
        print(
            f"[FAIL] 检查失败，退出状态为 {result.returncode}",
            file=sys.stderr,
        )
        record_result(
            display_command, working_directory, f"失败（退出状态 {result.returncode}）"
        )
    return result.returncode


def check_python(target: Path, required: bool = True) -> int:
    """使用 pylint 检查 Python 文件。"""
    python_files = find_python_files(target)
    if not python_files:
        message = f"目标中没有找到 Python 文件：{target}"
        if required:
            print(f"[ERROR] {message}", file=sys.stderr)
            record_result("pylint（收集 Python 文件）", target, "未找到 Python 文件")
            return 2
        print(f"[SKIP] {message}")
        return 0

    if importlib.util.find_spec("pylint") is None:
        print(
            "[ERROR] 当前 Python 环境未安装 pylint；请先执行："
            f"{sys.executable} -m pip install pylint",
            file=sys.stderr,
        )
        record_result("pylint", target, "未安装 pylint")
        return 2

    command = [
        sys.executable,
        "-m",
        "pylint",
        *(str(path) for path in python_files),
    ]
    working_directory = target if target.is_dir() else target.parent
    return run_command(command, working_directory)


def read_package_scripts(package_json: Path) -> dict[str, object] | None:
    """读取 package.json 中的 scripts 配置。"""
    try:
        package_data = json.loads(package_json.read_text(encoding="utf-8"))
    except (OSError, UnicodeError, json.JSONDecodeError) as error:
        print(f"[ERROR] 无法读取 {package_json}：{error}", file=sys.stderr)
        return None

    scripts = package_data.get("scripts")
    if not isinstance(scripts, dict):
        print(f"[ERROR] {package_json} 中未定义 scripts", file=sys.stderr)
        return None
    return scripts


def find_maven_projects(target: Path) -> list[Path]:
    """查找包含 pom.xml 的后端 Maven 项目目录。"""
    if target.is_file():
        return [target.parent] if target.name == "pom.xml" else []

    direct_pom = target / "pom.xml"
    if direct_pom.is_file():
        return [target]

    return [path.parent for path in iter_project_files(target) if path.name == "pom.xml"]


def check_backend(target: Path, required: bool = True) -> int:
    """依次执行后端 Maven 项目的 mvn clean 和 mvn compile。"""
    projects = find_maven_projects(target)
    if not projects:
        message = f"目标中没有找到后端项目（pom.xml）：{target}"
        if required:
            print(f"[ERROR] {message}", file=sys.stderr)
            record_result("maven（查找 pom.xml）", target, "未找到 pom.xml")
            return 2
        print(f"[SKIP] {message}")
        return 0

    mvn_executable = shutil.which("mvn")
    if mvn_executable is None:
        print("[ERROR] 未找到 mvn，请先安装 Maven 并加入 PATH", file=sys.stderr)
        record_result("maven", target, "未安装 Maven")
        return 2

    first_failure = 0
    for project in projects:
        for step in ("clean", "compile"):
            # Windows 下 mvn 通常是 mvn.cmd，subprocess 直接写 "mvn" 会 WinError 2，
            # 必须传 which 解析出的完整路径
            return_code = run_command([mvn_executable, step], project)
            first_failure = first_failure or return_code

    return first_failure


def check_frontend(target: Path, required: bool = True) -> int:
    """依次执行前端项目约定的 pnpm 检查。"""
    projects = find_frontend_projects(target)
    if not projects:
        message = f"目标中没有找到前端项目（package.json）：{target}"
        if required:
            print(f"[ERROR] {message}", file=sys.stderr)
            record_result("pnpm（查找 package.json）", target, "未找到前端项目")
            return 2
        print(f"[SKIP] {message}")
        return 0

    pnpm_executable = shutil.which("pnpm")
    if pnpm_executable is None:
        print("[ERROR] 未找到 pnpm，请先安装并加入 PATH", file=sys.stderr)
        record_result("pnpm", target, "未安装 pnpm")
        return 2

    first_failure = 0
    for project in projects:
        package_json = project / "package.json"
        scripts = read_package_scripts(package_json)
        if scripts is None:
            record_result("pnpm（读取 package.json）", project, "读取 package.json 失败")
            first_failure = first_failure or 2
            continue

        missing_scripts = [name for name in FRONTEND_SCRIPTS if name not in scripts]
        if missing_scripts:
            print(
                f"[ERROR] {package_json} 缺少脚本：{', '.join(missing_scripts)}",
                file=sys.stderr,
            )
            record_result(
                "pnpm（检查 scripts）",
                project,
                f"缺少脚本：{', '.join(missing_scripts)}",
            )
            first_failure = first_failure or 2
            continue

        for script_name in FRONTEND_SCRIPTS:
            return_code = run_command(
                [pnpm_executable, script_name],
                project,
            )
            first_failure = first_failure or return_code

    return first_failure


def print_summary() -> None:
    """全部检查结束后，统一输出每个步骤的执行结果。"""
    print("\n" + "=" * 60)
    print("检查结果汇总")
    print("=" * 60)

    if not CHECK_RESULTS:
        print("（没有执行任何检查步骤）")
        return

    for index, (step, directory, outcome) in enumerate(CHECK_RESULTS, start=1):
        mark = "✓" if outcome == "通过" else "✗"
        print(f"{index:>2}. [{mark}] {step}")
        print(f"      目录：{directory}")
        print(f"      结果：{outcome}")

    failed = sum(1 for _, _, outcome in CHECK_RESULTS if outcome != "通过")
    print("-" * 60)
    total = len(CHECK_RESULTS)
    if failed:
        print(f"共 {total} 步，{total - failed} 步通过，{failed} 步失败")
    else:
        print(f"共 {total} 步，全部通过")


def build_parser() -> argparse.ArgumentParser:
    """创建命令行参数解析器。"""
    parser = argparse.ArgumentParser(
        description="按语言执行后端、Python 或前端代码检查。",
    )
    parser.add_argument(
        "language",
        choices=("python", "frontend", "backend", "all"),
        help="python 使用 pylint；frontend 使用 pnpm；backend 使用 maven；all 执行三类检查。",
    )
    parser.add_argument(
        "target",
        nargs="?",
        default=str(REPOSITORY_ROOT),
        help="要检查的文件或目录；默认检查脚本所在仓库。",
    )
    return parser


def main() -> int:
    """解析参数并执行相应检查。"""
    arguments = build_parser().parse_args()
    target = Path(arguments.target).expanduser().resolve()
    if not target.exists():
        print(f"[ERROR] 检查目标不存在：{target}", file=sys.stderr)
        return 2

    if arguments.language == "python":
        status = check_python(target)
    elif arguments.language == "frontend":
        status = check_frontend(target)
    elif arguments.language == "backend":
        status = check_backend(target)
    else:
        python_status = check_python(target, required=False)
        frontend_status = check_frontend(target, required=False)
        backend_status = check_backend(target, required=False)
        status = python_status or frontend_status or backend_status

    print_summary()
    return status


if __name__ == "__main__":
    raise SystemExit(main())
