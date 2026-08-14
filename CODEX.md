# CODEX.md

## 0. 语言与沟通规则

1. 请始终使用简体中文与用户对话，并在回答时保持专业、简洁。
2. 代码注释、Git commit message、文档文件名可保持英文。
3. 技术术语优先使用业界通用英文原词（如 DTO、API、JWT），无需强制翻译。

## 1. Purpose and Precedence

This file is the AI operating manual for Codex sessions in this repository.

It defines:

1. mandatory session rules
2. hard constraints
3. naming and structure rules
4. allowed and forbidden actions
5. document precedence
6. shared workflow and output rules

This file does not replace the formal project documents.

Authoritative source mapping:

1. Product scope and feature intent: `Standard document/PRD.md`
2. User flows and screen routes: `Standard document/APP_FLOW.md`
3. Exact versions and toolchain: `Standard document/TECH_STACK.md`
4. Design system and UI rules: `Standard document/FRONTEND_GUIDELINES.md`
5. Backend schema, DTOs, and API contracts: `Standard document/BACKEND_STRUCTURE.md`
6. Execution order and phase sequencing: `Standard document/IMPLEMENTATION_PLAN.md`

Additional precedence rules:

1. `Standard document/*.md` is the only project knowledge base.
2. The `docs/` tree has been removed and must not be consulted as project knowledge, reference storage, or a plan output location.
3. `CODEX.md`, `CLAUDE.md`, and `AGENTS.md` share the same operating protocol; they do not create separate product or implementation authority.
4. Formal product, design, technical, and backend details still belong to the formal documents listed above.

## 2. Mandatory Read Order

Every Codex session must follow this default read order:

1. Read `CODEX.md`
2. Read `progress.txt`
3. Read `Standard document/IMPLEMENTATION_PLAN.md`
4. Select task-relevant formal documents from the matrix below
5. Inspect repository files before making assumptions

Task-to-document lookup matrix:

1. Product scope, user stories, success criteria, non-goals: `Standard document/PRD.md`
2. Pages, routes, user flows, triggers, success/error paths: `Standard document/APP_FLOW.md`
3. Dependencies, versions, build tools, runtime requirements: `Standard document/TECH_STACK.md`
4. UI tokens, components, layout, responsive rules: `Standard document/FRONTEND_GUIDELINES.md`
5. Database, API, DTO, auth, storage, migrations: `Standard document/BACKEND_STRUCTURE.md`
6. Build order, phase sequence, validation gates: `Standard document/IMPLEMENTATION_PLAN.md`

Required behavior:

1. Do not start implementation from memory alone.
2. Do not recreate or consult `docs/` for project knowledge.
3. Do not invent missing project rules if a formal document already defines them.
4. Use `progress.txt` as the current execution memory and state source.
5. Temporary implementation plans stay in the conversation; only durable build sequence changes belong in `Standard document/IMPLEMENTATION_PLAN.md`.

## 3. Unified AI Collaboration Model

All AI assistants working in this repository follow the same workflow: read the session manual, read `progress.txt`, consult the task-relevant formal documents, inspect the repo, state the files and plan before editing, implement the smallest safe diff, validate, and update progress when work is completed.

No assistant may redefine product requirements, invent contracts, or bypass the formal documents because of its tool identity.

For handoff or review work, provide changed files, validation results, referenced formal document sections, open risks, and next actions.

## 4. Project Snapshot

### 4.1 Product surfaces

Active product surfaces:

1. Patient Android app: `frontend/huanzhe`
2. Pharmacist Web: `frontend/pharmacist`
3. Doctor Web: `frontend/yisheng/web-admin`

### 4.2 Backend zones

1. Legacy backend: `backend`
2. New backend target: `backend-v2`

### 4.3 Fixed route prefixes

1. Patient and family APIs: `/app-api/**`
2. Pharmacist and doctor APIs: `/admin-api/**`

### 4.4 Response baseline

All new backend responses must use: `CommonResult<T>`

## 5. Allowed / Forbidden Actions

### 5.1 Always allowed

1. Read formal documents first
2. Inspect repository files before editing
3. Run code quality and security audits at any time
4. Write and run tests
5. Suggest refactoring improvements

### 5.2 Always forbidden unless explicitly approved

1. Do not modify legacy `backend`
2. Do not copy legacy backend code into `backend-v2`
3. Do not invent endpoints, DTOs, file paths, package roots, or dependency versions
4. Do not silently rename public DTOs or route prefixes
5. Do not make implementation changes without checking the task-relevant formal documents first
6. Do not use or recreate `docs/` as project knowledge, reference storage, or plan output

### 5.3 Default attitude

1. Review before rewrite
2. Suggest improvements, do not force changes
3. Prefer targeted fixes over broad refactors
4. Tests must match existing code contracts, not invent new ones

## 6. Tech Stack Summary

For exact versions, always read `Standard document/TECH_STACK.md`.

### 6.1 Frontend

1. Patient app: Ionic Vue 8 + Vue 3
2. Pharmacist web: Vue 3 + Element Plus 2.3.7
3. Doctor web: Vue 3 + Element Plus 2.11.2

### 6.2 Backend

1. New backend target: Java 17 + Spring Boot 3.2.12
2. Persistence: MyBatis Plus
3. Cache/session support: Redis
4. API docs: SpringDoc

### 6.3 Legacy

1. Legacy backend remains Java 11 + Spring Boot 2.7.18
2. Legacy backend is frozen — do not target for new work

## 7. Naming and Structure Rules

### 7.1 Backend rules

1. Backend package root: `com.medicine`
2. Required backend top-level packages: `api`, `service`, `repository`, `domain`, `config`, `common`
3. New backend DTO naming must align with `Standard document/BACKEND_STRUCTURE.md`
4. Do not expose entities directly from controllers

### 7.2 Frontend rules

Patient app: `src/app`, `src/modules`, `src/shared`
Pharmacist web: `src/modules/pharmacist/*`, shared under `src/app` and `src/shared`
Doctor web: MVP routes and menu only

### 7.3 Service boundary rules

1. New service modules own API calls
2. Page components must not hardcode URLs
3. DTO names must match formal backend contract names

## 8. Backend Implementation Rules

For exact schema, DTO, and endpoint details, read `Standard document/BACKEND_STRUCTURE.md`.

Public names that must not be renamed without updating the formal contract:

1. `TokenPair`
2. `BindPatientRequest`
3. `MedicationPlanDTO`
4. `DoseTaskDTO`
5. `CheckinRecordDTO`
6. `PrescriptionDTO`
7. `HelpOrderDTO`

## 9. Codex Skills Reference

| Skill | Purpose |
|-------|---------|
| `medicine-contract-and-architecture` | Verify backend-v2 against BACKEND_STRUCTURE.md |
| `security-best-practices` | Security audit for backend + frontend code |
| `gh-fix-ci` | Diagnose and fix CI/CD pipeline failures |
| `playwright-interactive` | Persistent browser debugging sessions |
| `doc` | Generate Word deliverables |
| `spreadsheet` | Export data / report analysis |
| `figma-implement-design` | Translate Figma designs to code |
| `skill-evolution-manager` | Iterate skills based on feedback |
| `skill-manager` | Check and update skill versions |

## 10. Session Workflow and Output Contract

Every Codex session should follow this workflow:

1. Read `CODEX.md`
2. Read `progress.txt`
3. Read `Standard document/IMPLEMENTATION_PLAN.md`
4. Identify task-relevant formal documents using the lookup matrix in Section 2
5. Inspect repository files before making assumptions
6. Execute task (review, test, audit, scaffold)
7. Output results with changed files, validation, open risks, and next actions
8. Update `progress.txt` when completing tasks

Relationship between session files:

1. `CODEX.md`, `CLAUDE.md`, and `AGENTS.md` = shared AI operating protocol for this repository
2. `progress.txt` = shared execution memory and state source

## 11. Validation Commands

- Compile: `mvn -q -DskipTests compile -f D:/yaoanshi/backend-v2/pom.xml`
- Unit tests: `mvn test -f D:/yaoanshi/backend-v2/pom.xml`
- Frontend build: `npm run build` in each frontend directory
- Local smoke: `powershell -ExecutionPolicy Bypass -File D:/yaoanshi/ops/scripts/phase4-local-run.ps1`
- E2E: Playwright scripts in `frontend/huanzhe/scripts/e2e-*.mjs`
