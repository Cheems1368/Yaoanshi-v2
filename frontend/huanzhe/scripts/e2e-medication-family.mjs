import { chromium } from 'playwright'
import {
  adminHeaders,
  appHeaders,
  createFamilyIdentity,
  createFamilySession,
  createPatientIdentity,
  createPatientSession,
  createRunId,
  expectCommonResultFailure,
  primePatientPage,
  professionalLogin,
  requestJson
} from './e2e-shared.mjs'

const runId = createRunId()
const patientBaseUrl = process.env.E2E_PATIENT_BASE_URL || 'http://127.0.0.1:3001'
const pharmacistBaseUrl = process.env.E2E_PHARMACIST_BASE_URL || 'http://127.0.0.1:3003'
const backendBaseUrl = process.env.E2E_BACKEND_BASE_URL || 'http://127.0.0.1:8080'
const legacyBaseUrl = process.env.E2E_LEGACY_BASE_URL || 'http://127.0.0.1:48080'
const pharmacistUsername = process.env.E2E_PHARMACIST_USERNAME || 'pharmacist'
const pharmacistPassword = process.env.E2E_PHARMACIST_PASSWORD || '123456'
const patientIdentity = createPatientIdentity(runId, 'Medication Patient')
const familyIdentity = createFamilyIdentity(runId)

const browser = await chromium.launch({ headless: true })
const patientContext = await browser.newContext({ viewport: { width: 430, height: 932 } })
const familyContext = await browser.newContext({ viewport: { width: 430, height: 932 } })
const patientPage = await patientContext.newPage()
const familyPage = await familyContext.newPage()

async function waitForCondition(label, reader, predicate, { attempts = 20, delayMs = 500 } = {}) {
  let lastValue
  for (let attempt = 0; attempt < attempts; attempt += 1) {
    lastValue = await reader()
    if (predicate(lastValue)) {
      return lastValue
    }
    await new Promise((resolve) => setTimeout(resolve, delayMs))
  }
  throw new Error(`Timed out waiting for ${label}: ${JSON.stringify(lastValue)}`)
}

try {
  const patientSession = await createPatientSession(backendBaseUrl, patientIdentity)
  const patientMe = await requestJson(`${backendBaseUrl}/app-api/patients/me`, {
    headers: appHeaders(patientSession.accessToken)
  })
  const patientId = patientMe.payload.data.id

  const pharmacistSession = await professionalLogin(legacyBaseUrl, {
    username: pharmacistUsername,
    password: pharmacistPassword,
    role: 'pharmacist'
  })

  const plan = await requestJson(`${backendBaseUrl}/admin-api/medication-plans`, {
    method: 'POST',
    headers: adminHeaders(pharmacistSession.accessToken),
    body: {
      patientId,
      planName: `Medication Plan ${runId}`,
      status: 'ACTIVE',
      sourceType: 'MANUAL',
      effectiveFrom: new Date().toISOString(),
      notes: 'Playwright medication flow',
      items: [
        {
          drugName: 'Amlodipine',
          dosage: '5mg',
          frequencyCode: 'BID',
          timePoints: ['08:00', '20:00'],
          mealRelation: 'AFTER_MEAL',
          route: 'PO',
          instructions: 'Take with water'
        }
      ]
    }
  })

  const today = new Date().toISOString().slice(0, 10)
  const planTasks = await requestJson(
    `${backendBaseUrl}/admin-api/medication-plans/${plan.payload.data.id}/tasks?dateFrom=${today}&dateTo=${today}`,
    {
      headers: adminHeaders(pharmacistSession.accessToken)
    }
  )
  const firstTaskId = planTasks.payload.data[0].id
  const secondTaskId = planTasks.payload.data[1].id

  await primePatientPage(patientPage, patientBaseUrl, patientSession, { bound: true, hasActivePlan: true })
  await patientPage.goto(`${patientBaseUrl}/medication-reminder`, { waitUntil: 'networkidle' })
  const taskCard = patientPage.locator('.task-card').first()
  await taskCard.waitFor()

  await Promise.all([
    patientPage.waitForResponse(
      (response) => response.url().includes('/app-api/checkins') && response.request().method() === 'POST'
    ),
    taskCard.locator('ion-button[color="success"]').click()
  ])
  const afterTaken = await waitForCondition(
    'taken task completion',
    async () =>
      requestJson(`${backendBaseUrl}/app-api/dose-tasks/today`, {
        headers: appHeaders(patientSession.accessToken)
      }),
    (result) => {
      const task = result.payload.data.find((item) => item.id === firstTaskId)
      return task?.status === 'COMPLETED' && Boolean(task.lastEffectiveCheckinId)
    }
  )
  const takenTask = afterTaken.payload.data.find((task) => task.id === firstTaskId)

  await Promise.all([
    patientPage.waitForResponse(
      (response) =>
        response.url().includes(`/app-api/checkins/${takenTask.lastEffectiveCheckinId}/correct`) &&
        response.request().method() === 'PUT'
    ),
    patientPage.locator('.task-card').first().locator('ion-button[color="warning"]').click()
  ])
  const afterCorrection = await waitForCondition(
    'corrected checkin',
    async () =>
      requestJson(`${backendBaseUrl}/app-api/checkins?pageNo=1&pageSize=20`, {
        headers: appHeaders(patientSession.accessToken)
      }),
    (result) =>
      result.payload.data.list.some(
        (record) =>
          record.doseTaskId === firstTaskId &&
          record.effective === true &&
          record.result === 'MISSED' &&
          record.correction === true
      )
  )
  const correctedRecord = afterCorrection.payload.data.list.find(
    (record) => record.doseTaskId === firstTaskId && record.effective === true
  )

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
  await primePatientPage(familyPage, patientBaseUrl, familySession, { bound: true, hasActivePlan: true })
  await familyPage.goto(`${patientBaseUrl}/medication-reminder`, { waitUntil: 'networkidle' })
  const familyTaskCard = familyPage.locator('.task-card').last()
  await familyTaskCard.waitFor()
  await Promise.all([
    familyPage.waitForResponse(
      (response) => response.url().includes('/app-api/checkins') && response.request().method() === 'POST'
    ),
    familyTaskCard.locator('ion-button[color="success"]').click()
  ])

  const familyCheckins = await waitForCondition(
    'family delegated checkin',
    async () =>
      requestJson(`${backendBaseUrl}/app-api/checkins?pageNo=1&pageSize=20`, {
        headers: appHeaders(familySession.accessToken)
      }),
    (result) =>
      result.payload.data.list.some(
        (record) => record.doseTaskId === secondTaskId && record.effective === true && record.operatorType === 'FAMILY'
      )
  )
  const familyRecord = familyCheckins.payload.data.list.find(
    (record) => record.doseTaskId === secondTaskId && record.effective === true
  )

  await requestJson(`${backendBaseUrl}/app-api/patients/me/family-bindings/${familyBinding.payload.data.id}`, {
    method: 'DELETE',
    headers: appHeaders(patientSession.accessToken)
  })

  const afterUnbind = await expectCommonResultFailure(
    () =>
      requestJson(`${backendBaseUrl}/app-api/dose-tasks/today`, {
        headers: appHeaders(familySession.accessToken),
        allowFailure: true
      }),
    'PATIENT_BIND_CONFLICT'
  )

  console.log(
    JSON.stringify({
      success: true,
      backendBaseUrl,
      legacyBaseUrl,
      planId: plan.payload.data.id,
      patientId,
      taskCount: planTasks.payload.data.length,
      taskStatusAfterTaken: takenTask?.status,
      lastEffectiveCheckinIdAfterTaken: takenTask?.lastEffectiveCheckinId,
      correctedRecordId: correctedRecord.id,
      correctedRecordResult: correctedRecord.result,
      correctedRecordCorrection: correctedRecord.correction,
      correctedRecordEffective: correctedRecord.effective,
      familyBindingId: familyBinding.payload.data.id,
      familyOperatorType: familyRecord?.operatorType,
      familyTaskId: secondTaskId,
      familyAccessAfterUnbindCode: afterUnbind.code,
      familyAccessAfterUnbindMessage: afterUnbind.msg
    })
  )
} finally {
  await patientContext.close()
  await familyContext.close()
  await browser.close()
}
