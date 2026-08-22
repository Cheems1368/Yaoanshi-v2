# IMPLEMENTATION_PLAN.md

## 1. Document role

This document is the authoritative step-by-step implementation sequence for the MVP P0 release.

Rules:

1. This document defines execution order, not product scope or architecture rationale.
2. `Standard document/PRD.md`, `APP_FLOW.md`, `TECH_STACK.md`, `FRONTEND_GUIDELINES.md`, and `BACKEND_STRUCTURE.md` are mandatory inputs for every implementation phase.
3. The removed `docs/` tree must not be recreated as a knowledge base, reference store, or plan output location.
4. This document covers `P0` and `P0+` MVP phases, plus the Post-P0 trial readiness sprint.
5. `P2` and later capabilities must stay in backlog and must not enter the primary build sequence.

## 1.5 Execution Status Summary (as of 2026-08-22)

| Phase | Description | Status |
|-------|-------------|--------|
| Phase 0 | Repo and Environment Baseline | ✅ Complete |
| Phase 1 | backend-v2 Foundation | ✅ Complete |
| Phase 2 | Frontend Foundation | ✅ Complete |
| Phase 3 | MVP Core Flows | ✅ Complete |
| Phase 4 | Integration, QA, and Trial Readiness | ✅ API integration complete; browser UI walkthrough remaining |
| Phase 5 | DDI Enhancement (P0+) | ✅ Complete |
| Phase 6 | Self-Reported Medication Domain (P0+) | ✅ Complete |
| Phase 7 | Drug Catalog and Pinyin Initials Search (P0+) | ✅ Complete |
| Phase 8 | Post-P0 Trial Readiness Sprint (A–H) | ✅ Complete |
| Phase T0–T5 | Trial/demo closed loops through voice help orders | ✅ Delivered through PR #32 (`35b1e8a`) |
| Phase T6 | Android JDK 21 and enforced APK build gate | ✅ Delivered through PR #37 (`98115b9`) |
| Phase T7 | SEC-002 refresh-token HttpOnly Cookie closure | ✅ Delivered through PR #39 (`4c90292`) |
| Phase T8 | SEC-005 AI data-boundary closure | Local implementation and all available gates pass on `codex/sec-005-ai-data-boundary`; protected PR qualification pending |

**Current active work:** T8 closes SEC-005 with versioned provider disclosure, high-confidence text redaction, exact upstream-host allowlisting, fail-closed metadata-only audit, and patient-side policy loading. SEC-005 remains open until the T8 protected required gate is green and all review conversations are resolved. Android release signing and store publishing remain separate release-engineering work.

**Flyway current latest:** V13 (`V13__ai_data_boundary.sql`). Next new migration must be V14.

**Known open items:**
- `MEDICINE_OCR_API_KEY` must be set in `.env.local` for live OCR (graceful fallback if omitted)
- SEC-005 T8 candidate passes local backend, patient Web/Android, migration, and data-boundary gates; protected PR gate and conversation check remain before closure
- Ops cleanup: `ops/sql/sec-003-clear-legacy-patient-id-card-masked.sql` needs to be run

## 1.6 Post-P0 Trial Demo Closed-Loop Sequence

The post-P0 trial/demo work is phase-based, not tied to fixed calendar weeks. Each phase must preserve existing patient, pharmacist, and doctor flows while adding the smallest demonstrable closed loop.

| Phase | Goal | Required validation |
|-------|------|---------------------|
| Phase T0 | Standard document sync for trial/demo scope | `BACKEND_STRUCTURE.md`, `APP_FLOW.md`, and this file describe the same contracts and sequence |
| Phase T1 | Image help-order loop: patient text+image -> pharmacist mobile Web reply -> patient detail sees reply | backend targeted help-order tests, patient build, pharmacist build, manual mobile-width walkthrough |
| Phase T2 | Doctor mobile Web collaboration and structured `ADOPTED` / `NOT_ADOPTED` decision events | doctor/pharmacist help-order tests, doctor mobile build, and mobile-width walkthrough |
| Phase T3 | Medication reminder and family second-reminder demo path | backend reminder-stage tests, patient build, and medication/family E2E script |
| Phase T4 | OCR hardening, CSV rule import, reviewed education recommendation | ✅ backend targeted tests, pharmacist build, patient build |
| Phase T5 | Help-order voice message + XFYUN transcription | ✅ backend targeted tests, patient/pharmacist/doctor builds, Docker live voice walkthrough |
| Phase T6 | Android JDK 21 toolchain and APK gate | Temurin 21.0.12+8, clean Capacitor sync, local `assembleDebug`, CI APK artifact, aggregate quality gate |
| Phase T7 | SEC-002 refresh-token Cookie closure across patient, pharmacist, and doctor clients | backend auth tests and full verify, three frontend gates, Android build, nine E2E scenarios, physical-device restart, static token scan, protected CI |
| Phase T8 | SEC-005 AI data-boundary closure | V13 migration, versioned consent/disclosure, redaction and upstream allowlist tests, metadata-only audit failure paths, patient Web/Android gates, protected CI |

Rules:

1. T1 must land before T2-T5 implementation work.
2. New database migrations continue from the latest existing Flyway version; after `V13__ai_data_boundary.sql`, the next migration is `V14`.
3. Temporary execution plans stay in conversation; durable sequence changes belong here.
4. T8 is additive: the legacy AI timestamp and chat DTO remain compatible, no legacy-backend change is allowed, and only the AI feature is denied to clients without current versioned consent.

### Phase T7 acceptance boundary

1. Official clients use the additive `/session/login`, `/session/refresh`, and `/session/logout` endpoints and never receive or persist refresh-token material.
2. Access tokens remain memory-only; route guards may improve navigation but backend Bearer authorization remains authoritative.
3. Patient refresh Cookies persist for the refresh TTL. Pharmacist and doctor refresh Cookies are browser-session Cookies.
4. Refresh rotation and Cookie logout reject replaced tokens atomically; refresh and logout require `X-Auth-Refresh: 1`.
5. SEC-002 may be marked closed only after backend, frontend, Android, nine-scenario E2E, physical-device restart, static scan, and protected CI gates pass.

### Phase T8 acceptance boundary

1. Only consent rows matching the current policy version, provider code, and statement SHA-256 unlock AI; the legacy timestamp alone never grants T8 consent.
2. The patient screen loads disclosure on every entry, remains fail-closed while disclosure is unknown, posts the current version, and can reopen the data-use statement at any time.
3. At most five recent non-system messages are sent after high-confidence redaction; no account identity or health-profile fields are automatically appended.
4. The upstream host exactly matches the allowlist, uses HTTPS except for an explicitly allowlisted loopback host, and never follows redirects.
5. A metadata-only `PENDING` audit is inserted before network access. Insert failure blocks the provider call; completion-update failure leaves `PENDING` without logging content.
6. V13 is additive, audit expiry is 180 days, and the cleanup SQL provides preview plus bounded deletion without adding a scheduler.
7. SEC-005 may be marked closed only after targeted and full backend gates, patient lint/unit/build/Android gates, migration checks, protected CI, and zero unresolved review conversations pass.

## 2. Implementation Rules

### 2.1 Global rules

1. Do not modify the legacy `backend` as part of MVP implementation.
2. The new backend target is `backend-v2`; the current `backendV2` directory must be treated as a naming normalization issue, not as a legacy code source.
3. Patient and family clients only integrate with `/app-api/**`.
4. Pharmacist and doctor Web clients only integrate with `/admin-api/**`.
5. Local development ports are fixed:
   - patient app: `3001`
   - doctor web: `3002`
   - pharmacist web: `3003`
6. All new backend work must follow `TECH_STACK.md`.
7. All new frontend work must follow `FRONTEND_GUIDELINES.md`.
8. All new backend contracts, tables, DTOs, and endpoint semantics must follow `BACKEND_STRUCTURE.md`.

### 2.2 Execution policy

1. Build order is fixed:
   - backend foundation first
   - frontend shell and shared layer second
   - core business flows third
   - integration and trial hardening last
2. Every phase must have:
   - Inputs
   - Steps
   - Deliverables
   - Validation commands
   - Done when
   - Blockers / Dependencies
3. No phase is considered complete without passing its validation gate.

## 3. Phase 0: Repo and Environment Baseline

### Inputs

- `TECH_STACK.md`
- current workspace layout under `D:\yaoanshi`

### Steps

#### 0.1 Validate local toolchain against `TECH_STACK.md`

Action:

1. Check Node.js, npm, pnpm, Java, Maven, Docker, Docker Compose versions.
2. Confirm Java 11 remains available for legacy backend reference only.
3. Confirm Java 17 is available for `backend-v2`.

Commands:

```powershell
node -v
npm -v
pnpm -v
java -version
mvn -v
docker -v
docker compose version
```

#### 0.2 Normalize repo baseline

Action:

1. Confirm `backend` remains frozen.
2. Confirm `frontend/huanzhe`, `frontend/pharmacist`, and `frontend/yisheng/web-admin` remain the three active client surfaces.
3. Confirm `backendV2` is not treated as final naming and will be normalized to `backend-v2` before implementation starts.

Commands:

```powershell
Get-ChildItem D:\yaoanshi
Get-ChildItem D:\yaoanshi\frontend
```

#### 0.3 Validate frontend port and proxy baseline

Action:

1. Confirm patient app targets port `3001`.
2. Confirm doctor web targets port `3002`.
3. Confirm pharmacist web targets port `3003`.
4. Confirm patient app API prefix is `/app-api/**`.
5. Confirm pharmacist and doctor web API prefix is `/admin-api/**`.

Commands:

```powershell
Get-Content D:\yaoanshi\frontend\huanzhe\vite.config.ts
Get-Content D:\yaoanshi\frontend\pharmacist\vite.config.ts
Get-Content D:\yaoanshi\frontend\yisheng\web-admin\vite.config.js
```

#### 0.4 Create environment and run-command checklist

Action:

1. Enumerate required `.env` files for all three frontends.
2. Enumerate required backend-v2 environment variables:
   - datasource
   - redis
   - jwt
   - object storage
   - springdoc
3. Record all standard local run commands for use in later phases.

### Deliverables

1. Verified toolchain baseline
2. Verified repo baseline
3. Verified port and proxy baseline
4. Standard run-command checklist

### Validation commands

```powershell
node -v
java -version
mvn -v
```

### Done when

1. All required tools are available.
2. Legacy and new implementation boundaries are clear.
3. Port and API prefix rules are confirmed.
4. Required environment surfaces are enumerated.

### Blockers / Dependencies

1. Java 17 missing
2. Maven missing
3. frontend port config drift
4. missing `.env` baseline for one of the clients

## 4. Phase 1: backend-v2 Foundation

### Inputs

- `TECH_STACK.md`
- `BACKEND_STRUCTURE.md`
- `PRD.md`

### Steps

#### 1.1 Initialize `backend-v2` Maven project

Action:

1. Create normalized root directory `backend-v2`.
2. Create Maven `pom.xml`.
3. Set Java target to `17.0.12` baseline.
4. Keep packaging as single-service modular monolith.

#### 1.2 Install and lock backend dependencies from `TECH_STACK.md`

Action:

1. Add Spring Boot `3.2.12`.
2. Add Spring Web.
3. Add Spring Security.
4. Add Spring Data Redis.
5. Add MyBatis-Plus Spring Boot 3 starter `3.5.7`.
6. Add SpringDoc WebMVC UI `2.5.0`.

Validation command:

```powershell
mvn -q -DskipTests compile -f D:\yaoanshi\backend-v2\pom.xml
```

#### 1.3 Create package and resource structure

Action:

1. Create package root `com.medicine`.
2. Create:
   - `com.medicine.api`
   - `com.medicine.service`
   - `com.medicine.repository`
   - `com.medicine.domain`
   - `com.medicine.config`
   - `com.medicine.common`
3. Create resource structure:
   - `src/main/resources/application.yml`
   - `src/main/resources/mapper`

#### 1.4 Create common response, exception, and health endpoint

Action:

1. Implement `CommonResult<T>`.
2. Implement global exception model and handler.
3. Add `/health`.
4. Confirm service can boot without business modules.

Validation commands:

```powershell
mvn -q -DskipTests compile -f D:\yaoanshi\backend-v2\pom.xml
mvn -f D:\yaoanshi\backend-v2\pom.xml spring-boot:run
curl http://localhost:8080/health
```

#### 1.5 Create JWT security baseline and route segmentation

Action:

1. Create JWT generation and validation skeleton.
2. Separate `/app-api/**` and `/admin-api/**` security matcher baselines.
3. Support app-side token model from `BACKEND_STRUCTURE.md`.
4. Support professional JWT acceptance bridge for legacy-authenticated users.

#### 1.6 Create infrastructure integration interfaces

Action:

1. Create Redis config.
2. Create OpenAPI config.
3. Create object storage abstraction for S3-compatible provider.
4. Add placeholder configuration for datasource and storage.

#### 1.7 Create initial schema and DTO skeletons from `BACKEND_STRUCTURE.md`

Action:

1. Create empty domain/entity/mapper/DTO skeletons for:
   - identity
   - prescription
   - medication
   - help order
   - notification
   - assessment
   - ddi
2. Do not migrate legacy business code.
3. Use document-defined names only.

### Deliverables

1. Runnable `backend-v2` service skeleton
2. Locked dependency baseline
3. Common response and exception layer
4. Health endpoint
5. Security and infrastructure skeleton
6. Initial schema/DTO code skeleton matching `BACKEND_STRUCTURE.md`

### Validation commands

```powershell
mvn -q -DskipTests compile -f D:\yaoanshi\backend-v2\pom.xml
mvn -f D:\yaoanshi\backend-v2\pom.xml spring-boot:run
curl http://localhost:8080/health
```

### Done when

1. `backend-v2` starts independently.
2. `/health` returns `CommonResult`.
3. Required dependencies are pinned.
4. Package structure exists.
5. Security baseline separates app/admin route spaces.

### Blockers / Dependencies

1. Java 17 unavailable
2. Maven dependency conflicts
3. datasource or redis placeholders missing
4. unresolved package naming drift

## 5. Phase 2: Frontend Foundation

### Inputs

- `FRONTEND_GUIDELINES.md`
- `APP_FLOW.md`
- `TECH_STACK.md`
- current frontend directories

### Steps

#### 2.1 Patient app: tokens, theme mapping, and shell

Action:

1. Map patient colors into Ionic tokens.
2. Establish patient typography, spacing, and radius tokens from `FRONTEND_GUIDELINES.md`.
3. Build patient shell:
   - page header
   - bottom tab
   - primary content area
4. Keep mobile-first layout.

Validation commands:

```powershell
cd D:\yaoanshi\frontend\huanzhe
npm install
npm run dev
```

#### 2.2 Patient app: `app/modules/shared`, routing, and HTTP baseline

Action:

1. Normalize `src/app`, `src/modules`, `src/shared`.
2. Establish route guard for:
   - login required
   - binding required
   - active medication plan required
3. Create shared HTTP client.
4. Create unified API error handling.
5. Keep old paths as compatibility wrappers where needed.

#### 2.3 Pharmacist web: professional theme variables and dashboard shell

Action:

1. Apply professional token mapping to Element Plus theme variables.
2. Build left navigation, top toolbar, and content shell.
3. Hide non-MVP menu groups for trial roles.
4. Preserve medium-density dashboard layout.

Validation commands:

```powershell
cd D:\yaoanshi\frontend\pharmacist
npm install
npm run dev
```

#### 2.4 Doctor web: reuse professional shell and reduce to MVP menu

Action:

1. Reuse professional theme rules.
2. Keep only doctor MVP routes and menus.
3. Surface escalated work-order queue and doctor profile entry only.

Validation commands:

```powershell
cd D:\yaoanshi\frontend\yisheng\web-admin
npm install
npm run dev
```

#### 2.5 All clients: establish service-layer API boundary

Action:

1. Create service modules around stable API prefixes.
2. Prevent page components from building URLs directly.
3. Keep DTO names aligned with `BACKEND_STRUCTURE.md`.

Required API group references:

- `/app-api/auth/*`
- `/app-api/patients/*`
- `/app-api/medication-plans/*`
- `/app-api/dose-tasks/*`
- `/app-api/checkins/*`
- `/app-api/help-orders/*`
- `/app-api/prescriptions/uploads`
- `/admin-api/patients/*`
- `/admin-api/prescriptions/*`
- `/admin-api/medication-plans/*`
- `/admin-api/help-orders/*`
- `/admin-api/ddi/*`
- `/admin-api/notifications/*`

### Deliverables

1. Patient app shell and shared layer
2. Pharmacist web professional shell
3. Doctor web professional shell
4. Unified client-side API boundary

### Validation commands

```powershell
cd D:\yaoanshi\frontend\huanzhe; npm run build
cd D:\yaoanshi\frontend\pharmacist; npm run build
cd D:\yaoanshi\frontend\yisheng\web-admin; npm run build
```

### Done when

1. Three clients can start independently on `3001/3002/3003`.
2. Three clients use document-defined design tokens.
3. Routing and API boundaries are standardized.
4. Non-MVP menus are hidden from pharmacist/doctor trial roles.

### Blockers / Dependencies

1. missing package install
2. broken Vite config
3. unresolved route drift
4. old page code bypassing service layer

## 6. Phase 3: MVP Core Flows

### Inputs

- `PRD.md`
- `APP_FLOW.md`
- `BACKEND_STRUCTURE.md`
- completed Phase 1 and Phase 2 outputs

### Steps

#### 3.1 App login, OTP, binding, and session refresh

Action:

1. Implement `OtpSendRequest`, `AppLoginRequest`, `RefreshTokenRequest`, `BindPatientRequest`.
2. Connect patient login flow to `/app-api/auth/*`.
3. Connect patient binding flow to `/app-api/patients/*`.
4. Enforce refresh-token rotation and device session behavior.

#### 3.2 Pharmacist patient profile, data completion, and ownership

Action:

1. Implement pharmacist patient list and detail.
2. Implement patient supplemental medical information update.
3. Implement fixed responsible pharmacist logic.
4. Trigger ABCD2 recalculation when required fields change.

#### 3.3 Prescription upload, OCR, pharmacist confirmation, and DDI warning

Action:

1. Implement patient-side prescription image upload.
2. Implement `prescription`, `prescription_item`, `prescription_attachment`, `prescription_ocr_record`.
3. Implement pharmacist OCR review screen.
4. Implement DDI rule screening and forced pharmacist confirmation.

#### 3.4 Medication plan, dose-task generation, reminders, check-in, and correction

Action:

1. Implement `medication_plan` and `medication_plan_item`.
2. Implement `medication_dose_task` generation.
3. Implement patient daily reminder task retrieval.
4. Implement patient/family check-in.
5. Implement 24-hour correction and effective-record switching.

#### 3.5 Family binding, delegated check-in, delegated help-order submit, and unbind

Action:

1. Implement `patient_family_binding`.
2. Allow delegated check-in and help-order submit.
3. Ensure unbind revokes permissions immediately.
4. Keep history visible in audit.

#### 3.6 Help-order collaboration flow

Action:

1. Implement patient/family work-order submit.
2. Implement pharmacist queue and handling flow.
3. Implement escalation to doctor.
4. Implement doctor write-back.
5. Implement reject and close rules tied to current handler.
6. Implement notification and event timeline.

#### 3.7 Knowledge assessment, satisfaction, ABCD2, and adherence report

Action:

1. Implement knowledge assessment submission and answer storage.
2. Implement post-close satisfaction survey.
3. Implement ABCD2 raw-input + snapshot score storage.
4. Implement 7-day and 30-day adherence report and export.

### Deliverables

1. App auth and binding flow
2. Pharmacist patient management
3. OCR-to-plan pipeline
4. Medication execution and correction loop
5. Family delegated care loop
6. Work-order collaboration loop
7. Measurable reporting loop

### Validation commands

```powershell
mvn -q -DskipTests compile -f D:\yaoanshi\backend-v2\pom.xml
cd D:\yaoanshi\frontend\huanzhe; npm run build
cd D:\yaoanshi\frontend\pharmacist; npm run build
cd D:\yaoanshi\frontend\yisheng\web-admin; npm run build
```

### Done when

1. The patient can log in, bind, and enter the main flow.
2. The pharmacist can maintain plans and handle work orders.
3. The doctor can process escalated work orders only.
4. The patient and family can complete delegated care actions.
5. Metrics and export paths are available.

### Blockers / Dependencies

1. missing backend skeleton from Phase 1
2. unstable frontend service layer from Phase 2
3. OCR provider unavailable
4. object storage unavailable
5. Redis unavailable

## 7. Phase 4: Integration, QA, and Trial Readiness ✅ (API complete; browser walkthrough remaining)

### Inputs

- completed P0 implementation
- all formal documents in `Standard document`

### Steps

#### 4.1 Parallel client startup and baseline smoke test

Action:

1. Start patient app, pharmacist web, and doctor web at the same time.
2. Verify no port collision.
3. Verify shared backend-v2 integration entry points.

#### 4.2 Patient critical-path integration

Critical path:

1. login
2. bind
3. home
4. reminder
5. check-in
6. correction

#### 4.3 Pharmacist and doctor collaboration integration

Critical path:

1. prescription upload
2. OCR review
3. plan generation
4. work-order submit
5. pharmacist handle
6. doctor escalation
7. doctor write-back
8. close

#### 4.4 Export, notification, WebSocket, and failure compensation

Action:

1. Verify adherence export.
2. Verify knowledge, satisfaction, and ABCD2 persistence.
3. Verify WebSocket and in-app notification behavior.
4. Verify notification-log failure handling.
5. Verify object-storage compensation behavior.

#### 4.5 Trial release package preparation

Action:

1. Prepare interface checklist.
2. Prepare test report.
3. Prepare known-issues list.
4. Prepare rollback notes.

**Completed (2026-03-27):**
- backend-v2: `mvn compile` ✅, `mvn test` 97/97 ✅ (4 broken test files fixed in `4fc16db`)
- frontend/huanzhe: `npm run build` ✅
- frontend/pharmacist: `npm run build` ✅
- frontend/yisheng/web-admin: `npm run build` ✅
- Docker stack: MySQL, Redis, MinIO, Nginx all running (infrastructure fixes committed in `fdb3233`)
- API integration: all patient and admin endpoints verified (send-otp, login, auth/me, bind-patient, medication-plans, dose-tasks, ai/consent, self-reported-medications, prescriptions, ddi/screen, assessments, help-orders, drugs/search)
- AI consent bug fix (commit `fdb3233`): `AiChatService` was parsing JWT sub as Long for account ID; fixed to resolve account by mobile via `selectActiveByMobile` first

**Remaining:** Browser-level UI testing — manual walk-through of all 3 frontends.

### Deliverables

1. End-to-end validated MVP trial build
2. Integration checklist
3. Test report
4. Known issues list
5. Rollback notes

### Validation commands

```powershell
cd D:\yaoanshi\frontend\huanzhe; npm run build
cd D:\yaoanshi\frontend\pharmacist; npm run build
cd D:\yaoanshi\frontend\yisheng\web-admin; npm run build
mvn -q -DskipTests compile -f D:\yaoanshi\backend-v2\pom.xml
```

### Done when

1. Three clients run in parallel.
2. Patient critical path works end to end.
3. Pharmacist-doctor collaboration path works end to end.
4. Notification and export capabilities are verified.
5. Trial release package is complete.

### Blockers / Dependencies

1. unresolved API contract drift
2. unstable notification channel
3. missing OCR integration
4. missing export verification

## 8. Phase 5: DDI Enhancement (P0+ 用药助手增强)

### Goal

Extend the existing DDI screening from pharmacist-only prescription confirmation to patient-side real-time drug interaction alerts, drug-condition contraindication checking, and pharmacist DDI rule management.

First-period scope: stroke-related drugs only (aspirin, clopidogrel, warfarin, statins, antihypertensives, antidiabetics, PPI).

### Step 5.1: Stroke DDI rule library expansion

Action:

1. Define stroke core drug interaction rules (HIGH + MEDIUM severity).
2. Generate SQL INSERT scripts for `ddi_rule` table with 30-50 rules covering:
   - Antiplatelet combinations (aspirin + clopidogrel + ticagrelor)
   - Antiplatelet + anticoagulant (aspirin + warfarin, clopidogrel + rivaroxaban)
   - Clopidogrel + PPI (omeprazole, esomeprazole — MEDIUM)
   - Statin interactions (atorvastatin + CYP3A4 inhibitors)
   - Antihypertensive combinations (ACEI + potassium-sparing diuretics)
   - NSAID + antiplatelet/anticoagulant bleeding risk
3. Define initial `drug_contraindication` rules for stroke drugs:
   - Anticoagulants + active bleeding history
   - Metformin + severe renal impairment
   - Beta-blockers + severe bradycardia
4. Generate SQL INSERT scripts for `drug_contraindication` table.

AI collaboration:

- Confirm rule content (drug pairs, severity, message text) against `BACKEND_STRUCTURE.md` 4.9 before generation.
- Generate SQL scripts and unit tests only after the rule content is clear.

### Step 5.2: Patient-side DDI screening backend

Action:

1. Create `AppDdiScreeningService` with `screenPatientMedications(patientId, newDrugNames)` method.
2. Implement `POST /app-api/ddi/screen` endpoint per `BACKEND_STRUCTURE.md` 6.1 contract.
3. Screening logic:
   - Fetch patient's ACTIVE medication plan items as existing drug set
   - Match new drugs against existing drugs using `ddi_rule` (HIGH + MEDIUM)
   - Fetch patient health profile from `patient_health_profile`
   - Match new drugs against health conditions using `drug_contraindication`
   - Return combined sorted alert list
4. Create `drug_contraindication` table DDL in local schema bootstrap.
5. Add unit tests for screening service.

AI collaboration:

- Confirm API constraints and screening logic against `BACKEND_STRUCTURE.md`.
- Generate service, controller, DTO, and tests after the contract is clear.
- Review product alignment against `PRD.md` and `APP_FLOW.md`.

### Step 5.3: Patient-side DDI alert frontend

Action:

1. Add DDI screening call before prescription upload in patient app.
2. Implement alert dialog component:
   - List of alerts with severity badges (red=HIGH, orange=MEDIUM)
   - Each alert shows drug pair/condition, message, recommended action
   - Two buttons: "已知晓，继续提交" / "取消，咨询药师"
3. "取消" option can navigate to help order creation with pre-filled context.
4. Graceful degradation: if DDI screening API fails, proceed without blocking.

AI collaboration:

- Implement the frontend flow and run browser acceptance.
- Add quality review and test coverage appropriate to the behavior changed.

### Step 5.4: Pharmacist DDI rule management

Action:

1. Implement `GET /admin-api/ddi/rules` (paged, filterable by drug name and severity).
2. Implement `PUT /admin-api/ddi/rules/{id}` for editing existing rules.
3. Implement CRUD for `drug_contraindication` via `/admin-api/ddi/contraindications`.
4. Create pharmacist web rule management page at `/ddi/rules`:
   - Searchable table with drug name and severity filters
   - Create/edit form in drawer or dialog
   - Separate tabs for DDI rules and contraindication rules

AI collaboration:

- Confirm constraints before implementation.
- Implement backend CRUD, frontend UI, and tests within the documented contracts.
- Run browser acceptance for the management page.

### Validation commands

```powershell
mvn -q -DskipTests compile -f D:\yaoanshi\backend-v2\pom.xml
mvn test -f D:\yaoanshi\backend-v2\pom.xml
cd D:\yaoanshi\frontend\huanzhe; npm run build
cd D:\yaoanshi\frontend\pharmacist; npm run build
```

### Done when

1. Stroke DDI rule library contains at least 20 HIGH + MEDIUM rules.
2. Patient app triggers DDI screening before prescription upload.
3. DDI alerts display correctly with severity differentiation.
4. Pharmacist can manage DDI rules via web interface.
5. Drug contraindication rules work against patient health profile.
6. All unit tests pass, all frontend builds pass.

### Blockers / Dependencies

1. `patient_health_profile` table must be populated (already implemented in SEC-003 closure)
2. Stroke drug rule content requires pharmacist domain review for medical accuracy

---

## 9. Phase 6: Self-Reported Medication Domain (P0+ 自报用药)

### Goal

Allow patients to record medications taken outside the formal plan, with DDI screening on entry, automatic pharmacist review work order creation, and pharmacist handling flows (acknowledge / reject / transfer-to-plan). Extend DDI rule richness with patient-friendly summaries and clinical detail fields.

**Status: Code-complete as of 2026-03-19. This phase documents the implementation after the fact.**

### Step 6.1: Backend — self-reported medication table + state machine + services

Action:

1. V6 Flyway migration:
   - Create `self_reported_medication` table (see `BACKEND_STRUCTURE.md` 4.5)
   - Extend `ddi_rule` with 4 fields: `patient_summary`, `mechanism_desc`, `alternative_recommendation`, `reference_source`
   - Extend `drug_contraindication` with `patient_summary`
   - Extend `ddi_screening_log` with `self_reported_medication_id`
2. Implement `AppSelfReportedMedicationService`:
   - submit (create record + auto-create NEW_MEDICATION_REVIEW work order)
   - list (paged, filterable by status)
   - withdraw (patient stops PENDING record)
3. Implement `AdminSelfReportedMedicationService`:
   - acknowledge (PENDING → ACTIVE)
   - reject (PENDING/ACTIVE → STOPPED)
   - transfer-prefill (read-only prefill data)
   - mark-transferred (ACTIVE → TRANSFERRED)

Reference: `BACKEND_STRUCTURE.md` 4.5 (`self_reported_medication` table), 4.9 (DDI extensions)

### Step 6.2: Backend — patient and admin endpoints

Action:

1. Patient endpoints (via `AppSelfReportedMedicationController`):
   - `POST /app-api/self-reported-medications`
   - `GET /app-api/self-reported-medications`
   - `POST /app-api/self-reported-medications/{id}/withdraw`
   - `POST /app-api/self-reported-medications/{id}/stop`
2. Admin endpoints in `AdminHelpOrderController`:
   - `GET /admin-api/help-orders/self-reported-medications`
   - `POST /admin-api/help-orders/{id}/acknowledge`
   - `POST /admin-api/help-orders/{id}/reject-medication`
   - `GET /admin-api/help-orders/{id}/transfer-prefill`
   - `POST /admin-api/help-orders/{id}/mark-transferred`
3. Admin endpoint in `AdminMedicationPlanController`:
   - `GET /admin-api/medication-plans/{id}/ddi-check`
4. Update `AppDdiScreeningService`:
   - Include ACTIVE self-reported medications as background drugs in screening

Reference: `BACKEND_STRUCTURE.md` 6.1 (self-reported endpoints), 6.2 (admin help-order and plan endpoints)

### Step 6.3: Frontend — patient SelfReportedMedicationPage

Action:

1. Create `SelfReportedMedicationPage.vue` at `/self-reported-medications`
2. Entry form: drug name (required), dosage, notes
3. Before submit: call `POST /app-api/ddi/screen` with drug name; show HIGH/MEDIUM alert dialog if hits
4. Submit via `POST /app-api/self-reported-medications`
5. List view: display all records with status badge
6. PENDING records: show "撤回" button → `POST /withdraw`
7. Add `selfReportedMedicationService` to `mvp.ts` with typed DTOs
8. Add `/self-reported-medications` route and "记录新药品" quick entry on Home page (6th quick action)

Reference: `APP_FLOW.md` 4.3 (Self-Reported Medication Screen), 7.4.2 (DDI alert dialog)

### Step 6.4: Frontend — pharmacist SelfReportedMedicationsPanel + work order integration

Action:

1. Create `SelfReportedMedicationsPanel.vue` component:
   - Show drug details (name, dosage, notes, submission time)
   - Show DDI alert summary if present
   - Action buttons: 确认接收 / 拒绝（附原因）/ 转入计划
2. Update `OrderDetail.vue`: embed panel when `category = NEW_MEDICATION_REVIEW`
3. Update `useWorkOrders.js`: add `NEW_MEDICATION_REVIEW` category display text
4. Update `workorders.js` API: add 5 self-reported medication admin endpoints
5. Update `MedicationPlanPanel.vue`: call `GET /admin-api/medication-plans/{id}/ddi-check` before activation; show confirmation dialog if alerts exist
6. Update `medicationPlans.js` API: add `getMedicationPlanDdiCheck` method
7. Update `DdiRules.vue` drawer form: add 4 new fields (patientSummary / mechanismDesc / alternativeRecommendation / referenceSource)

Reference: `APP_FLOW.md` 7.5 (NEW_MEDICATION_REVIEW handling flow)

### Validation commands

```powershell
mvn -q "-Dtest=AdminDdiServiceTest,AppDdiScreeningServiceTest" test -f D:\yaoanshi\backend-v2\pom.xml
mvn -q test -f D:\yaoanshi\backend-v2\pom.xml
cd D:\yaoanshi\frontend\huanzhe; npm run build
cd D:\yaoanshi\frontend\pharmacist; npm run build
cd D:\yaoanshi\frontend\yisheng\web-admin; npm run build
```

### Done when

1. V6 Flyway migration runs cleanly on a fresh schema.
2. Patient can submit self-reported medication with DDI screening and DDI alert dialog.
3. NEW_MEDICATION_REVIEW work order is auto-created and pushed to pharmacist.
4. Pharmacist can acknowledge / reject / transfer self-reported medications via work order detail.
5. Plan activation DDI check includes self-reported drugs.
6. All unit tests pass, all three frontend builds pass.

### Blockers / Dependencies

1. `AppDdiScreeningService` must be updated to include ACTIVE self-reported medications before Step 6.2 can be verified end to end.

---

## 10. Phase 7: Drug Catalog and Pinyin Initials Search (P0+ 药品目录拼音检索)

### Inputs

- `BACKEND_STRUCTURE.md` section 4.10 (`drug_catalog` table)
- `BACKEND_STRUCTURE.md` section 6.1 (`/app-api/drugs/search`)
- `BACKEND_STRUCTURE.md` section 6.2 (`/admin-api/drugs/search`)
- completed Phase 6 outputs (self-reported medication domain)

### Background

Drug name entry currently relies on free-text inputs with no autocomplete. This phase introduces a shared drug catalog that supports:

1. Chinese name fuzzy match (e.g., `阿司` → 阿司匹林)
2. Pinyin initials prefix match (e.g., `aspl` → 阿司匹林)

Pinyin initials are pre-computed at insert time using `PinyinUtils` (backed by `pinyin4j 2.5.1`) and stored in `drug_catalog.pinyin_initials`. The catalog is a search-aid only; free-text drug names remain valid everywhere.

### Steps

#### 7.1 Backend: drug catalog table and seed

Action:

1. Create `drug_catalog` table via `V7__drug_catalog.sql` per `BACKEND_STRUCTURE.md` 4.10.
2. Pre-seed 41 common cardiovascular and clinical drugs with pre-computed `pinyin_initials`.
3. Add `PinyinUtils` under `com.medicine.common.util` using `pinyin4j 2.5.1` for future new-entry indexing.

#### 7.2 Backend: search endpoints

Action:

1. Implement `GET /app-api/drugs/search?q=&limit=` → `CommonResult<List<DrugCatalogDTO>>`.
2. Implement `GET /admin-api/drugs/search?q=&limit=` → same shape.
3. Search matches `name_cn LIKE CONCAT(q, '%')` OR `pinyin_initials LIKE CONCAT(q, '%')` OR `name_cn LIKE CONCAT('%', q, '%')`, ordered by priority.
4. Results capped at 20 entries per request.

#### 7.3 Frontend — patient app

Action:

1. Add `drugCatalogService.search(q, limit)` to `src/modules/patient/api/mvp.ts`.
2. Update `SelfReportedMedicationPage.vue` drug name input with 280ms debounce autocomplete:
   - Show suggestion list with drug name + category label.
   - Tap to fill input, hide list on blur (150ms delay to allow click to register).

#### 7.4 Frontend — pharmacist web

Action:

1. Create `src/modules/pharmacist/api/drugs.js` with `searchDrugs(q, limit)`.
2. Update `DdiRules.vue`: replace `el-input` with `el-autocomplete` for **drug A**, **drug B**, and **drug name** (contraindication form) fields, using `fetch-suggestions` callback to `searchDrugs`.

### Deliverables

1. `V7__drug_catalog.sql` with 41-drug seed
2. `DrugCatalogMapper`, `DrugCatalogService`, `AppDrugCatalogController`, `AdminDrugCatalogController`
3. `PinyinUtils` utility
4. `drugCatalogService` in patient app + autocomplete UI in `SelfReportedMedicationPage.vue`
5. `drugs.js` in pharmacist web + `el-autocomplete` on all three DDI rule drug name inputs

### Validation commands

```powershell
mvn -q -DskipTests compile -f D:\yaoanshi\backend-v2\pom.xml
cd D:\yaoanshi\frontend\huanzhe; npm run build
cd D:\yaoanshi\frontend\pharmacist; npm run build
```

### Done when

1. Backend compiles cleanly with pinyin4j on classpath.
2. `GET /app-api/drugs/search?q=aspl` returns 阿司匹林 in `data`.
3. `GET /admin-api/drugs/search?q=hfl` returns 华法林 in `data`.
4. Both frontend builds pass.
5. Patient app drug name input shows suggestions on typing `aspl`.
6. Pharmacist DDI rule drawer drug name fields show autocomplete suggestions.

### Blockers / Dependencies

1. V7 migration requires V6 migration applied first.
2. `pinyin4j 2.5.1` must be resolvable from Maven Central or local cache.

---

---

## 11. Phase 8: Post-P0 Trial Readiness Sprint ✅ Complete (2026-03-27)

### Goal

Harden the platform for trial/demo within ~1 month. Eight sub-phases executed after the P0/P0+ code freeze.

### Phase A — Commit Baseline ✅

Committed all Phase 6+7 backend + frontend + standard doc changes (57 files) to `main`. Commit: `f9274e5`.

### Phase B — MinIO S3 Object Storage Integration ✅ (commit `e6178c9`)

- `ops/docker-compose.yml`: MinIO service (9000/9001) + backend-v2 storage env vars
- `pom.xml`: `software.amazon.awssdk:s3:2.25.60`
- `S3CompatibleObjectStorageImpl`: real AWS SDK v2 impl, `@Profile(!test)`, PostConstruct bucket init
- `S3CompatibleObjectStorageStub`: demoted to `@Profile(test)`
- `ObjectStorageProperties`: region field added
- `application.yml`: `medicine.storage` config block
- `run-local-with-env.ps1` + `.env.local.example`: `MEDICINE_STORAGE_*` vars documented

### Phase C — DeepSeek-OCR Integration ✅ (commit `201525c`)

- `OcrProperties`: `@ConfigurationProperties` for `medicine.ocr.*`
- `OcrService` interface + `OcrResult` (success/extractedText/confidence/responseJson)
- `DeepSeekOcrService`: SiliconFlow vision API (`deepseek-ai/deepseek-vl2`), graceful fallback if key missing
- `AppPrescriptionService`: replaced `MANUAL_REVIEW_REQUIRED` stub; `DEEPSEEK_OCR` on success, `MANUAL_REVIEW_REQUIRED + manualFallbackUsed=true` on failure
- `application.yml`: `medicine.ocr.*` config block

### Phase D — SEC-005 AI Consent Gate ✅ (commit `5f69a4d`)

- `V8__ai_consent.sql`: `ai_consent_granted_at DATETIME NULL` added to `app_account`
- `AppAccount` domain + `AppAccountMapper`: `selectAiConsentGrantedAt` + `updateAiConsent`
- `ErrorCode`: `AI_CONSENT_REQUIRED (40031)`
- `AiChatService`: consent check before upstream call; `checkConsentStatus` / `grantConsent`; bug-fix: resolve account by mobile via `selectActiveByMobile` (not JWT sub parsed as Long)
- `AiChatController`: `GET/POST /app-api/ai/consent`
- Patient app: `aiConsentService` in `mvp.ts`; `IonAlert` consent dialog on `AiAssistantPage` first entry

### Phase E — ARC-001/003 Map → Explicit DTO Migration ✅ (commit `036a39f`)

New DTOs: `AppPatientSummaryDTO`, `AppCurrentAccountDTO`, `AppFamilyBindingDTO`, `AppLogoutRequest`, `TransferPrefillDTO`.
Controllers updated: `AppAuthController`, `AppPatientController`, `AdminHelpOrderController`.
Response field names preserved; backward-compatible.

### Phase F — CI/CD GitHub Actions ✅ (commit `f285037`)

`.github/workflows/build.yml`: 4 parallel jobs (backend compile+test, 3 frontend builds). Triggers on push/PR to `main`. Maven + npm cache enabled.

### Phase G — Patient App UI/UX Polish ✅ (commit `c5feb36`)

- `variables.css`: 12 semantic `--patient-*` color tokens added
- `HomePage`, `MedicationReminderPage`, `HelpOrderPage`, `SelfReportedMedicationPage`: hardcoded hex → CSS tokens + `IonRefresher` pull-to-refresh added
- `ProfilePage`: avatar path fixed (`/src/picture/` → `@/shared/assets/images/` via Vite import)

### Phase H — Pharmacist/Doctor Minor Fixes ✅

- `DdiRules.vue`: `submitRuleForm` + `submitContraindicationForm` — added catch block with `ElMessage.error` on API failure
- `PharmacistDashboard.vue`: WS-triggered `loadDashboard()` debounced 300ms
- `Layout.vue` (doctor): removed "Phase 2" subtitle; `.workspace overflow-x: hidden`
- `index.scss` (doctor): `body overflow-x: hidden`

### Infrastructure Notes (post-Phase 8)

- Docker stack: MySQL `13306`, Redis `16379`, MinIO `9000/9001`, legacy server `48080`
- Runtime fixes applied during integration testing (committed in `fdb3233`):
  - `ops/docker-security-audit/redis/redis.conf`: `protected-mode no` (was blocking cross-container Redis connection)
  - `ops/docker-security-audit/nginx-dev.conf`: upstream → `backend-v2` (was referencing wrong service name)
  - `ops/sql/mysql/ruoyi-vue-pro.sql` imported: `system_users` + `system_role` + `system_user_role` tables

---

## 12. Backlog After P0+

The following items are intentionally excluded from the primary execution sequence:

1. Science education content library
2. Content operation capability
3. More disease-path expansion (DDI rules for hypertension, diabetes, etc.)
4. Non-MVP capability expansion
5. Any work that reopens legacy backend refactor
6. Ops cleanup: `ops/sql/sec-003-clear-legacy-patient-id-card-masked.sql` (historical `id_card_masked` rows)
7. S3 presigned URL upload end-to-end browser test (Phase B, pending manual verification)

