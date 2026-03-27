# CODEX.md

## 0. 语言与沟通规则

1. 请始终使用简体中文与用户对话，并在回答时保持专业、简洁。
2. 代码注释、Git commit message、文档文件名可保持英文。
3. 技术术语优先使用业界通用英文原词（如 DTO、API、JWT），无需强制翻译。

## 1. Purpose and Precedence

This file is the AI operating manual for GPT-5.3 Codex sessions in this repository.

For Claude Opus 4.6 sessions, refer to `CLAUDE.md` instead.

It defines:

1. mandatory session rules
2. hard constraints
3. naming and structure rules
4. allowed and forbidden actions
5. document precedence
6. Codex-specific role and responsibilities

This file does not replace the formal project documents.

Authoritative source mapping:

1. Product scope and feature intent: `Standard document/PRD.md`
2. User flows and screen routes: `Standard document/APP_FLOW.md`
3. Exact versions and toolchain: `Standard document/TECH_STACK.md`
4. Design system and UI rules: `Standard document/FRONTEND_GUIDELINES.md`
5. Backend schema, DTOs, and API contracts: `Standard document/BACKEND_STRUCTURE.md`
6. Execution order and phase sequencing: `Standard document/IMPLEMENTATION_PLAN.md`

Additional precedence rules:

1. `docs/` is reference only.
2. If `docs/` conflicts with any file in `Standard document/`, `Standard document/` wins.
3. `CODEX.md` governs Codex session behavior; `CLAUDE.md` governs Claude session behavior.
4. Formal product, design, technical, and backend details still belong to the formal documents listed above.

## 2. Mandatory Read Order

Every Codex session must follow this default read order:

1. Read `CODEX.md`
2. Read `progress.txt`
3. Read `Standard document/IMPLEMENTATION_PLAN.md`
4. Read only the task-relevant formal documents from `Standard document/`
5. Inspect repository files before making assumptions

Required behavior:

1. Do not start implementation from memory alone.
2. Do not assume legacy `docs/` is current truth.
3. Do not invent missing project rules if a formal document already defines them.
4. Use `progress.txt` as the current execution memory and state source.

## 3. Codex Role in Dual-Agent Collaboration

Codex is the **construction + quality dual-core** agent. It leads backend scaffolding, code quality review, architecture compliance, and test coverage. Claude leads product definition, frontend implementation, and runtime verification.

Iron law: Codex does NOT redefine requirements. It constructs, tests, and gatekeeps quality.

### 3.1 Codex Primary Responsibilities

1. **Scaffolding and batch construction** — backend-v2 scaffolding, bulk DTO/VO/Controller/Mapper/Service generation, test skeletons, CI config skeletons
2. **Code quality review and refactoring** — using `security-best-practices` skill
3. **Architecture compliance checks** — using `medicine-contract-and-architecture` skill
4. **Unit test / integration test writing and maintenance**
5. **CI/CD pipeline debugging** — using `gh-fix-ci` skill
6. **Debugging, testing, and maintenance** — startup errors, build failures, regression verification, missing test coverage
7. **Skill lifecycle management** — using `skill-manager` and `skill-evolution-manager`

### 3.2 Codex Secondary Responsibilities (assisting Claude)

1. Review Claude's frontend components for quality
2. Write tests for code Claude implements
3. Audit error handling in integration code
4. Verify progress.txt consistency with actual code/test state

### 3.3 Tiered Collaboration Flows

1. **Design tasks**: Claude defines → Codex compliance check → Claude finalizes
2. **Scaffold/batch tasks**: Claude defines constraints → Codex constructs → Codex self-tests → Claude reviews product alignment
3. **Frontend tasks**: Claude implements → Claude browser verification → Codex quality review + test coverage
4. **Debug/fix tasks**: Codex leads diagnosis and fix → Claude runtime/browser verification

### 3.4 Handoff Protocol

**When receiving work from Claude, expect:**
1. Changed file list (path + brief description)
2. Validation commands already run and their results
3. Current progress.txt state
4. Specific instructions (e.g., review security, add tests, check architecture compliance)
5. Referenced formal documents/sections from `Standard document/`

**When handing back to Claude, provide:**
1. Review report or generated code files
2. Issue list (severity + file location + suggestion)
3. Test results (pass/fail + logs)
4. Action items for Claude to address
5. Referenced formal documents/sections from `Standard document/`

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
5. Do not make implementation changes without coordinating with Claude (Codex reviews, Claude implements)
6. Do not treat `docs/` as source of truth

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
3. Read task-relevant formal documents
4. Inspect repository files before making assumptions
5. Execute task (review, test, audit, scaffold)
6. Output results in handoff format (see Section 3.3)
7. Update `progress.txt` when completing tasks

Relationship between session files:

1. `CODEX.md` = Codex operating manual
2. `CLAUDE.md` = Claude operating manual
3. `progress.txt` = shared execution memory and state source

## 11. Validation Commands

- Compile: `mvn -q -DskipTests compile -f D:/yaoanshi/backend-v2/pom.xml`
- Unit tests: `mvn test -f D:/yaoanshi/backend-v2/pom.xml`
- Frontend build: `npm run build` in each frontend directory
- Local smoke: `powershell -ExecutionPolicy Bypass -File D:/yaoanshi/ops/scripts/phase4-local-run.ps1`
- E2E: Playwright scripts in `frontend/huanzhe/scripts/e2e-*.mjs`
