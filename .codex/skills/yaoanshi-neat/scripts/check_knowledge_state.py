#!/usr/bin/env python3
"""Read-only knowledge-state checker for the Yaoanshi repository."""

from __future__ import annotations

import argparse
import re
from pathlib import Path


STANDARD_DOCS = [
    "PRD.md",
    "APP_FLOW.md",
    "TECH_STACK.md",
    "FRONTEND_GUIDELINES.md",
    "BACKEND_STRUCTURE.md",
    "IMPLEMENTATION_PLAN.md",
]

PROGRESS_SECTIONS = [
    "Progress Maintenance Rules",
    "Current State Update Template",
    "Project",
    "Branch / Context",
    "Current State",
    "In Progress",
    "Next",
    "Last Updated",
]

ROOT_INSTRUCTION_FILES = ["AGENTS.md", "CODEX.md", "CLAUDE.md"]
RELATIVE_TIME_PATTERN = re.compile(
    r"(今天|昨天|刚刚|最近|上周|下周|明天|\btoday\b|\byesterday\b|\brecently\b|\blast week\b|\bnext week\b)",
    re.IGNORECASE,
)
MOJIBAKE_PATTERN = re.compile(r"(鈥|鉁|涓|琛|鎴|姹|棣|鑽|濂|璁|杩|�)")


def read_utf8(path: Path) -> tuple[str | None, str | None]:
    try:
        return path.read_text(encoding="utf-8"), None
    except UnicodeDecodeError as exc:
        return None, f"UTF-8 decode failed: {exc}"
    except OSError as exc:
        return None, f"read failed: {exc}"


def line_count(text: str) -> int:
    return len(text.splitlines())


def scan_markers(label: str, path: Path, text: str, warnings: list[str]) -> None:
    relative_hits = RELATIVE_TIME_PATTERN.findall(text)
    mojibake_hits = MOJIBAKE_PATTERN.findall(text)
    if relative_hits:
        warnings.append(f"{label}: contains relative-time markers ({len(relative_hits)} hit(s))")
    if mojibake_hits:
        warnings.append(f"{label}: contains possible mojibake markers ({len(mojibake_hits)} hit(s))")

    lines = line_count(text)
    if path.name in ("AGENTS.md", "CODEX.md", "CLAUDE.md") and lines > 350:
        warnings.append(f"{label}: root instruction file is large ({lines} lines)")
    if path.name == "progress.txt" and lines > 500:
        warnings.append(f"{label}: progress memory is large ({lines} lines); consider summarizing only after approval")
    if path.parent.name == "Standard document" and lines > 1500:
        warnings.append(f"{label}: formal document is large ({lines} lines)")


def main() -> int:
    parser = argparse.ArgumentParser(description="Check Yaoanshi knowledge-state files without modifying them.")
    parser.add_argument("repo", nargs="?", default=".", help="Repository root, default: current directory")
    args = parser.parse_args()

    repo = Path(args.repo).resolve()
    errors: list[str] = []
    warnings: list[str] = []
    ok: list[str] = []

    if not repo.exists():
        print(f"# Yaoanshi Knowledge Check\n\n## ERROR\n\n- Repository does not exist: `{repo}`")
        return 2

    progress = repo / "progress.txt"
    if not progress.exists():
        errors.append("progress.txt is missing")
    else:
        text, error = read_utf8(progress)
        if error:
            errors.append(f"progress.txt: {error}")
        else:
            assert text is not None
            missing_sections = [section for section in PROGRESS_SECTIONS if section not in text]
            if missing_sections:
                warnings.append(f"progress.txt: missing expected section(s): {', '.join(missing_sections)}")
            else:
                ok.append("progress.txt: UTF-8 readable and expected sections found")
            scan_markers("progress.txt", progress, text, warnings)

    standard_dir = repo / "Standard document"
    if not standard_dir.exists():
        errors.append("Standard document directory is missing")
    else:
        missing_docs = [name for name in STANDARD_DOCS if not (standard_dir / name).exists()]
        if missing_docs:
            errors.append(f"Standard document: missing file(s): {', '.join(missing_docs)}")
        else:
            ok.append("Standard document: all 6 formal documents found")
        for name in STANDARD_DOCS:
            path = standard_dir / name
            if not path.exists():
                continue
            text, error = read_utf8(path)
            if error:
                errors.append(f"Standard document/{name}: {error}")
            else:
                assert text is not None
                scan_markers(f"Standard document/{name}", path, text, warnings)

    docs_dir = repo / "docs"
    if docs_dir.exists():
        errors.append("docs/ exists but this project forbids docs/ as a knowledge base")
    else:
        ok.append("docs/: not present")

    for name in ROOT_INSTRUCTION_FILES:
        path = repo / name
        if not path.exists():
            continue
        text, error = read_utf8(path)
        if error:
            errors.append(f"{name}: {error}")
        else:
            assert text is not None
            scan_markers(name, path, text, warnings)

    print("# Yaoanshi Knowledge Check")
    print(f"\nRepository: `{repo}`")

    print("\n## OK")
    if ok:
        for item in ok:
            print(f"- {item}")
    else:
        print("- No OK checks recorded")

    print("\n## WARN")
    if warnings:
        for item in warnings:
            print(f"- {item}")
    else:
        print("- No warnings")

    print("\n## ERROR")
    if errors:
        for item in errors:
            print(f"- {item}")
    else:
        print("- No errors")

    return 2 if errors else 0


if __name__ == "__main__":
    raise SystemExit(main())
