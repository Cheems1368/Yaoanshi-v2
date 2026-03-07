import { chromium } from 'playwright'
import {
  adminHeaders,
  appHeaders,
  createFamilyIdentity,
  createFamilySession,
  createPatientIdentity,
  createPatientSession,
  createRunId,
  primePatientPage,
  primeProfessionalPage,
  professionalLogin,
  requestJson
} from './e2e-shared.mjs'

const runId = createRunId()
const patientBaseUrl = process.env.E2E_PATIENT_BASE_URL || 'http://127.0.0.1:3001'
const pharmacistBaseUrl = process.env.E2E_PHARMACIST_BASE_URL || 'http://127.0.0.1:3003'
const doctorBaseUrl = process.env.E2E_DOCTOR_BASE_URL || 'http://127.0.0.1:3002'
const backendBaseUrl = process.env.E2E_BACKEND_BASE_URL || 'http://127.0.0.1:8080'
const legacyBaseUrl = process.env.E2E_LEGACY_BASE_URL || 'http://127.0.0.1:48080'
const pharmacistUsername = process.env.E2E_PHARMACIST_USERNAME || 'pharmacist'
const pharmacistPassword = process.env.E2E_PHARMACIST_PASSWORD || '123456'
const doctorUsername = process.env.E2E_DOCTOR_USERNAME || 'doctor'
const doctorPassword = process.env.E2E_DOCTOR_PASSWORD || '123456'
const patientIdentity = createPatientIdentity(runId, 'HelpOrder Patient')
const familyIdentity = createFamilyIdentity(runId)

const browser = await chromium.launch({ headless: true })
const familyContext = await browser.newContext({ viewport: { width: 430, height: 932 } })
const pharmacistContext = await browser.newContext({ viewport: { width: 1440, height: 960 } })
const doctorContext = await browser.newContext({ viewport: { width: 1440, height: 960 } })
const familyPage = await familyContext.newPage()
const pharmacistPage = await pharmacistContext.newPage()
const doctorPage = await doctorContext.newPage()

try {
  const patientSession = await createPatientSession(backendBaseUrl, patientIdentity)
  const familyBinding = await requestJson(`${backendBaseUrl}/app-api/patients/me/family-bindings`, {
    method: 'POST',
    headers: appHeaders(patientSession.accessToken),
    body: {
      familyMobile: familyIdentity.mobile,
      familyDisplayName: `Family ${runId}`,
      relationshipType: 'SPOUSE'
    }
  })
  const familySession = await createFamilySession(backendBaseUrl, familyIdentity)

  const pharmacistSession = await professionalLogin(legacyBaseUrl, {
    username: pharmacistUsername,
    password: pharmacistPassword,
    role: 'pharmacist'
  })
  const doctorSession = await professionalLogin(legacyBaseUrl, {
    username: doctorUsername,
    password: doctorPassword,
    role: 'doctor'
  })

  await primePatientPage(familyPage, patientBaseUrl, familySession, { bound: true, hasActivePlan: false })
  await familyPage.goto(`${patientBaseUrl}/tabs/help-order`, { waitUntil: 'networkidle' })

  const titleInput = familyPage.locator('ion-input input').first()
  const descriptionInput = familyPage.locator('ion-textarea textarea').first()
  const submitButton = familyPage.locator('.form-card ion-button').first()
  await titleInput.fill(`Need advice ${runId}`)
  await descriptionInput.fill(`Family submitted help-order ${runId}`)
  const [orderResponse] = await Promise.all([
    familyPage.waitForResponse(
      (response) => response.url().includes('/app-api/help-orders') && response.request().method() === 'POST'
    ),
    submitButton.click()
  ])
  const createdOrder = (await orderResponse.json()).data

  await primeProfessionalPage(pharmacistPage, pharmacistBaseUrl, pharmacistSession)
  await pharmacistPage.goto(`${pharmacistBaseUrl}/workorders`, { waitUntil: 'networkidle' })
  const pharmacistRow = pharmacistPage.locator('.el-table__row').filter({ hasText: createdOrder.orderNo }).first()
  await pharmacistRow.waitFor()
  const rowButtons = pharmacistRow.locator('.el-button')
  if ((await rowButtons.count()) > 1) {
    await rowButtons.nth(1).click()
  }

  const claimDetail = await requestJson(`${backendBaseUrl}/admin-api/help-orders/${createdOrder.id}`, {
    headers: adminHeaders(pharmacistSession.accessToken)
  })
  const pharmacistReply = await requestJson(`${backendBaseUrl}/admin-api/help-orders/${createdOrder.id}/reply`, {
    method: 'POST',
    headers: adminHeaders(pharmacistSession.accessToken),
    body: {
      content: 'Pharmacist replied'
    }
  })
  const pharmacistEscalate = await requestJson(`${backendBaseUrl}/admin-api/help-orders/${createdOrder.id}/escalate`, {
    method: 'POST',
    headers: adminHeaders(pharmacistSession.accessToken),
    body: {
      note: 'Escalated to doctor'
    }
  })

  await primeProfessionalPage(doctorPage, doctorBaseUrl, doctorSession)
  await doctorPage.goto(`${doctorBaseUrl}/workorders`, { waitUntil: 'networkidle' })
  const doctorRow = doctorPage.locator('.el-table__row').filter({ hasText: createdOrder.orderNo }).first()
  await doctorRow.waitFor()
  await doctorRow.locator('.el-button').first().click()

  const doctorReply = await requestJson(`${backendBaseUrl}/admin-api/help-orders/${createdOrder.id}/reply`, {
    method: 'POST',
    headers: adminHeaders(doctorSession.accessToken),
    body: {
      content: 'Doctor replied'
    }
  })
  const doctorClose = await requestJson(`${backendBaseUrl}/admin-api/help-orders/${createdOrder.id}/close`, {
    method: 'POST',
    headers: adminHeaders(doctorSession.accessToken),
    body: {
      note: 'Closed by doctor'
    }
  })

  const knowledge = await requestJson(`${backendBaseUrl}/app-api/assessments/knowledge`, {
    method: 'POST',
    headers: appHeaders(familySession.accessToken),
    body: {
      phase: 'FOLLOW_UP',
      questionSetVersion: 'v1',
      answers: [
        {
          questionCode: 'Q1',
          questionText: 'Question 1',
          selectedOption: 'A',
          correctOption: 'A',
          correct: true
        }
      ]
    }
  })
  const satisfaction = await requestJson(`${backendBaseUrl}/app-api/assessments/satisfaction`, {
    method: 'POST',
    headers: appHeaders(familySession.accessToken),
    body: {
      helpOrderId: createdOrder.id,
      rating: 5,
      comment: 'Resolved'
    }
  })
  const notifications = await requestJson(
    `${backendBaseUrl}/admin-api/notifications/logs?businessType=HELP_ORDER_CLOSED&pageNo=1&pageSize=20`,
    {
      headers: adminHeaders(pharmacistSession.accessToken)
    }
  )

  console.log(
    JSON.stringify({
      success: true,
      backendBaseUrl,
      legacyBaseUrl,
      orderId: createdOrder.id,
      orderNo: createdOrder.orderNo,
      familyBindingId: familyBinding.payload.data.id,
      claimStatus: claimDetail.payload.data.status,
      pharmacistReplyStatus: pharmacistReply.payload.data.status,
      escalatedStatus: pharmacistEscalate.payload.data.status,
      doctorReplyStatus: doctorReply.payload.data.status,
      doctorCloseStatus: doctorClose.payload.data.status,
      knowledgeAssessmentId: knowledge.payload.data.id,
      satisfactionSurveyId: satisfaction.payload.data.id,
      closedNotificationCount: notifications.payload.data.total
    })
  )
} finally {
  await familyContext.close()
  await pharmacistContext.close()
  await doctorContext.close()
  await browser.close()
}
