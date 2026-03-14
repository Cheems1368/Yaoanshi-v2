# Security Best Practices Report

## Executive Summary

Scope:

- `backend-v2/`
- `frontend/huanzhe`
- `frontend/pharmacist`
- `frontend/yisheng/web-admin`

This audit found 5 actionable findings:

- 3 High (all FIXED as of 2026-03-09)
- 1 Medium FIXED as of 2026-03-09
- 1 Medium open

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

### SEC-002: All three frontends persist access or refresh tokens in `localStorage` — FIXED (2026-03-08)

**Status: FIXED** — All three frontends now store accessToken in memory only and refreshToken in sessionStorage. localStorage no longer contains any token material.

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

- Prefer backend-managed `HttpOnly` cookies for refresh/session material.
- If bearer auth must remain, keep refresh tokens out of browser storage and keep access tokens memory-only with short TTL.

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

### SEC-005: AI proxy forwards raw user prompts to an external upstream without visible consent, minimization, or audit controls

Impact:
Patient-entered medical text can cross the first-party boundary to a third-party AI endpoint with no code-visible redaction, explicit consent gate, or durable disclosure trail.

Location:

- `backend-v2/src/main/resources/application.yml:28-32`
- `backend-v2/src/main/java/com/medicine/service/ai/AiChatService.java:140-176`

Evidence:

```text
base-url: ${MEDICINE_AI_BASE_URL:https://api.openai.com/v1/chat/completions}
payload.put("messages", messages);
.header("Authorization", "Bearer " + aiProperties.getApiKey())
```

Fix:

- Add explicit user-facing consent and provider disclosure for medical-text submission.
- Minimize/redact identifiable data before forwarding.
- Add durable audit metadata for provider, request class, and decision path.
- Restrict upstream base URLs to an allowlist.

Mitigation:

- Enforce server-side content length and PHI filters before upstream dispatch.

False positive notes:

- If the configured base URL points to a self-hosted internal model and consent is handled outside this repository, severity is lower. I did not find that evidence in the scanned code.
