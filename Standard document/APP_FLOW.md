# APP_FLOW.md

## 1. Document purpose

This document is the authoritative MVP flow document for `Yaoshian / 药按时`.

Rules:

- All flow descriptions in this document use plain English.
- This document defines the expected user navigation for MVP.
- Old front-end code can be inspected for current implementation state, but it is not a flow authority.
- If a legacy route conflicts with this document, this document wins.

## 2. Product surfaces

There are three product surfaces in MVP:

1. Patient App on Android
2. Pharmacist Web
3. Doctor Web

## 3. Global navigation rules

### 3.1 Authentication rule

- Patient and family account must log in before entering the main medication flow.
- Pharmacist and doctor must log in before entering any work dashboard.
- Each client keeps the access token in runtime memory only and restores it through its HttpOnly refresh Cookie when a protected route is first entered after reload.
- Access-token expiry triggers one locked Cookie refresh and one request retry. Refresh failure clears runtime auth state and redirects to login.
- Refresh Cookies are transport-only and never replace Bearer authorization on business APIs.
- Patient sessions persist for 30 days unless revoked or expired. Pharmacist and doctor sessions end when the browser session ends.
- Manual logout revokes the server session, clears the refresh Cookie, clears runtime access state, and returns to login.

### 3.2 Entry gate rule

For the patient side, the required gate is:

1. Login
2. Patient identity binding
3. Active medication plan exists

The following do not block the main flow in MVP:

- Real-name completion as a separate gate
- Questionnaire completion as a separate gate

### 3.3 Error handling rule

For all user surfaces:

- If the network fails, show a clear message and allow retry.
- If access-token renewal fails, redirect to login.
- If the user has no permission, show a permission message and stop the action.
- If data is empty, show an empty state and the next recommended action.

### 3.4 Success handling rule

For all user surfaces:

- Show a clear success message.
- Update the relevant list or dashboard state immediately.
- Keep the user in the current flow unless the next screen is required.

## 4. Patient App flow

### 4.1 Patient App goal

The Patient App exists to help the patient or family account do one core thing:

Take medicine on time and record the result with the smallest possible effort.

### 4.2 Patient App main route list

Current MVP route list for the patient app:

- `/login` - Login Screen
- `/realname` - Patient Binding Screen
- `/questionnaire` - Health profile questionnaire, not a hard gate
- `/knowledge-assessment` - DAY_1 / WEEK_4 medication-literacy assessment, not a hard gate
- `/tabs/home` - Home Screen
- `/tabs/help-order` - Help Order List / Entry Screen
- `/help-order/:id` - Help Order Detail / Conversation Screen
- `/tabs/prescription` - Prescription Screen
- `/tabs/profile` - Profile Screen
- `/medication-reminder` - Reminder Task Screen
- `/medication-record` - Medication Record Screen
- `/drug-detail/:id` - Drug Detail Screen
- `/drug-category/:id` - Drug Category Screen
- `/rational-medication` - Rational Medication Education Screen
- `/education/cards/:id` - Reviewed Education Card Detail Screen
- `/ai-assistant` - AI Medication Assistant Screen
- `/self-reported-medications` - Self-Reported Medication Screen

### 4.3 Patient App screen list

#### Login Screen

Route: `/login`

Purpose:

- Log in as patient or family account with mobile number and SMS code

Trigger:

- App is opened and no valid session exists
- Session expired
- User manually logs out

Success:

- If the account is already bound, go to `/tabs/home`
- If the account is not bound, go to `/realname`

Error:

- Show invalid code or login failure
- Stay on the same screen
- Allow resend and retry

#### Patient Binding Screen

Route: `/realname`

Purpose:

- Bind the logged-in account to the correct patient identity

Trigger:

- First login after account creation
- Account exists but patient identity is not yet confirmed

Success:

- If there is an active medication plan, go to `/tabs/home`
- If there is no active medication plan, show a waiting state and explain that the pharmacist will create the plan

Error:

- Show binding failure reason
- Allow retry
- If duplicate identity is detected, show that pharmacist confirmation is required

#### Health Profile Questionnaire Screen

Route: `/questionnaire`

Purpose:

- Complete the non-blocking health profile questionnaire.

Trigger:

- The patient chooses to complete or update the health profile.

Success:

- Save the answers
- Return to the previous screen or `/tabs/home`

Error:

- Show save failure and allow retry
- Keep already entered answers when possible

#### Knowledge Assessment Screen

Route: `/knowledge-assessment`

Purpose:

- Complete the fixed `medication-literacy-v1` science-education assessment without blocking medication or help-order flows.
- DAY_1 starts on the first ACTIVE plan effective date; WEEK_4 becomes current on calendar day 28. A future effective date is not due.
- The client sends only question-set version, phase, question codes, and selected options; scoring is server-owned.

Trigger:

- A due card appears on Home.
- The pharmacist reminds the patient to complete the current phase.

Success:

- Show the server-calculated score and completion timestamp.
- A completed phase cannot be submitted twice.

Error:

- Keep the main medication flow usable and allow retry.

#### Home Screen

Route: `/tabs/home`

Purpose:

- Show today's pending medicines
- Show the next reminder
- Show quick entry to medication actions and help

Trigger:

- Login success
- App open with valid session
- User taps Home tab

Success state:

- User sees today’s tasks, next reminder, and status summary

Error state:

- Show partial load error and retry option
- Keep the rest of the page usable if some data still loads

Main actions from Home:

1. Tap the next medicine reminder -> go to `/medication-reminder`
2. Tap medication history -> go to `/medication-record`
3. Tap Help -> go to `/tabs/help-order`
4. Tap Prescription -> go to `/tabs/prescription`
5. Tap Profile -> go to `/tabs/profile`

#### Reminder Task Screen

Route: `/medication-reminder`

Purpose:

- Complete a medication event with the minimum number of actions

Trigger:

- Local reminder notification is tapped
- In-app reminder card is tapped
- User opens a pending task from the home screen

Decision points:

1. Did the user take the medicine on time?
2. Is this a delayed record?
3. Is this a missed or skipped dose?

Success:

- Save the record
- Show the result clearly
- Update today’s progress
- Show the next reminder
- Return to `/tabs/home` or stay on the result state

Error:

- Show save failure
- Keep the entered selection
- Allow retry
- If offline, cache locally and auto-sync later
- Native Android caches at most 48 hours of tasks and pending check-ins in app-private AES/GCM ciphertext backed by Android Keystore. Web does not persist medical task/check-in data in Web Storage.
- Every queued check-in carries a stable `clientRequestId`; entry, login/online recovery, and medication-page entry attempt replay, and the server idempotency contract prevents duplicates.
- Local lock-screen reminders use generic wording and contain no identity, medicine name, or dose.

#### Medication Record Screen

Route: `/medication-record`

Purpose:

- View medication history
- Correct a recent record within 24 hours

Trigger:

- User taps medication history from Home
- User wants to review or correct an event

Decision points:

1. Is the record still inside the 24-hour correction window?
2. Is the current user the patient or the family account?

Success:

- Updated record becomes the latest valid result for that dose
- Audit trail is preserved

Error:

- Show that the record can no longer be edited
- Show retry if the update request fails

#### Help Order Screen

Route: `/tabs/help-order`

Purpose:

- Create and track help orders

Trigger:

- User taps Help from Home
- User needs support after a missed dose, adverse reaction, or medicine question

Available categories:

1. Medication question
2. Missed or wrong dose
3. Adverse reaction
4. Plan change request
5. Other

Success:

- Create the help order
- After successful submit, enter the help order detail conversation
- Show that the pharmacist will handle it during service hours if submitted after hours
- For image help orders, pre-sign, upload, register the file, then submit the order with up to three image file IDs
- For voice help orders, the user fills title/category first, holds the voice button, releases to send the audio original, and enters the detail conversation

Error:

- Show validation message when the title is missing
- Show upload failure if an image fails
- Show voice send failure if recording, upload, or file registration fails
- Allow retry

#### Help Order Detail / Conversation Screen

Route: `/help-order/:id`

Purpose:

- Show the full help order event stream
- Let patient or family append follow-up messages in the same order thread

Trigger:

- User submits a new help order successfully
- User taps an existing help order from the list

Success:

- Load the latest event stream
- Show new pharmacist or doctor replies
- Allow text follow-up while the order is still open
- Show image thumbnails in the relevant conversation bubble
- Show voice playback, duration, transcription status, and transcription result under the relevant conversation bubble
- Allow follow-up messages with text, images, or one voice recording, as long as at least one of them is present
- Patient/family can tap "transcribe" or "retry transcribe" on an audio message; if speech credentials are missing or provider fails, the original audio remains playable

Error:

- Show detail load failure and retry
- If realtime update is unavailable, keep the page usable with polling refresh
- When the app WebSocket is unavailable, initialize the cold-start boundary from `/app-api/notifications/logs/latest-id`, then poll `/app-api/notifications/logs` with an `afterId` cursor and dispatch the same in-app event shape without replaying the full history.
- A CLOSED order offers a non-blocking 1–5 satisfaction card; the patient may defer it and return later, but may submit only once.

#### Prescription Screen

Route: `/tabs/prescription`

Purpose:

- Show prescriptions and allow prescription image upload
- Trigger DDI screening before submission (P0+ enhancement)

Trigger:

- User taps Prescription from the tab bar
- Pharmacist asks the patient to upload a prescription photo

Success (with DDI screening):

1. Image is selected and drug names are extracted or entered
2. System calls DDI screening endpoint
3. If DDI alerts exist, show alert dialog (see section 7.4.2)
4. If user confirms or no alerts, proceed with upload
5. Pharmacist can later review and confirm OCR result

Error:

- Show upload failure
- Suggest retake if the image is unclear
- Keep existing uploaded items visible, including after page reload
- DDI screening failure does not block upload (graceful degradation)

#### Profile Screen

Route: `/tabs/profile`

Purpose:

- Show account info, patient info, family binding, and logout entry

Trigger:

- User taps Profile from the tab bar

Success:

- Profile data loads
- User can manage family account binding status and view care-related info
- Only a patient account can add, list, confirm, or remove family bindings; the UI enforces the two-binding limit while the server remains authoritative.

Error:

- Show load failure and allow retry

#### Medication Consultation Screen

Route: `/medication-consultation`

Purpose:

- Show medication-related support content and consultation entry

Trigger:

- User taps a medication consultation entry from Home, Profile, or Help

Success:

- User reads content or starts a support action

Error:

- Show content load failure and a retry action

#### Drug Detail Screen

Route: `/drug-detail/:id`

Purpose:

- Show medicine details

Trigger:

- User taps a medicine item from reminder, record, or prescription screens

Success:

- Show the medicine detail and usage notes

Error:

- Show not found or load failure

#### Drug Category Screen

Route: `/drug-category/:id`

Purpose:

- Show a grouped list of medicines by category

Trigger:

- User taps a category entry

Success:

- Show the category list

Error:

- Show load failure and allow retry

#### Rational Medication Education Screen

Route: `/rational-medication`

Purpose:

- Show static medication navigation plus reviewed education recommendations from backend-v2

Trigger:

- User opens education content from a campaign or a profile entry

Success:

- Recommended `REVIEWED` cards are shown at the top using patient diagnosis, active medications, elderly tags, and GENERAL fallback
- User can open `/education/cards/:id` to read card body and review metadata
- Static category browsing remains available below recommendations

Error:

- Show load failure
- Recommendation load failure does not block static category browsing

#### AI Medication Assistant Screen

Route: `/ai-assistant`

Purpose:

- Provide AI-powered medication knowledge Q&A for patients
- Answer questions about medication timing, missed doses, adverse effects, and general medication literacy
- Explain the third-party data boundary before any medical text leaves the application

Trigger:

- User taps "AI 用药助手" quick entry on Home page
- Every screen entry requests the current disclosure from `GET /app-api/ai/disclosure`; local consent caches are not authoritative

Success:

- Before first use or whenever the policy/provider/statement changes, show the full server-provided provider name and disclosure statement
- Grant consent by posting the displayed `policyVersion`; only an exact current version unlocks sending
- Keep a visible "数据使用说明" entry so the disclosure can be reopened at any time
- AI responds with concise medication guidance including safety disclaimer
- The screen reminds the user not to enter identity information and explains that only redacted recent messages are sent; account mobile, identity number, patient id, and health profile are not appended
- The disclosure states that call audit retains metadata for 180 days and does not save question or response text

Error:

- Until disclosure loads successfully, keep send disabled and show a retry-later message
- Missing or stale consent reopens the current disclosure instead of sending the question
- AI service failure preserves the local conversation and allows retry; it must not expose upstream error bodies

#### Self-Reported Medication Screen

Route: `/self-reported-medications`

Purpose:

- Let the patient record medications taken outside the formal medication plan
- Trigger DDI screening before submission
- View and manage self-reported medication records

Trigger:

- User taps "记录新药品" (Record New Drug) quick entry on Home page

Main actions:

1. View list of self-reported medication records (PENDING / ACTIVE / STOPPED / TRANSFERRED)
2. Tap "新增" to open entry form: drug name (required), dosage, notes
3. Before submit: system calls DDI screening; if alerts exist, show alert dialog (see section 7.4.2)
4. Submit → `POST /app-api/self-reported-medications`
5. PENDING records can be withdrawn → `POST /app-api/self-reported-medications/{id}/withdraw`

Success:

- Record is created and review work order is auto-created for pharmacist
- User sees the updated record list with status

Error:

- Show DDI screening failure gracefully (do not block submission)
- Show save failure and allow retry

### 4.4 Patient App primary flow

#### Flow A: First-time patient entry

Trigger:

- New patient opens the app for the first time

Steps:

1. Open app
2. Go to `/login`
3. Enter mobile number and SMS code
4. Login succeeds
5. Go to `/realname`
6. Complete patient identity binding
7. System checks whether an active medication plan exists
8. If yes, go to `/tabs/home`
9. If no, show waiting state and pharmacist follow-up message

Success:

- Patient is ready to start the medication flow

Error:

- Login or binding failure blocks entry
- Show exact reason and retry option

#### Flow B: Daily medication action

Trigger:

- Reminder notification fires
- Patient opens today’s tasks from Home

Steps:

1. User opens `/medication-reminder`
2. App shows each task reminder stage: upcoming, due, overdue, family reminder due, or handled
3. If the task is at least 30 minutes overdue and the patient has an active family binding, the patient sees that family has been reminded
4. User selects the result: on time, delayed, missed, or skipped
5. User optionally adds a note
6. User submits
7. System saves the record
8. Home data refreshes
9. System shows the next reminder

Success:

- Record is stored
- Adherence calculation is updated

Error:

- Request fails or device is offline
- Cache locally if possible
- Sync later automatically

#### Flow C: Missed dose correction

Trigger:

- Patient realizes a dose was missed or recorded incorrectly

Steps:

1. Open `/medication-record`
2. Select the target event
3. Check whether the event is still within 24 hours
4. If yes, edit and resubmit
5. System marks the newest valid result as current
6. Audit history is kept

Success:

- Medication history reflects the corrected result

Error:

- If outside the time window, show that editing is no longer allowed

#### Flow D: Patient asks for help

Trigger:

- Patient has a question, side effect, missed dose issue, or plan change request

Steps:

1. Open `/tabs/help-order`
2. Choose category
3. Enter title and optionally add detail
4. Submit
5. Enter `/help-order/:id`
6. Optionally continue the conversation in the same thread
7. Pharmacist is notified

Decision point:

- If the case sounds urgent, the system tells the user to seek offline emergency care

Success:

- Help order enters the pharmacist queue
- Patient can continue to append messages in the same order thread

Error:

- Validation or upload failure
- Stay on the same screen and allow retry

### 4.5 Family account flow

#### Flow E: Family-assisted medication action

Trigger:

- Family account logs in to help the patient
- A pending dose task is at least 30 minutes overdue and family reminder is due

Steps:

1. Family account logs in through `/login`
2. Bound care view opens
3. Family account sees pending tasks and help orders
4. Overdue dose tasks with active family reminder are highlighted as needing family assistance
5. Family account completes a dose record or submits a help order
6. System stores the operator type as family account

Success:

- The same patient flow is completed with family assistance
- After family check-in, the task returns to handled reminder state

Error:

- If family binding is removed, deny the action and return to a safe state

## 5. Pharmacist Web flow

### 5.1 Pharmacist Web goal

The Pharmacist Web exists to help the pharmacist:

1. onboard the patient,
2. maintain the medication plan,
3. handle abnormal adherence,
4. process help orders,
5. confirm OCR results,
6. export measurable service results.

### 5.2 Pharmacist Web MVP route list

Authoritative MVP route list for pharmacist-facing navigation:

- `/login` - Login Screen
- `/` or `/index` - Home / Dashboard Screen
- `/patient/list` - Patient List Screen
- `/patient/info` - Patient Detail Screen
- `/system/help-order` - Help Order Queue Screen
- `/system/help-order/detail` - Help Order Detail Screen
- `/prescription/prescriptions` - Prescription List Screen
- `/prescription/prescriptionDetail` - Prescription Detail / OCR Review Screen
- `/ddi/rules` - DDI Rule Management Screen (P0+ enhancement)
- `/education/content` - Reviewed Education Content Management Screen (T4)
- `/user/profile` - User Profile Screen

Non-MVP legacy menu groups must stay hidden for trial users.

### 5.3 Pharmacist Web screen list

#### Login Screen

Route: `/login`

Purpose:

- Authenticate pharmacist user

Trigger:

- Session is missing or expired

Success:

- Go to dashboard

Error:

- Show login failure and allow retry

#### Dashboard Screen

Route: `/` or `/index`

Purpose:

- Show workload summary
- Show new help orders
- Show high-risk medication alerts
- Show patients with poor adherence

Trigger:

- Login success
- User opens the default page

Success:

- Pharmacist sees the next important work items

Error:

- Show partial load state and retry actions

#### Patient List Screen

Route: `/patient/list`

Purpose:

- Search and open patients managed by the pharmacist

Trigger:

- User wants to view or manage patient records

Success:

- Open patient detail

Error:

- Show load failure or empty state

#### Patient Detail Screen

Route: `/patient/info`

Purpose:

- View patient profile
- Complete missing medical information
- Review adherence
- Manage family account context

Trigger:

- User selects a patient from the patient list
- User opens a patient from a help order

Success:

- Updated patient information is saved and synced to the patient app where needed

Error:

- Show save failure and allow retry

#### Help Order Queue Screen

Route: `/system/help-order`

Mobile route: `/mobile/workorders`

Purpose:

- Show all help orders assigned to the pharmacist
- On mobile Web, provide the full pharmacist workflow: list pending orders, open detail, view image/voice attachments, claim, reply, trigger voice transcription, escalate to doctor, reject, and close

Trigger:

- User opens work queue
- Notification for a new help order is clicked

Success:

- User opens a help order detail screen
- Mobile Web receives help-order WebSocket events, refreshes the list, and shows an in-page prompt

Error:

- Show queue load failure and retry

#### Help Order Detail Screen

Route: `/system/help-order/detail`

Purpose:

- Read the patient issue
- respond,
- reject,
- or escalate to doctor
- View image attachments on mobile Web without entering the desktop workstation layout
- Use the same status-based action rules on desktop and mobile Web: `SUBMITTED` allows claim/reply/escalate; `PHARMACIST_IN_PROGRESS` allows reply/escalate/reject/close; `WAITING_DOCTOR`, `CLOSED`, and `REJECTED` are read-only

Trigger:

- User selects a help order from the queue

Decision points:

1. Can the pharmacist handle the issue directly?
2. Is the issue incomplete and should be rejected?
3. Does the case require doctor escalation?

Success:

- Status changes to pharmacist processing, completed, rejected, or waiting for doctor
- Timeline and audit data are updated
- For the first image-help-order phase, pharmacist text reply is visible in the patient conversation detail
- Pharmacist can play help-order voice messages, view transcription state/result, and trigger transcription or retry
- Pharmacist mobile Web can claim, reply, transcribe, escalate, reject, and close without switching to the desktop route
- Doctor views can play voice messages and view transcription results, but cannot trigger transcription

Error:

- Show update failure
- Keep unsaved response text if possible

#### Prescription List Screen

Route: `/prescription/prescriptions`

Purpose:

- Review uploaded prescriptions

Trigger:

- User opens the prescription work area
- New uploaded prescription arrives

Success:

- Open prescription detail

Error:

- Show load failure

#### Prescription Detail / OCR Review Screen

Route: `/prescription/prescriptionDetail`

Purpose:

- Review original prescription image
- confirm OCR result
- manually enter medicine items if OCR fails

Trigger:

- User opens a prescription item from the list

Decision points:

1. Is OCR good enough?
2. Is manual correction needed?
3. Does the medicine set trigger a DDI warning?

Success:

- Confirmed medicine items are saved
- DDI high-risk alert requires pharmacist confirmation before final save

Error:

- OCR fail: fall back to manual template entry
- Save fail: show retry

#### User Profile Screen

Route: `/user/profile`

Purpose:

- View pharmacist account information

Trigger:

- User opens profile from the header or menu

Success:

- Profile loads

Error:

- Show load failure

### 5.4 Pharmacist primary flows

#### Flow F: Enroll a new patient

Trigger:

- Pharmacist meets a patient offline and starts onboarding

Steps:

1. Pharmacist logs in
2. Open patient list
3. Search existing patient or create a new identity match
4. Confirm mobile number + hospital patient ID
5. Ask the patient to log in on the app
6. Complete binding confirmation
7. Create or prepare the medication plan

Success:

- Patient can enter the Home screen after binding and plan activation

Error:

- Duplicate identity conflict requires pharmacist confirmation

#### Flow G: Maintain medication plan

Trigger:

- New prescription arrives
- Existing plan needs a change

Steps:

1. Open patient detail
2. Open prescription review if needed
3. Use OCR or manual entry
4. Confirm medicine items
5. Check DDI warning if present
6. Save plan
7. System recalculates remaining reminders for today

Success:

- Patient app shows the updated plan

Error:

- OCR failure falls back to manual entry
- DDI alert requires explicit confirmation before save

#### Flow H: Handle a help order

Trigger:

- New help order notification

Steps:

1. Notification opens the help order queue or detail
2. Pharmacist reads patient context
3. Pharmacist decides to reply, reject, or escalate
4. If handled directly, close the order
5. If escalation is needed, send to doctor

Success:

- Patient sees updated status
- Satisfaction request can be triggered after closure

Error:

- Save failure keeps the user on the detail screen

#### Flow I: Export adherence report

Trigger:

- Pharmacist needs trial evidence or follow-up review material

Steps:

1. Open patient detail or report area
2. Choose 7-day or 30-day range
3. Export report

Success:

- Export is available in Excel or CSV

Error:

- Show export failure and retry

## 6. Doctor Web flow

### 6.1 Doctor Web goal

The Doctor Web exists to let the doctor handle only escalated clinical issues from pharmacists.

### 6.2 Doctor Web MVP route list

Current MVP route list for doctor-facing navigation:

- `/login` - Login Screen
- `/doctor-dashboard` - Doctor Dashboard Screen
- `/workorders` - Escalated Work Order Queue Screen
- `/mobile/workorders` - Doctor Mobile Escalated Work Order Screen
- `/profile` - User Profile Screen

The following are not part of the normal doctor MVP navigation:

- `/patients`
- `/prescriptions`
- `/chat`
- generic full-access admin menus

### 6.3 Doctor Web screen list

#### Login Screen

Route: `/login`

Purpose:

- Authenticate doctor user

Trigger:

- Session missing or expired

Success:

- Go to `/doctor-dashboard`

Error:

- Show login failure and allow retry

#### Doctor Dashboard Screen

Route: `/doctor-dashboard`

Purpose:

- Show escalated cases
- Show pending response items

Trigger:

- Login success
- User opens the dashboard

Success:

- Doctor sees only doctor-relevant work

Error:

- Show partial load error and retry

#### Escalated Work Order Queue Screen

Route: `/workorders`

Purpose:

- List only help orders escalated to doctor

Trigger:

- Dashboard entry
- Notification click

Success:

- Doctor opens one case and writes back the decision

Error:

- Show queue load failure

#### Doctor Mobile Escalated Work Order Screen

Route: `/mobile/workorders`

Purpose:

- Let doctors process escalated help orders on mobile Web during trial/demo walkthroughs
- Show the patient minimum context needed for a decision
- Submit a structured doctor decision

Trigger:

- Doctor opens the mobile Web route directly
- Doctor receives a help-order notification and opens the mobile route

Success:

- Doctor sees escalated order detail, images, timeline, and patient context
- Doctor chooses `ADOPTED` or `NOT_ADOPTED`, enters the conclusion, and submits
- Patient and pharmacist can see the final doctor conclusion in the timeline

Error:

- Show load or save failure and allow retry

#### User Profile Screen

Route: `/profile`

Purpose:

- View doctor account info

Trigger:

- User opens profile

Success:

- Profile loads

Error:

- Show load failure

### 6.4 Doctor primary flow

#### Flow J: Process an escalated case

Trigger:

- Pharmacist escalates a help order
- Doctor receives a Web notification

Steps:

1. Doctor logs in if needed
2. Go to `/workorders` on desktop or `/mobile/workorders` on mobile Web
3. Open the escalated item
4. Review pharmacist notes, patient images, and patient context
5. Select `ADOPTED` or `NOT_ADOPTED`
6. Write the final doctor conclusion and submit the decision

Success:

- Patient and pharmacist can see the updated outcome
- The order is closed by the doctor decision action

Error:

- Show save failure and allow retry

## 7. Cross-surface decision points

### 7.1 No active medication plan

Applies to:

- Patient App

Behavior:

- Do not enter the medication task flow
- Show that the pharmacist will create the plan

### 7.2 After-hours help order

Applies to:

- Patient App
- Pharmacist Web

Behavior:

- Patient can still submit the order
- System explains the service hours
- Queue processing starts in work hours

### 7.3 Urgent condition

Applies to:

- Patient App

Behavior:

- Show offline emergency guidance
- Mark the order as urgent for visibility
- Do not pretend the app can solve the emergency online

### 7.4 DDI high-risk warning

#### 7.4.1 Pharmacist-side (P0)

Applies to:

- Pharmacist Web

Behavior:

- Strong warning appears during prescription confirm or plan save
- Pharmacist must confirm before final save
- Confirmation is recorded in screening log

#### 7.4.2 Patient-side DDI alert (P0+ enhancement)

Applies to:

- Patient App

Trigger:

- Patient uploads a new prescription
- Patient submits a new self-reported medication via `/self-reported-medications`

Behavior:

1. Before submission, system calls `POST /app-api/ddi/screen` with the new drug names
2. If alerts are returned (HIGH or MEDIUM severity):
   - Show alert dialog listing each interaction/contraindication
   - Each alert shows: drug pair or drug-condition pair, severity badge, warning message, recommended action
   - HIGH severity alerts are shown in red
   - MEDIUM severity alerts are shown in orange
3. User can choose:
   - "已知晓，继续提交" (Acknowledged, continue) — proceeds with submission
   - "取消，咨询药师" (Cancel, consult pharmacist) — returns to the form and optionally opens help order creation
4. If no alerts, submission proceeds normally without interruption

#### 7.4.3 Pharmacist DDI rule management (P0+ enhancement)

Applies to:

- Pharmacist Web

New route: `/ddi/rules`

Purpose:

- View, create, and edit DDI rules and drug contraindication rules

Trigger:

- Pharmacist opens DDI rule management from the navigation menu

Success:

- Rules are listed in a searchable table (drug name, severity filter)
- Pharmacist can create new rules or edit existing ones
- Pharmacist can download header-only CSV templates
- Pharmacist can upload CSV, preview row-level create/update/error/warning results, then apply only when there are no errors
- Changes take effect immediately for subsequent screenings

#### 7.4.4 Reviewed education content management (T4)

Applies to:

- Pharmacist Web

New route: `/education/content`

Purpose:

- Import and review structured patient education cards without inventing medical content in the app

Success:

- Pharmacist lists cards by keyword, category, and review status
- CSV import previews row-level results and applies overwrite by `contentCode`
- Only `REVIEWED` cards are returned to the patient app

### 7.5 NEW_MEDICATION_REVIEW work order handling (pharmacist-side)

Applies to:

- Pharmacist Web

Trigger:

- Patient submits a self-reported medication
- Pharmacist receives a `NEW_MEDICATION_REVIEW` category work order notification

Behavior (in Help Order Detail `OrderDetail`):

1. `OrderDetail` detects `category = NEW_MEDICATION_REVIEW` and embeds `SelfReportedMedicationsPanel`
2. Panel displays: drug name, dosage, notes, submission time, and any DDI alerts from the original screening
3. Pharmacist chooses one of three actions:

   **Action A: Acknowledge** (confirm receipt)
   - Call `POST /admin-api/help-orders/{id}/acknowledge`
   - Self-reported medication status → `ACTIVE`
   - Drug is now included in DDI screening background for this patient

   **Action B: Reject**
   - Call `POST /admin-api/help-orders/{id}/reject-medication` with reason
   - Self-reported medication status → `STOPPED`
   - Rejection reason is stored and work order is closed

   **Action C: Transfer to plan**
   - Call `GET /admin-api/help-orders/{id}/transfer-prefill` to get prefill data
   - Open or navigate to `MedicationPlanPanel` with prefilled drug/dosage
   - Pharmacist completes plan item details and activates plan
   - Call `POST /admin-api/help-orders/{id}/mark-transferred`
   - Self-reported medication status → `TRANSFERRED`

DDI check before plan activation:

- Before pharmacist activates a plan, `MedicationPlanPanel` calls `GET /admin-api/medication-plans/{id}/ddi-check`
- If alerts exist, show confirmation dialog: pharmacist must acknowledge before activating

### 7.7 OCR low confidence

Applies to:

- Pharmacist Web

Behavior:

- Keep the original image
- Switch to manual template entry

## 8. Empty states

### 8.1 Patient App

- No plan: show waiting for pharmacist plan
- No reminders today: show today is complete
- No records: show first-use guidance
- No help orders: show entry to create the first help order

### 8.2 Pharmacist Web

- No assigned patients: show onboarding action
- No help orders: show queue is clear
- No prescription uploads: show empty upload review state

### 8.3 Doctor Web

- No escalated cases: show no pending doctor work

## 9. Out-of-flow screens

These screens exist in code or legacy navigation, but they are not part of the MVP critical path:

- Patient education expansion flows beyond basic medication guidance
- Full legacy admin menus in pharmacist web
- Generic doctor full-access management pages
- Chat as a primary MVP dependency

They must not block the main trial workflow.

## 10. MVP critical path checklist

The following path must work end to end:

1. Patient login
2. Patient binding
3. Home loads
4. Reminder opens
5. Medication record is submitted
6. Help order can be submitted
7. Pharmacist receives the order
8. Pharmacist handles or escalates it
9. Doctor handles escalated cases
10. Patient sees the final outcome
