# Phase 4 Acceptance Checklist

## 1. Patient Critical Path

| # | Step | Expected Result | Pass |
|---|------|-----------------|------|
| 1.1 | Open patient app at :3001 | Login page displayed | Y |
| 1.2 | Send OTP and login | Redirect to /realname (new user) or /tabs/home (bound user) | Y |
| 1.3 | Complete real-name binding | Redirect to /tabs/home, binding status = BOUND | Y |
| 1.4 | View medication reminders | Today's dose tasks displayed (if active plan exists) | Y |
| 1.5 | Submit check-in | Task status changes to COMPLETED | Y |
| 1.6 | Correct check-in within 24h | Correction record created, effective record switched | Y |
| 1.7 | Upload prescription image | Local preview shown, metadata registered in backend | Y |
| 1.8 | DDI screening triggers before upload | Alert modal with severity badges if DDI rules match | Y |
| 1.9 | Submit help order | Order created, appears in pharmacist queue | Y |
| 1.10 | Complete knowledge assessment | Answers persisted | Y |
| 1.11 | Complete satisfaction survey | Survey persisted (one per order) | Y |
| 1.12 | Logout | Tokens cleared, redirect to /login | Y |

## 2. Family Delegation Path

| # | Step | Expected Result | Pass |
|---|------|-----------------|------|
| 2.1 | Patient creates family binding | Family member can login and see patient data | Y |
| 2.2 | Family submits check-in | operatorType = FAMILY recorded | Y |
| 2.3 | Family submits help order | Order created on behalf of patient | Y |
| 2.4 | Patient unbinds family | Family loses access immediately (PATIENT_BIND_CONFLICT) | Y |

## 3. Pharmacist Collaboration Path

| # | Step | Expected Result | Pass |
|---|------|-----------------|------|
| 3.1 | Pharmacist login via legacy auth | Token accepted by backend-v2 | Y |
| 3.2 | View patient list | Patient data displayed with search/filter | Y |
| 3.3 | Edit patient medical fields | Primary disease saved, ABCD2 snapshot created | Y |
| 3.4 | Review prescription | Drawer opens with item list and DDI warnings | Y |
| 3.5 | Confirm/reject prescription | Status updated, event logged | Y |
| 3.6 | Create medication plan | Plan with items created, tasks generated on activate | Y |
| 3.7 | Claim and reply to help order | Order status transitions correctly | Y |
| 3.8 | Escalate to doctor | Order moves to doctor queue, notification sent | Y |
| 3.9 | Manage DDI rules | Create/edit DDI rules and contraindications | Y |

## 4. Doctor Path

| # | Step | Expected Result | Pass |
|---|------|-----------------|------|
| 4.1 | Doctor login via legacy auth | Token accepted | Y |
| 4.2 | View escalated work orders | Only escalated orders shown | Y |
| 4.3 | Reply and close work order | Order closed, satisfaction reminder triggered | Y |

## 5. Realtime Notifications

| # | Step | Expected Result | Pass |
|---|------|-----------------|------|
| 5.1 | Patient submits help order | Pharmacist receives WebSocket notification | Y |
| 5.2 | Pharmacist escalates order | Doctor receives WebSocket notification | Y |
| 5.3 | Notification log records delivery | status = SENT in notification_log | Y |

## 6. Build Validation

| # | Check | Result |
|---|-------|--------|
| 6.1 | `npm run build` patient app | Pass |
| 6.2 | `npm run build` pharmacist web | Pass |
| 6.3 | `npm run build` doctor web | Pass |
| 6.4 | `mvn -DskipTests compile` backend-v2 | Pass |
| 6.5 | `mvn test` backend-v2 | Pass (21+ service tests) |

## 7. E2E Browser Tests

| Script | Coverage | Result |
|--------|----------|--------|
| e2e:auth-bind | Login -> bind -> home -> logout | Pass |
| e2e:auth-refresh | Token refresh on page reload | Pass |
| e2e:auth-expiry | Auto-refresh on token expiry | Pass |
| e2e:prescription-review | Upload -> DDI -> pharmacist confirm/reject | Pass |
| e2e:medication-family | Plan tasks -> check-in -> correction -> family delegation -> unbind | Pass |
| e2e:helporder-collab | Family submit -> pharmacist reply/escalate -> doctor close -> assessments | Pass |
| e2e:admin-notifications | Realtime notification delivery across pharmacist/doctor | Pass |
| e2e:pharmacist-patient | Pharmacist patient search/edit | Pass |
| e2e:challenge-cup | Rational medication + AI assistant (mock) | Pass |
| e2e:challenge-cup:live | Rational medication + AI assistant (live SiliconFlow) | Pass |
