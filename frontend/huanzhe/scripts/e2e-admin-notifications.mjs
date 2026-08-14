import { chromium } from 'playwright'
import {
  adminHeaders,
  appHeaders,
  createPatientIdentity,
  createPatientSession,
  createRunId,
  primeProfessionalPage,
  professionalLogin,
  requestJson
} from './e2e-shared.mjs'

const runId = createRunId()
const backendBaseUrl = process.env.E2E_BACKEND_BASE_URL || 'http://127.0.0.1:8080'
const legacyBaseUrl = process.env.E2E_LEGACY_BASE_URL || backendBaseUrl
const pharmacistBaseUrl = process.env.E2E_PHARMACIST_BASE_URL || 'http://127.0.0.1:3003'
const doctorBaseUrl = process.env.E2E_DOCTOR_BASE_URL || 'http://127.0.0.1:3002'
const pharmacistUsername = process.env.E2E_PHARMACIST_USERNAME || 'pharmacist'
const pharmacistPassword = process.env.E2E_PHARMACIST_PASSWORD || '123456'
const doctorUsername = process.env.E2E_DOCTOR_USERNAME || 'doctor'
const doctorPassword = process.env.E2E_DOCTOR_PASSWORD || '123456'
const patientIdentity = createPatientIdentity(runId, 'Realtime Patient')

const browser = await chromium.launch({ headless: true })
const pharmacistContext = await browser.newContext({ viewport: { width: 1440, height: 960 } })
const doctorContext = await browser.newContext({ viewport: { width: 1440, height: 960 } })
const pharmacistPage = await pharmacistContext.newPage()
const doctorPage = await doctorContext.newPage()

try {
  const patientSession = await createPatientSession(backendBaseUrl, patientIdentity)
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

  await primeProfessionalPage(pharmacistPage, pharmacistBaseUrl, pharmacistSession)
  await pharmacistPage.goto(`${pharmacistBaseUrl}/workorders`, { waitUntil: 'networkidle' })
  await primeProfessionalPage(doctorPage, doctorBaseUrl, doctorSession)
  await doctorPage.goto(`${doctorBaseUrl}/workorders`, { waitUntil: 'networkidle' })
  await pharmacistPage.waitForTimeout(1500)
  await doctorPage.waitForTimeout(1500)

  const orderTitle = `Realtime order ${runId}`
  const createdOrder = await requestJson(`${backendBaseUrl}/app-api/help-orders`, {
    method: 'POST',
    headers: appHeaders(patientSession.accessToken),
    body: {
      category: 'MEDICATION_CONSULTATION',
      title: orderTitle,
      description: `Realtime notification smoke ${runId}`,
      priority: 'NORMAL',
      requiresOfflineEmergency: false
    }
  })

  const pharmacistRow = pharmacistPage.locator('.queue-card').filter({ hasText: orderTitle }).first()
  await pharmacistRow.waitFor({ timeout: 15000 })

  const escalatedOrder = await requestJson(`${backendBaseUrl}/admin-api/help-orders/${createdOrder.payload.data.id}/escalate`, {
    method: 'POST',
    headers: adminHeaders(pharmacistSession.accessToken),
    body: {
      note: 'Realtime escalate'
    }
  })

  const doctorRow = doctorPage.locator('.queue-card').filter({ hasText: orderTitle }).first()
  await doctorRow.waitFor({ timeout: 15000 })

  const doctorReply = await requestJson(`${backendBaseUrl}/admin-api/help-orders/${createdOrder.payload.data.id}/reply`, {
    method: 'POST',
    headers: adminHeaders(doctorSession.accessToken),
    body: {
      content: 'Realtime doctor reply'
    }
  })

  const doctorClose = await requestJson(`${backendBaseUrl}/admin-api/help-orders/${createdOrder.payload.data.id}/close`, {
    method: 'POST',
    headers: adminHeaders(doctorSession.accessToken),
    body: {
      note: 'Realtime closed'
    }
  })

  const closedNotifications = await requestJson(
    `${backendBaseUrl}/admin-api/notifications/logs?businessType=HELP_ORDER_CLOSED&pageNo=1&pageSize=20`,
    {
      headers: adminHeaders(pharmacistSession.accessToken)
    }
  )

  console.log(
    JSON.stringify({
      success: true,
      orderId: createdOrder.payload.data.id,
      orderNo: createdOrder.payload.data.orderNo,
      pharmacistRowVisible: await pharmacistRow.isVisible(),
      doctorRowVisible: await doctorRow.isVisible(),
      escalatedStatus: escalatedOrder.payload.data.status,
      doctorReplyStatus: doctorReply.payload.data.status,
      doctorCloseStatus: doctorClose.payload.data.status,
      closedNotificationStatus: closedNotifications.payload.data.list?.[0]?.deliveryStatus || null
    })
  )
} finally {
  await pharmacistContext.close()
  await doctorContext.close()
  await browser.close()
}
