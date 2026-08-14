# Global rules for codex

## Operating principles

- Prefer small, reviewable diffs. Avoid sweeping refactors unless explicitly requested.
- Before editing, identify the file(s) to change and state the plan in 3-6 bullets.
- Never invent APIs, configs, or file paths. If unsure, search the repo first.
- Keep changes consistent with existing style and architecture.

## Safety and secrets

- Never paste secrets, tokens, private keys, `.env` values, or credentials into code or logs.
- If a task requires secrets, ask me to provide them via environment variables or approved secret tooling.
- Do not add analytics, telemetry, or network calls unless I ask.

## Code quality bar

- Add or update tests for behavior changes when the project has tests.
- Prefer type safety and explicit error handling.
- Add comments only when the intent is non-obvious.

## Build and run etiquette

- If you need to run commands, propose the exact command and why.
- When you make changes that may break build, run the fastest relevant check first.

## Output formatting

- For code changes: include a short summary + list of files changed.
- For debugging: include hypotheses, experiments run, and the minimal fix.

## My preferences

- I like concise explanations, concrete steps, and copy-pastable commands.
- Default language for explanations: Chinese.

## Standard knowledge base

The only project knowledge base is `Standard document/*.md`.
The `docs/` tree has been removed and must not be consulted as project knowledge, reference storage, or a plan output location.

Task-to-document lookup matrix:

- Product scope, user stories, success criteria, non-goals: `Standard document/PRD.md`
- Pages, routes, user flows, triggers, success/error paths: `Standard document/APP_FLOW.md`
- Dependencies, versions, build tools, runtime requirements: `Standard document/TECH_STACK.md`
- UI tokens, components, layout, responsive rules: `Standard document/FRONTEND_GUIDELINES.md`
- Database, API, DTO, auth, storage, migrations: `Standard document/BACKEND_STRUCTURE.md`
- Build order, phase sequence, validation gates: `Standard document/IMPLEMENTATION_PLAN.md`

Workflow rules:

- Read `progress.txt` before implementation work to recover current state.
- Read `Standard document/IMPLEMENTATION_PLAN.md` for phase/order constraints.
- Then read only the task-relevant formal documents from the matrix above.
- Temporary implementation plans stay in the conversation; only durable build sequence changes belong in `Standard document/IMPLEMENTATION_PLAN.md`.

## Skills

Only the skills listed below are enabled by default for this repository.
Any skill not listed here should be treated as temporarily disabled unless I explicitly ask to enable it again.

Core set for this repo:

- `medicine-contract-and-architecture`
- `Code`
- `security-best-practices`
- `brainstorming`
- `writing-plans`
- `executing-plans`
- `playwright-interactive`
- `doc`
- `1password`
- `frontend-design`
- `spreadsheet`
- `Memory`

On-demand set for this repo:

- `gh-fix-ci`
- `session-logs`
- `security-auditor`
- `ui-ux-pro-max`
- `find-skills`
- `skill-manager`

### Available skills

- `1password`: Set up and use 1Password CLI (`op`). Use when installing the CLI, enabling desktop app integration, signing in, or reading/injecting secrets via `op`. (file: `C:/Users/Z5089/.agents/skills/1password-1.0.1/SKILL.md`)
- `Code`: Coding workflow with planning, implementation, verification, and testing for clean software development. (file: `C:/Users/Z5089/.agents/skills/code-1.0.4/SKILL.md`)
- `Memory`: Infinite organized memory that complements built-in memory with categorized storage. (file: `C:/Users/Z5089/.agents/skills/memory-1.0.2/SKILL.md`)
- `brainstorming`: Must be used before creative work, feature work, or behavior changes to clarify intent and design before implementation. (file: `C:/Users/Z5089/.agents/skills/brainstorming-0.1.0/SKILL.md`)
- `doc`: Use when reading, creating, or editing `.docx` documents, especially when formatting fidelity matters. (file: `C:/Users/Z5089/.codex/skills/doc/SKILL.md`)
- `executing-plans`: Use when you already have a written implementation plan and need to execute it with checkpoints. (file: `C:/Users/Z5089/.agents/skills/executing-plans-0.1.0/SKILL.md`)
- `find-skills`: Helps discover installable skills when a missing capability is explicitly being searched for. (file: `C:/Users/Z5089/.agents/skills/find-skills/SKILL.md`)
- `frontend-design`: Create distinctive, production-grade frontend interfaces while respecting the existing visual system. (file: `C:/Users/Z5089/.agents/skills/frontend-design-3-0.1.0/SKILL.md`)
- `gh-fix-ci`: Use when debugging or fixing failing GitHub PR checks that run in GitHub Actions. (file: `C:/Users/Z5089/.codex/skills/gh-fix-ci/SKILL.md`)
- `medicine-contract-and-architecture`: Spring Boot 3 backend-v2 architecture governance, API contract enforcement, and dual-backend routing rules for the medicine project. (file: `C:/Users/Z5089/.codex/skills/medicine-contract-and-architecture/SKILL.md`)
- `playwright-interactive`: Persistent browser and Electron interaction for iterative UI debugging. (file: `C:/Users/Z5089/.codex/skills/playwright-interactive/SKILL.md`)
- `security-auditor`: Security scanner and reviewer for vulnerabilities, auth flows, input validation, and OWASP-style issues. (file: `C:/Users/Z5089/.agents/skills/security-auditor-1.0.0/SKILL.md`)
- `security-best-practices`: Perform language and framework specific security best-practice reviews and suggest improvements. (file: `C:/Users/Z5089/.codex/skills/security-best-practices/SKILL.md`)
- `session-logs`: Search and analyze your own session logs using `jq`. (file: `C:/Users/Z5089/.agents/skills/session-logs-1.0.0/SKILL.md`)
- `skill-manager`: Lifecycle manager for GitHub-based skills; use to scan, check updates, and perform guided upgrades. (file: `C:/Users/Z5089/.codex/skills/skill-manager/SKILL.md`)
- `spreadsheet`: Use when tasks involve creating, editing, analyzing, or formatting spreadsheets (`.xlsx`, `.csv`, `.tsv`). (file: `C:/Users/Z5089/.codex/skills/spreadsheet/SKILL.md`)
- `ui-ux-pro-max`: UI/UX design guidance for polished interfaces, information architecture, component specs, and accessibility. (file: `C:/Users/Z5089/.agents/skills/ui-ux-pro-max-0.1.0/SKILL.md`)
- `writing-plans`: Use when you have requirements for a multi-step task and need a plan before touching code. (file: `C:/Users/Z5089/.agents/skills/writing-plans-0.1.0/SKILL.md`)

## How to use skills

- Trigger rules: If I name a skill explicitly, or the task clearly matches one of the enabled skills above, use it for that turn.
- If a skill is not listed in this file, treat it as disabled for this repository unless I explicitly ask to re-enable it.
- Read only the minimum necessary part of a skill's `SKILL.md` to follow the workflow.
- Keep context small and load only the files needed for the current task.
