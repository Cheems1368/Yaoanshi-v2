import { chromium } from 'playwright'
import {
  adminHeaders,
  appHeaders,
  createPatientIdentity,
  createPatientSession,
  createRunId,
  primePatientPage,
  primeProfessionalPage,
  professionalLogin,
  requestJson,
  tinyPngBuffer
} from './e2e-shared.mjs'

const runId = createRunId()
const patientBaseUrl = process.env.E2E_PATIENT_BASE_URL || 'http://127.0.0.1:3001'
const pharmacistBaseUrl = process.env.E2E_PHARMACIST_BASE_URL || 'http://127.0.0.1:3003'
const doctorBaseUrl = process.env.E2E_DOCTOR_BASE_URL || 'http://127.0.0.1:3002'
const backendBaseUrl = process.env.E2E_BACKEND_BASE_URL || 'http://127.0.0.1:8080'
const adminAuthBaseUrl = process.env.E2E_ADMIN_AUTH_BASE_URL || backendBaseUrl
const pharmacistUsername = process.env.E2E_PHARMACIST_USERNAME || 'pharmacist'
const pharmacistPassword = process.env.E2E_PHARMACIST_PASSWORD || '123456'
const doctorUsername = process.env.E2E_DOCTOR_USERNAME || 'doctor'
const doctorPassword = process.env.E2E_DOCTOR_PASSWORD || '123456'

const patientIdentity = createPatientIdentity(runId, 'Mobile HelpOrder Patient')
const orderTitle = `服药后皮下淤血 ${runId}`
const orderDescription = `患者反馈服药后出现皮下淤血，需要药师和医生协同判断 ${runId}`
const pharmacistReplyText = `药师建议先记录症状并升级医生确认 ${runId}`
const doctorDecisionText = `医生采纳药师建议，继续观察并按原方案复诊 ${runId}`
const patientContext = {
  diagnosis: `脑卒中随访合并高血压 ${runId}`,
  allergy: `青霉素过敏 ${runId}`,
  currentMedication: `苯磺酸氨氯地平片、阿司匹林肠溶片 ${runId}`,
  medicalHistory: `脑卒中后随访，血压需持续监测 ${runId}`
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(message)
  }
}

async function waitForImageLoaded(locator, label) {
  await locator.waitFor({ timeout: 15000 })
  await locator.evaluate(
    (image, assertionLabel) => {
      if (!(image instanceof HTMLImageElement)) {
        throw new Error(`${assertionLabel} is not an image`)
      }
      if (!image.complete || image.naturalWidth <= 0) {
        throw new Error(`${assertionLabel} did not load`)
      }
    },
    label
  )
}

async function waitForApiCondition(label, reader, predicate, { attempts = 30, delayMs = 500 } = {}) {
  let lastValue
  for (let attempt = 0; attempt < attempts; attempt += 1) {
    lastValue = await reader()
    if (predicate(lastValue)) {
      return lastValue
    }
    await new Promise((resolve) => setTimeout(resolve, delayMs))
  }
  throw new Error(`Timed out waiting for ${label}: ${JSON.stringify(lastValue?.payload ?? lastValue)}`)
}

async function seedPatientContext(patientSession, pharmacistSession) {
  const patientMe = await requestJson(`${backendBaseUrl}/app-api/patients/me`, {
    headers: appHeaders(patientSession.accessToken)
  })
  const patientId = patientMe.payload.data?.id
  assert(patientId, 'Current patient response missing id')

  await requestJson(`${backendBaseUrl}/admin-api/patients/${patientId}`, {
    method: 'PUT',
    headers: adminHeaders(pharmacistSession.accessToken),
    body: {
      gender: '男',
      age: 72,
      primaryDiseaseCode: 'STROKE_FOLLOW_UP',
      primaryDiseaseName: patientContext.diagnosis,
      drugAllergy: patientContext.allergy,
      foodAllergy: '未填写',
      medicalHistory: {
        layer2: {
          medicalHistory: patientContext.medicalHistory,
          currentMedication: patientContext.currentMedication
        }
      },
    }
  })

  return patientId
}

async function createHelpOrderFromPatientUi(page, session) {
  const params = new URLSearchParams({
    category: 'ADVERSE_REACTION',
    title: orderTitle,
    description: orderDescription
  })
  await primePatientPage(page, patientBaseUrl, session, { bound: true, hasActivePlan: false })
  await page.goto(`${patientBaseUrl}/tabs/help-order?${params.toString()}`, { waitUntil: 'networkidle' })
  await page.locator('ion-input input').first().waitFor()
  await page.locator('ion-input input').first().fill(orderTitle)
  await page.locator('ion-textarea textarea').first().fill(orderDescription)

  await page.locator('input.file-input').nth(1).setInputFiles({
    name: `help-order-${runId}.png`,
    mimeType: 'image/png',
    buffer: tinyPngBuffer
  })
  await waitForImageLoaded(page.locator('.image-preview img').first(), 'patient selected help-order image')

  const [orderResponse] = await Promise.all([
    page.waitForResponse(
      (response) => response.url().includes('/app-api/help-orders') && response.request().method() === 'POST'
    ),
    page.locator('.form-card ion-button').filter({ hasText: '提交工单' }).click()
  ])
  const payload = await orderResponse.json()
  assert(payload.code === 200 || payload.code === 0, payload.msg || 'Create help order failed')
  const createdOrder = payload.data
  assert(createdOrder?.id, 'Create help order response missing id')

  await page.waitForURL(new RegExp(`/help-order/${createdOrder.id}$`), { timeout: 15000 })
  await page.locator('.summary-title').filter({ hasText: orderTitle }).waitFor({ timeout: 15000 })
  await waitForImageLoaded(page.locator('.event-attachment img').first(), 'patient detail submitted attachment')
  return createdOrder
}

async function verifyPharmacistMobileReply(page, session, orderId) {
  await primeProfessionalPage(page, pharmacistBaseUrl, session)
  await page.goto(`${pharmacistBaseUrl}/mobile/workorders`, { waitUntil: 'networkidle' })
  await page.locator('.order-card').filter({ hasText: orderTitle }).first().click()
  await page.locator('.detail-summary').filter({ hasText: orderTitle }).waitFor({ timeout: 15000 })
  await waitForImageLoaded(page.locator('.attachment-grid img').first(), 'pharmacist mobile attachment')

  await page.locator('.reply-box textarea').fill(pharmacistReplyText)
  await Promise.all([
    page.waitForResponse(
      (response) =>
        response.url().includes(`/admin-api/help-orders/${orderId}/reply`) &&
        response.request().method() === 'POST'
    ),
    page.locator('.reply-box button').filter({ hasText: '回复患者' }).click()
  ])

  await page.locator('.timeline-content').filter({ hasText: pharmacistReplyText }).waitFor({ timeout: 15000 })
}

async function verifyDoctorMobileDecision(page, session) {
  await primeProfessionalPage(page, doctorBaseUrl, session)
  await page.goto(`${doctorBaseUrl}/mobile/workorders`, { waitUntil: 'networkidle' })
  await page.locator('.order-card').filter({ hasText: orderTitle }).first().click()
  await page.locator('.detail-summary').filter({ hasText: orderTitle }).waitFor({ timeout: 15000 })
  await page.locator('.patient-context').filter({ hasText: patientContext.diagnosis }).waitFor({ timeout: 15000 })
  await page.locator('.patient-context').filter({ hasText: patientContext.allergy }).waitFor({ timeout: 15000 })
  await page.locator('.patient-context').filter({ hasText: patientContext.currentMedication }).waitFor({ timeout: 15000 })
  await page.locator('.patient-context').filter({ hasText: patientContext.medicalHistory }).waitFor({ timeout: 15000 })
  await waitForImageLoaded(page.locator('.attachment-grid img').first(), 'doctor mobile attachment')

  await page.locator('.decision-box textarea').fill(doctorDecisionText)
  await Promise.all([
    page.waitForResponse(
      (response) => response.url().includes('/doctor-decision') && response.request().method() === 'POST'
    ),
    page.locator('.decision-box button').filter({ hasText: '提交医生决策' }).click()
  ])
  await page.locator('.terminal-note').filter({ hasText: '工单已关闭' }).waitFor({ timeout: 15000 })
  await page.locator('.timeline-content').filter({ hasText: doctorDecisionText }).waitFor({ timeout: 15000 })
  await page.locator('.timeline-item').filter({ hasText: '采纳建议' }).waitFor({ timeout: 15000 })
}

async function verifyPatientFinalResult(page, orderId) {
  await page.goto(`${patientBaseUrl}/help-order/${orderId}`, { waitUntil: 'networkidle' })
  await page.locator('.terminal-banner').filter({ hasText: '工单已关闭' }).waitFor({ timeout: 15000 })
  await page.locator('.summary-notice').filter({ hasText: doctorDecisionText }).waitFor({ timeout: 15000 })
  await page.locator('.event-content').filter({ hasText: doctorDecisionText }).waitFor({ timeout: 15000 })
}

const browser = await chromium.launch({ headless: true })
const patientContextBrowser = await browser.newContext({ viewport: { width: 430, height: 932 } })
const pharmacistContextBrowser = await browser.newContext({ viewport: { width: 430, height: 932 } })
const doctorContextBrowser = await browser.newContext({ viewport: { width: 430, height: 932 } })
const patientPage = await patientContextBrowser.newPage()
const pharmacistPage = await pharmacistContextBrowser.newPage()
const doctorPage = await doctorContextBrowser.newPage()

try {
  const patientSession = await createPatientSession(backendBaseUrl, patientIdentity)

  const pharmacistSession = await professionalLogin(adminAuthBaseUrl, {
    username: pharmacistUsername,
    password: pharmacistPassword,
    role: 'pharmacist'
  })
  const doctorSession = await professionalLogin(adminAuthBaseUrl, {
    username: doctorUsername,
    password: doctorPassword,
    role: 'doctor'
  })
  const patientId = await seedPatientContext(patientSession, pharmacistSession)

  const createdOrder = await createHelpOrderFromPatientUi(patientPage, patientSession)
  await verifyPharmacistMobileReply(pharmacistPage, pharmacistSession, createdOrder.id)
  await patientPage.locator('.event-content').filter({ hasText: pharmacistReplyText }).waitFor({ timeout: 15000 })

  const escalated = await requestJson(`${backendBaseUrl}/admin-api/help-orders/${createdOrder.id}/escalate`, {
    method: 'POST',
    headers: adminHeaders(pharmacistSession.accessToken),
    body: {
      note: `移动验证升级医生 ${runId}`
    }
  })
  assert(escalated.payload.data.status === 'WAITING_DOCTOR', 'Escalated order should be WAITING_DOCTOR')

  await verifyDoctorMobileDecision(doctorPage, doctorSession)

  const finalDetail = await waitForApiCondition(
    'closed help-order detail',
    () =>
      requestJson(`${backendBaseUrl}/app-api/help-orders/${createdOrder.id}`, {
        headers: appHeaders(patientSession.accessToken)
      }),
    (result) =>
      result.payload.data.status === 'CLOSED' &&
      result.payload.data.serviceNotice === doctorDecisionText &&
      result.payload.data.events?.some(
        (event) => event.eventType === 'DOCTOR_DECISION' && event.decisionResult === 'ADOPTED'
      )
  )
  await verifyPatientFinalResult(patientPage, createdOrder.id)

  const submittedEvent = finalDetail.payload.data.events.find((event) => event.eventType === 'SUBMITTED')
  const doctorDecisionEvent = finalDetail.payload.data.events.find((event) => event.eventType === 'DOCTOR_DECISION')

  console.log(
    JSON.stringify({
      success: true,
      backendBaseUrl,
      patientBaseUrl,
      pharmacistBaseUrl,
      doctorBaseUrl,
      orderId: createdOrder.id,
      orderNo: finalDetail.payload.data.orderNo,
      patientId,
      attachmentCount: submittedEvent?.attachments?.length || 0,
      pharmacistReplyVisible: finalDetail.payload.data.events.some((event) => event.content === pharmacistReplyText),
      decisionResult: doctorDecisionEvent?.decisionResult,
      finalStatus: finalDetail.payload.data.status,
      patientFinalResultVisible: finalDetail.payload.data.serviceNotice === doctorDecisionText
    })
  )
} finally {
  await patientContextBrowser.close()
  await pharmacistContextBrowser.close()
  await doctorContextBrowser.close()
  await browser.close()
}
