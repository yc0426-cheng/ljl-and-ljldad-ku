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
    """运行命令并返回退出状态。"""
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
        return 2

    if result.returncode == 0:
        print("[PASS] 检查通过")
    else:
        print(
            f"[FAIL] 检查失败，退出状态为 {result.returncode}",
            file=sys.stderr,
        )
    return result.returncode


def check_python(target: Path, required: bool = True) -> int:
    """使用 pylint 检查 Python 文件。"""
    python_files = find_python_files(target)
    if not python_files:
        message = f"目标中没有找到 Python 文件：{target}"
        if required:
            print(f"[ERROR] {message}", file=sys.stderr)
            return 2
        print(f"[SKIP] {message}")
        return 0

    if importlib.util.find_spec("pylint") is None:
        print(
            "[ERROR] 当前 Python 环境未安装 pylint；请先执行："
            f"{sys.executable} -m pip install pylint",
            file=sys.stderr,
        )
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


def check_frontend(target: Path, required: bool = True) -> int:
    """依次执行前端项目约定的 pnpm 检查。"""
    projects = find_frontend_projects(target)
    if not projects:
        message = f"目标中没有找到前端项目（package.json）：{target}"
        if required:
            print(f"[ERROR] {message}", file=sys.stderr)
            return 2
        print(f"[SKIP] {message}")
        return 0

    pnpm_executable = shutil.which("pnpm")
    if pnpm_executable is None:
        print("[ERROR] 未找到 pnpm，请先安装并加入 PATH", file=sys.stderr)
        return 2

    first_failure = 0
    for project in projects:
        package_json = project / "package.json"
        scripts = read_package_scripts(package_json)
        if scripts is None:
            first_failure = first_failure or 2
            continue

        missing_scripts = [name for name in FRONTEND_SCRIPTS if name not in scripts]
        if missing_scripts:
            print(
                f"[ERROR] {package_json} 缺少脚本：{', '.join(missing_scripts)}",
                file=sys.stderr,
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


def build_parser() -> argparse.ArgumentParser:
    """创建命令行参数解析器。"""
    parser = argparse.ArgumentParser(
        description="按语言执行 Python 或前端代码检查。",
    )
    parser.add_argument(
        "language",
        choices=("python", "frontend", "all"),
        help="python 使用 pylint；frontend 使用 pnpm；all 执行两类检查。",
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
        return check_python(target)
    if arguments.language == "frontend":
        return check_frontend(target)

    python_status = check_python(target, required=False)
    frontend_status = check_frontend(target, required=False)
    return python_status or frontend_status


if __name__ == "__main__":
    raise SystemExit(main())
