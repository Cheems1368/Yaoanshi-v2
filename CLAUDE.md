# CLAUDE.md

## 0. 语言与沟通规则

1. 请始终使用简体中文与用户对话，并在回答时保持专业、简洁。
2. 代码注释、Git commit message、文档文件名可保持英文。
3. 技术术语优先使用业界通用英文原词（如 DTO、API、JWT），无需强制翻译。

## 1. Purpose and Precedence

This file is the AI operating manual for this repository.

It defines:

1. mandatory session rules
2. hard constraints
3. naming and structure rules
4. allowed and forbidden actions
5. document precedence
6. language and communication rules

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
3. `CLAUDE.md` governs Claude session behavior; `CODEX.md` governs Codex session behavior.
4. Formal product, design, technical, and backend details still belong to the formal documents listed above.

## 2. Mandatory Read Order

Every AI session must follow this default read order:

1. Read `CLAUDE.md`
2. Read `progress.txt`
3. Read `Standard document/IMPLEMENTATION_PLAN.md`
4. Read only the task-relevant formal documents from `Standard document/`
5. Inspect repository files before making assumptions

Required behavior:

1. Do not start implementation from memory alone.
2. Do not assume legacy `docs/` is current truth.
3. Do not invent missing project rules if a formal document already defines them.
4. Use `progress.txt` as the current execution memory and state source.

## 3. Project Snapshot

### 3.1 Product surfaces

Active product surfaces:

1. Patient Android app: `frontend/huanzhe`
2. Pharmacist Web: `frontend/pharmacist`
3. Doctor Web: `frontend/yisheng/web-admin`

### 3.2 Dual-agent role

Claude is the product/data/frontend/verification host. It defines correct answers, implements frontend, runs browser acceptance, and manages ops delivery. It does NOT serve as the primary high-frequency backend constructor — that role belongs to Codex.

Codex leads backend scaffolding, code quality review, and test coverage; Claude leads product definition, frontend implementation, and runtime verification.

### 3.3 Backend zones

1. Legacy backend: `backend`
2. New backend target: `backend-v2`
3. Backend directory normalization from `backendV2` to `backend-v2` is completed

### 3.4 Fixed route prefixes

1. Patient and family APIs: `/app-api/**`
2. Pharmacist and doctor APIs: `/admin-api/**`

### 3.5 Fixed local development ports

1. Patient app: `3001`
2. Doctor web: `3002`
3. Pharmacist web: `3003`

### 3.6 Response baseline

All new backend responses must use:

`CommonResult<T>`

### 3.7 WebSocket endpoints

1. Professional notifications: `/ws/admin-notifications` (ticket-authenticated)
2. Patient notifications: `/ws/app-notifications` (ticket-authenticated)

Both paths require a short-lived one-time socket ticket obtained from `/admin-api/notifications/socket-ticket` or `/app-api/notifications/socket-ticket` before opening the WebSocket connection. Bearer tokens must not appear in WebSocket URLs.

### 3.8 AI assistant

1. Backend endpoint: `POST /app-api/ai/chat`
2. Patient app route: `/ai-assistant`
3. AI provider configuration: `medicine.ai.base-url` / `medicine.ai.api-key` / `medicine.ai.model`
4. Current default provider: SiliconFlow `/v1/chat/completions`, model `zai-org/GLM-4.5-Air`
5. Local secrets: `backend-v2/.env.local` (gitignored); loaded by `backend-v2/scripts/run-local-with-env.ps1`

## 4. Allowed / Forbidden Actions

### 4.1 Always allowed

1. Read formal documents first
2. Inspect repository files before editing
3. Prefer small, reviewable diffs
4. Keep changes aligned with existing structure and formal docs
5. Run the fastest relevant validation first

### 4.2 Always forbidden unless explicitly approved

1. Do not modify legacy `backend`
2. Do not copy legacy backend code into `backend-v2`
3. Do not invent endpoints, DTOs, file paths, package roots, or dependency versions
4. Do not let page components build URLs directly
5. Do not add undefined colors, radii, breakpoints, or ad-hoc CSS systems
6. Do not treat `docs/` as source of truth
7. Do not silently rename public DTOs or route prefixes
8. Do not create new visual systems outside `FRONTEND_GUIDELINES.md`
9. Do not skip Flyway migration version numbers or modify already-applied migration files

### 4.3 Default implementation attitude

1. Reuse formal decisions
2. Prefer consistency over novelty
3. Prefer explicit contracts over inferred ones
4. Prefer phase-safe changes over broad refactors

## 5. Tech Stack Summary

This section is a summary only.
For exact versions, always read `Standard document/TECH_STACK.md`.

### 5.1 Frontend

1. Patient app: Ionic Vue 8 + Vue 3
2. Pharmacist web: Vue 3 + Element Plus 2.3.7
3. Doctor web: Vue 3 + Element Plus 2.11.2

### 5.2 Backend

1. New backend target: Java 17 + Spring Boot 3.2.12
2. Persistence: MyBatis Plus
3. Cache/session support: Redis
4. API docs: SpringDoc
5. Drug catalog pinyin search: pinyin4j 2.5.1

### 5.3 Legacy

1. Legacy backend remains Java 11 + Spring Boot 2.7.18
2. Legacy backend is frozen and is not the implementation target for new MVP work

## 6. Naming and Structure Rules

### 6.1 Backend rules

1. Backend package root: `com.medicine`
2. Required backend top-level packages:
   - `api`
   - `service`
   - `repository`
   - `domain`
   - `config`
   - `common`
3. New backend DTO naming must align with `Standard document/BACKEND_STRUCTURE.md`
4. Do not expose entities directly from controllers
5. Schema changes must use Flyway migrations under `backend-v2/src/main/resources/db/migration/` with convention `V{N}__{description}.sql`. Current latest applied version: V8.
6. Active sub-domain packages include: `drug` (drug catalog), `medication` (self-reported medication)

### 6.2 Frontend rules

Patient app new code goes under:

1. `src/app`
2. `src/modules`
3. `src/shared`

Pharmacist web new business code goes under:

1. `src/modules/pharmacist/*`
2. shared foundations under `src/app` and `src/shared`

Doctor web rules:

1. keep MVP routes only
2. keep MVP menu only
3. do not expand into full admin scope

### 6.3 Service boundary rules

1. New service modules own API calls
2. Page components must not hardcode URLs
3. Shared HTTP clients belong in the shared layer
4. DTO names must match formal backend contract names

## 7. Frontend Implementation Rules

This section is a summary only.
For detailed tokens and component rules, read `Standard document/FRONTEND_GUIDELINES.md`.

Required frontend rules:

1. Use token-based colors only
2. Patient theme must map through Ionic tokens
3. Professional theme must map through CSS variables and Element Plus theme variables
4. Patient navigation uses bottom-tab patterns for MVP
5. Professional surfaces use left navigation + top toolbar
6. Non-MVP menus must be hidden for trial roles
7. Do not introduce random page-level visual systems

## 8. Backend Implementation Rules

This section is a summary only.
For exact schema, DTO, and endpoint details, read `Standard document/BACKEND_STRUCTURE.md`.

Required backend rules:

1. Use `CommonResult<T>`
2. Keep `/app-api/**` and `/admin-api/**` strictly separated
3. App auth model:
   - OTP login
   - access token + refresh token
4. Professional auth model:
   - legacy user bridge
   - professional identity mapped from existing legacy user tables
5. All schema, table, DTO, and endpoint work must match `BACKEND_STRUCTURE.md`
6. Schema evolution uses Flyway. Never alter already-applied migrations (V1–V8).

Public names that must not be renamed without updating the formal contract:

1. `TokenPair`
2. `BindPatientRequest`
3. `MedicationPlanDTO`
4. `DoseTaskDTO`
5. `CheckinRecordDTO`
6. `PrescriptionDTO`
7. `HelpOrderDTO`
8. `SelfReportedMedicationDTO`
9. `SelfReportedMedicationCreateRequest`
10. `DrugCatalogDTO`
11. `AppDdiAlert`
12. `AppDdiScreenResult`
13. `AdminDdiRuleDTO`
14. `AdminDdiRuleUpsertRequest`
15. `NotificationLogDTO`
16. `AppPatientProfileDTO`
17. `AppPatientHealthProfileDTO`
18. `AdminPatientDetailDTO`
19. `KnowledgeAssessmentDTO`
20. `SatisfactionSurveyDTO`

## 9. Session Workflow and Output Contract

Every AI session should follow this default workflow:

1. Read `CLAUDE.md`
2. Read `progress.txt`
3. Read `Standard document/IMPLEMENTATION_PLAN.md`
4. Identify task-relevant formal documents
5. Inspect repository files before making assumptions
6. State target files and editing plan before making changes
7. Prefer small diffs
8. Run the fastest relevant validation first
9. Update `progress.txt` when starting, completing, switching branch, or changing active workstream
10. Summarize changed files and validation results at the end

Output requirements for implementation work:

1. Name the files being changed before editing
2. Keep explanations concise
3. Report what was validated
4. Report what could not be validated

Relationship between session files:

1. `CLAUDE.md` = Claude operating manual
2. `CODEX.md` = Codex operating manual
3. `progress.txt` = shared execution memory and state source

`CLAUDE.md` does not contain:

1. full PRD content
2. full design token tables
3. full backend schema tables
4. full implementation phase detail

Those details remain in the formal documents and must be referenced there instead of being reinvented here.
