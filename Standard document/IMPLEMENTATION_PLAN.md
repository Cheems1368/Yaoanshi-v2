# IMPLEMENTATION_PLAN.md

## 1. Document role

This document is the authoritative step-by-step implementation sequence for the MVP P0 release.

Rules:

1. This document defines execution order, not product scope or architecture rationale.
2. `Standard document/PRD.md`, `APP_FLOW.md`, `TECH_STACK.md`, `FRONTEND_GUIDELINES.md`, and `BACKEND_STRUCTURE.md` are mandatory inputs for every implementation phase.
3. `docs/` is reference only and must not override this file.
4. This document covers `P0` only.
5. `P2` and later capabilities must stay in backlog and must not enter the primary build sequence.

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

## 7. Phase 4: Integration, QA, and Trial Readiness

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

Collaboration:

- Claude: define rule content (drug pairs, severity, message text) — reference `BACKEND_STRUCTURE.md` 4.9
- Codex: batch generate SQL scripts + unit tests

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

Collaboration:

- Claude: define API constraints + screening logic design
- Codex: generate service/controller/DTO skeleton + tests
- Claude: review product alignment

### Step 5.3: Patient-side DDI alert frontend

Action:

1. Add DDI screening call before prescription upload in patient app.
2. Implement alert dialog component:
   - List of alerts with severity badges (red=HIGH, orange=MEDIUM)
   - Each alert shows drug pair/condition, message, recommended action
   - Two buttons: "已知晓，继续提交" / "取消，咨询药师"
3. "取消" option can navigate to help order creation with pre-filled context.
4. Graceful degradation: if DDI screening API fails, proceed without blocking.

Collaboration:

- Claude: frontend implementation + browser acceptance
- Codex: quality review + test coverage

### Step 5.4: Pharmacist DDI rule management

Action:

1. Implement `GET /admin-api/ddi/rules` (paged, filterable by drug name and severity).
2. Implement `PUT /admin-api/ddi/rules/{id}` for editing existing rules.
3. Implement CRUD for `drug_contraindication` via `/admin-api/ddi/contraindications`.
4. Create pharmacist web rule management page at `/ddi/rules`:
   - Searchable table with drug name and severity filters
   - Create/edit form in drawer or dialog
   - Separate tabs for DDI rules and contraindication rules

Collaboration:

- Claude: define constraints + frontend implementation
- Codex: generate backend CRUD skeleton + tests
- Claude: browser acceptance

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

## 9. Backlog After P0+

The following are intentionally excluded from the primary execution sequence:

1. science education content library
2. content operation capability
3. more disease-path expansion (DDI rules for hypertension, diabetes, etc.)
4. non-MVP capability expansion
5. any work that reopens legacy backend refactor

