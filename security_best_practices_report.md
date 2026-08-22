# Security Best Practices Report

## Executive Summary

Scope:

- `backend-v2/`
- `frontend/huanzhe`
- `frontend/pharmacist`
- `frontend/yisheng/web-admin`

This audit found 5 actionable findings:

- 3 High (all FIXED; SEC-002 delivered through PR #39 as of 2026-08-22)
- 1 Medium FIXED as of 2026-03-09
- 1 Medium protected-qualified on the T8 candidate in PR #40; delivery to `main` pending

The highest-risk themes are browser-accessible token storage, WebSocket authentication through query strings with wildcard origins, and patient medical data persisted in `localStorage`. I did not find obvious use of `eval`, `new Function`, or unsafe `postMessage` handlers in the scanned code paths.

## High Severity

### SEC-001: Admin WebSocket authentication sends bearer tokens in query strings and accepts any origin — FIXED (2026-03-08)

**Status: FIXED** — WebSocket auth now uses short-lived one-time tickets via `POST /admin-api/notifications/socket-ticket`; `setAllowedOriginPatterns("*")` replaced with explicit origin allowlist configured via `medicine.notifications.websocket.allowed-origins`.

~~Impact:~~
~~Admin access tokens can be exposed in browser history, intermediary logs, reverse-proxy logs, and monitoring pipelines; wildcard origin acceptance also weakens origin-based defenses for the notification channel.~~

Location:

- `frontend/pharmacist/src/utils/adminNotifications.js:69`
- `frontend/yisheng/web-admin/src/utils/adminNotifications.js:66`
- `backend-v2/src/main/java/com/medicine/service/notification/AdminNotificationWebSocketHandler.java:30-39`
- `backend-v2/src/main/java/com/medicine/config/AdminNotificationWebSocketConfig.java:20-22`

Evidence:

```text
new WebSocket(`${resolveWebSocketUrl()}?access_token=${encodeURIComponent(token)}`)
token = ...getQueryParams().getFirst("access_token");
registry.addHandler(..., "/ws/admin-notifications").setAllowedOriginPatterns("*");
```

Fix:

- Move WebSocket auth to `Authorization: Bearer ...` during handshake, or use a short-lived one-time socket ticket.
- Replace `setAllowedOriginPatterns("*")` with explicit dev/prod origin allowlists.
- Ensure upstream proxies redact query strings if any transition period is needed.

Mitigation:

- Reduce token lifetime for professional sessions.
- Disable query-string auth once header-based auth is available.

False positive notes:

- If an upstream gateway already strips query strings from all logs, leak surface is reduced, but the code still accepts the token in the URL and still allows all origins.

### SEC-002: Browser-accessible refresh-token storage — FIXED (2026-08-22)

**Status: FIXED.** PR #39 was squash-merged to `main` as `4c90292`. Patient, pharmacist, and doctor official clients keep access tokens in memory and use backend-managed HttpOnly refresh Cookies. Session responses contain no refresh token, professional refresh query parameters were removed, and refresh/logout require `X-Auth-Refresh: 1`. The production Nginx credential limiter covers both legacy and session professional login paths, with a CI regression check preventing either path from falling through to the general API rate. Local backend, frontend, Android-build, static-scan, HTTP-contract, nine-scenario E2E, Android physical-device, protected PR gate, and post-merge `main` CI passed.

~~Impact:~~
~~Any XSS in the browser runtime can immediately exfiltrate both access and refresh tokens, enabling session replay and long-lived account takeover.~~

Location:

- `frontend/huanzhe/src/app/session.ts:23-40`
- `frontend/pharmacist/src/store/user.js:76-97`
- `frontend/pharmacist/src/store/user.js:123-147`
- `frontend/yisheng/web-admin/src/store/user.js:76-97`
- `frontend/yisheng/web-admin/src/store/user.js:123-147`

Evidence:

```text
localStorage.setItem(ACCESS_TOKEN_KEY, token)
localStorage.setItem(REFRESH_TOKEN_KEY, token)
localStorage.setItem('token', token.value)
localStorage.setItem('refreshToken', refreshToken)
```

Fix:

- Implemented additive patient and professional session endpoints returning `SessionTokenDTO` without refresh-token material.
- Implemented persistent patient and browser-session professional Cookies with production/local SameSite and Secure policies.
- Implemented database conditional rotation/revocation for patient sessions and Redis compare-and-set/compare-and-delete for professional sessions.
- Migrated all three official clients to Cookie session restoration while retaining Bearer access-token authorization and legacy endpoint compatibility.
- Verified two physical Android process-stop/restart cycles on Xiaomi `2407FRK8EC` (Android 14/API 34), including successful Cookie refresh followed by protected JSON reads.
- Verified prescription image, help-order image, WebM voice playback, WAV transcript-source, presigned MinIO PUT, file registration, and post-restart record visibility without recording tokens, Cookies, phone numbers, or credentials as evidence.
- Removed the conflicting `CapacitorHttp` XHR patch after a physical-device reproduction showed relative requests failing before dispatch; retained `CapacitorCookies` and added a regression test for this configuration.

Mitigation:

- Tighten CSP at the edge.
- Reduce token TTLs and scope.
- Add logout-all / token revocation paths for professional clients if not already present.

False positive notes:

- This is a design-level security exposure, not proof of an existing XSS bug. The risk becomes exploitable once any DOM XSS lands anywhere on the same origin.

### SEC-003: Patient identity and health data are stored in `localStorage`, with additional console logging — FIXED (2026-03-09)

**Status: FIXED** — Patient profile and health data services now use runtime memory only (no localStorage). PHI-path console.log calls removed. patient_mobile moved from localStorage to sessionStorage. Backend profile DTOs no longer expose idCard/hospitalPatientId; identity material persisted as SHA-256 digest only.

~~Impact:~~
~~Medical profile data, structured health history, and real-name verification details remain readable to any script running in the origin and may also leak through browser debugging or shared-device access.~~

Location:

- `frontend/huanzhe/src/services/healthDataService.ts:23-37`
- `frontend/huanzhe/src/services/healthDataService.ts:42-56`
- `frontend/huanzhe/src/services/userDataService.ts:29-80`
- `frontend/huanzhe/src/services/userDataService.ts:84-102`

Evidence:

```text
localStorage.setItem(this.STORAGE_KEY, JSON.stringify(healthData));
localStorage.setItem(this.STORAGE_KEY, JSON.stringify(updatedProfile));
console.log('健康数据已保存:', healthData);
```

Fix:

- Move patient profile and health data to backend persistence or encrypted platform storage if this must stay client-side.
- Remove production `console.log` for patient data paths.
- Avoid caching PII/PHI in web storage beyond strictly necessary session state.

Mitigation:

- Clear cached local state on logout and account switch.
- Mask or omit nonessential identity fields from any cached representation.

False positive notes:

- Some data is masked before storage, but the stored objects still contain sensitive medical and identity context.

## Medium Severity

### SEC-004: `backend-v2` shipped insecure default secrets and exposed Swagger/OpenAPI anonymously in the default profile — FIXED (2026-03-09)

**Status: FIXED** — `backend-v2` now disables `springdoc` by default, restricts OpenAPI bean creation and Swagger allowlisting to the `local` profile, and fails fast in `prod` before datasource initialization when JWT/storage secrets are blank or placeholder values.

~~Impact:~~
~~A misconfigured deployment can boot with predictable JWT/object-storage credentials and expose API structure to unauthenticated callers.~~

Location:

- `backend-v2/src/main/resources/application.yml:23-42`
- `backend-v2/src/main/resources/application-prod.yml`
- `backend-v2/src/main/java/com/medicine/config/ProductionSecretEnvironmentPostProcessor.java`
- `backend-v2/src/main/java/com/medicine/config/OpenApiConfig.java`
- `backend-v2/src/main/java/com/medicine/config/SecurityConfig.java`

Evidence:

```text
springdoc:
  api-docs.enabled: false
@Profile("local")
Production startup blocked: medicine.jwt.secret is blank or placeholder
```

Fix:

- Fail fast when JWT or object-storage credentials still match placeholder defaults.
- Restrict Swagger/OpenAPI to local/dev profiles or authenticated admin access.
- Move all deploy-time secrets to environment variables or secret stores only.

Mitigation:

- Add startup assertions for placeholder secret values.
- Add deployment checks in CI/CD.

False positive notes:

- This is a secure-defaults problem. If every environment overrides these values and Swagger is gated at an upstream layer, runtime risk is lower.

### SEC-005: AI proxy forwards raw user prompts to an external upstream without visible consent, minimization, or audit controls — FIXED ON T8 CANDIDATE

**Status: PROTECTED-QUALIFIED in PR #40 on `codex/sec-005-ai-data-boundary`; delivery to `main` awaits separate merge authorization.** Local targeted and full backend tests, the 60% JaCoCo gate, patient lint/unit/build/Android gates, Flyway fresh/V12-upgrade checks, metadata schema assertions, and bounded-cleanup validation pass. Protected review-remediation run #32568900388 passed all six checks. The three Codex review threads were answered and resolved, leaving zero unresolved conversations.

Original impact:
Patient-entered medical text could cross the first-party boundary to a third-party AI endpoint with no code-visible redaction, versioned consent, durable disclosure trail, or metadata audit.

Remediation:

- `V13__ai_data_boundary.sql` adds exact-policy consent records and metadata-only request audits without prompt, reply, payload, or body hash.
- `AiConsentPolicy` and `AiConsentService` require the current policy/provider/statement hash; the legacy timestamp alone is insufficient.
- `AiPrivacyRedactor` removes high-confidence identity patterns, including explicitly labeled encounter and patient IDs, from user and assistant text; payload tests verify the identifiers do not reach the provider.
- `AiChatService` sends at most five recent non-system messages and computes `redaction_count` only from those transmitted messages.
- `HttpAiUpstreamClient` requires an exact host allowlist, HTTPS except for explicitly allowlisted loopback hosts, and never follows redirects.
- `AiAuditService` inserts `PENDING` before network access and blocks the provider call if insertion fails; audit finalization logs identifiers only.
- The patient screen fetches disclosure on every entry, remains fail-closed while it is unavailable, submits the displayed policy version, and exposes a persistent data-use entry.

Evidence:

```text
GET  /app-api/ai/disclosure -> AiDisclosureDTO
POST /app-api/ai/consent    <- AiConsentGrantRequest { policyVersion }
MEDICINE_AI_ALLOWED_HOSTS=api.siliconflow.cn
audit status: PENDING -> SUCCESS | FAILED; expires_at = create_time + 180 days
```

Closure requirements:

- Targeted and full backend gates, patient lint/unit/build/Android gates, fresh/existing migration checks, and static no-content audit checks pass. ✅
- Protected `CI / Required Quality Gate` is green with zero unresolved review conversations. ✅ PR #40 / review-remediation run #32568900388
- Merge to `main` is a separate delivery authorization and is not part of SEC-005 candidate qualification.
