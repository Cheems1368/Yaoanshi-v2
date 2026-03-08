# Backend-v2 Architecture Compliance Report

## Goal And Scope

Goal:
Check whether `backend-v2` matches `Standard document/BACKEND_STRUCTURE.md` and the `medicine-contract-and-architecture` governance boundaries.

Scope:

- `backend-v2/src/main/java/com/medicine/**`
- `Standard document/BACKEND_STRUCTURE.md`

Overall result:
`backend-v2` is partially compliant. Core route prefixes, `CommonResult<T>`, and most controller/service/repository layering are aligned, but there are still contract-shape and packaging-boundary drifts that should be treated as remediation items before the next contract freeze.

## Contract Impact

### ARC-001: App auth/patient APIs still return dynamic `Map` payloads instead of explicit DTOs

Evidence:

- `backend-v2/src/main/java/com/medicine/api/controller/AppAuthController.java:45-55`
- `backend-v2/src/main/java/com/medicine/api/controller/AppPatientController.java:29-54`
- `backend-v2/src/main/java/com/medicine/service/auth/AppPatientService.java:41-112`
- `backend-v2/src/main/java/com/medicine/service/auth/AppPatientService.java:115-189`
- `Standard document/BACKEND_STRUCTURE.md:99-116`
- `Standard document/BACKEND_STRUCTURE.md:822-828`

Why it is a drift:

- The governance skill requires explicit transport DTOs.
- The contract document defines stable named request DTOs and stable app-auth/app-patient response semantics, but current code exposes `Map<String, Object>` / `List<Map<String, Object>>`, which weakens schema stability and OpenAPI clarity.

Impact:

- Response shape can drift silently.
- Frontend/backed compatibility becomes harder to validate and version.

### ARC-002: Family binding create/unbind endpoints exist in code but are not documented in Section 6.1

Evidence:

- `backend-v2/src/main/java/com/medicine/api/controller/AppPatientController.java:44-54`
- `Standard document/BACKEND_STRUCTURE.md:822-828`
- `Standard document/BACKEND_STRUCTURE.md:1015-1019`

Why it is a drift:

- The behavior is mentioned indirectly in edge-case rules (`Family unbind`), but the API contract table does not currently list:
  - `POST /app-api/patients/me/family-bindings`
  - `DELETE /app-api/patients/me/family-bindings/{id}`

Impact:

- The implementation is ahead of the formal contract.
- Downstream consumers cannot rely on the document as the single source of truth for these endpoints.

### ARC-003: `logout` request body is still an untyped map

Evidence:

- `backend-v2/src/main/java/com/medicine/api/controller/AppAuthController.java:44-47`
- `Standard document/BACKEND_STRUCTURE.md:819-820`

Why it is a drift:

- The contract documents `deviceId` as the request payload for logout, but the controller currently accepts `Map<String, String>` instead of an explicit request DTO.

Impact:

- This creates the same transport-schema drift risk as other map-based endpoints.

## Architecture Impact

### ARC-004: Required `app` and `infrastructure` package boundaries are still absent at the top level

Evidence:

- Observed top-level package set under `backend-v2/src/main/java/com/medicine`: `api`, `common`, `config`, `domain`, `repository`, `service`

Why it is a drift:

- The governance skill boundary model expects:
  - `app` for bootstrapping/application wiring
  - `infrastructure` for external adapters such as storage, WebSocket transport, and third-party integrations

Impact:

- External adapters currently sit under `service` / `config`, which weakens one-way dependency clarity.
- Migration governance becomes harder as integrations grow.

### ARC-005: External integration concerns are mixed into service/config packages instead of an infrastructure seam

Evidence:

- `backend-v2/src/main/java/com/medicine/service/storage/S3CompatibleObjectStorageStub.java`
- `backend-v2/src/main/java/com/medicine/service/notification/AdminNotificationWebSocketHandler.java`
- `backend-v2/src/main/java/com/medicine/service/ai/AiChatService.java`

Why it is a drift:

- These classes implement storage, WebSocket transport, and external AI integration concerns. Under the governance model, they belong behind infrastructure-facing adapters or interfaces.

Impact:

- Harder to swap adapters cleanly.
- Service package now contains both domain orchestration and transport/integration code.

## Implementation Steps

1. Introduce explicit app-side response/request DTOs for auth profile, patient summary, family binding summary, and logout payload.
2. Update `Standard document/BACKEND_STRUCTURE.md` in the same change that formalizes family binding create/unbind endpoints.
3. Create `com.medicine.infrastructure` and move storage, WebSocket transport, and AI client adapter classes behind service-facing interfaces where practical.
4. Optionally create `com.medicine.app` for bootstrapping/application assembly if the team wants full conformance to the skill boundary model.
5. Regenerate or review OpenAPI after DTO changes so schema examples become stable.

## Validation Commands

```powershell
mvn -q -DskipTests compile -f D:/yaoanshi/backend-v2/pom.xml
mvn -q test -f D:/yaoanshi/backend-v2/pom.xml
```

## Rollback Strategy

- No production code was changed as part of this compliance audit.
- If follow-up remediation begins, keep DTO introduction additive first and avoid removing existing fields or routes until frontend callers have switched.
