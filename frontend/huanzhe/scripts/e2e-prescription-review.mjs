import { chromium } from 'playwright'
import {
  adminHeaders,
  createPatientIdentity,
  createPatientSession,
  createPrescriptionUpload,
  createRunId,
  expectCommonResultFailure,
  primePatientPage,
  primeProfessionalPage,
  professionalLogin,
  requestJson,
  tinyPngBuffer
} from './e2e-shared.mjs'

const runId = createRunId()
const patientBaseUrl = process.env.E2E_PATIENT_BASE_URL || 'http://127.0.0.1:3001'
const pharmacistBaseUrl = process.env.E2E_PHARMACIST_BASE_URL || 'http://127.0.0.1:3003'
const backendBaseUrl = process.env.E2E_BACKEND_BASE_URL || 'http://127.0.0.1:8080'
const legacyBaseUrl = process.env.E2E_LEGACY_BASE_URL || 'http://127.0.0.1:48080'
const pharmacistUsername = process.env.E2E_PHARMACIST_USERNAME || 'pharmacist'
const pharmacistPassword = process.env.E2E_PHARMACIST_PASSWORD || '123456'
const patientIdentity = createPatientIdentity(runId, 'Prescription Patient')

const browser = await chromium.launch({ headless: true })
const patientContext = await browser.newContext({ viewport: { width: 430, height: 932 } })
const pharmacistContext = await browser.newContext({ viewport: { width: 1440, height: 960 } })
const patientPage = await patientContext.newPage()
const pharmacistPage = await pharmacistContext.newPage()

try {
  const patientSession = await createPatientSession(backendBaseUrl, patientIdentity)
  await primePatientPage(patientPage, patientBaseUrl, patientSession, { bound: true, hasActivePlan: false })
  await patientPage.goto(`${patientBaseUrl}/tabs/prescription`, { waitUntil: 'networkidle' })

  const uploadResponsePromise = patientPage.waitForResponse((response) => response.url().includes('/app-api/prescriptions/uploads'))
  await patientPage.locator('input[type="file"]').setInputFiles({
    name: `prescription-${runId}.png`,
    mimeType: 'image/png',
    buffer: tinyPngBuffer
  })
  await patientPage.locator('.selected-file ion-button').click()

  const uploadResponse = await uploadResponsePromise
  const uploadedPayload = await uploadResponse.json()
  const uploadedPrescriptionId = uploadedPayload.data.id
  await patientPage.locator('.record-card').first().waitFor()

  const pharmacistSession = await professionalLogin(legacyBaseUrl, {
    username: pharmacistUsername,
    password: pharmacistPassword,
    role: 'pharmacist'
  })

  const pendingConfirm = await expectCommonResultFailure(
    () =>
      requestJson(`${backendBaseUrl}/admin-api/prescriptions/${uploadedPrescriptionId}/confirm`, {
        method: 'POST',
        headers: adminHeaders(pharmacistSession.accessToken),
        body: {
          items: [
            { drugName: 'Aspirin', dosage: '100mg', frequency: 'QD', route: 'PO', durationDays: 7 },
            { drugName: 'Warfarin', dosage: '2.5mg', frequency: 'QD', route: 'PO', durationDays: 7 }
          ]
        },
        allowFailure: true
      }),
    'DDI_CONFIRM_REQUIRED'
  )

  await primeProfessionalPage(pharmacistPage, pharmacistBaseUrl, pharmacistSession)
  await pharmacistPage.goto(`${pharmacistBaseUrl}/prescriptions`, { waitUntil: 'networkidle' })
  const prescriptionRow = pharmacistPage.locator('.el-table__row').filter({ hasText: String(uploadedPrescriptionId) }).first()
  await prescriptionRow.waitFor()
  await prescriptionRow.locator('.el-button').first().click()
  await pharmacistPage.locator('.el-drawer').last().waitFor()
  const ddiCards = pharmacistPage.locator('.ddi-card')
  await ddiCards.first().waitFor()

  const detailBeforeConfirm = await requestJson(`${backendBaseUrl}/admin-api/prescriptions/${uploadedPrescriptionId}`, {
    headers: adminHeaders(pharmacistSession.accessToken)
  })
  const screeningId = detailBeforeConfirm.payload.data.pendingDdiScreenings[0].id

  const ddiConfirm = await requestJson(`${backendBaseUrl}/admin-api/ddi/screenings/${screeningId}/confirm`, {
    method: 'POST',
    headers: adminHeaders(pharmacistSession.accessToken),
    body: {
      note: 'Playwright confirmed continue'
    }
  })

  const prescriptionConfirm = await requestJson(`${backendBaseUrl}/admin-api/prescriptions/${uploadedPrescriptionId}/confirm`, {
    method: 'POST',
    headers: adminHeaders(pharmacistSession.accessToken),
    body: {
      items: [
        { drugName: 'Aspirin', dosage: '100mg', frequency: 'QD', route: 'PO', durationDays: 7 },
        { drugName: 'Warfarin', dosage: '2.5mg', frequency: 'QD', route: 'PO', durationDays: 7 }
      ]
    }
  })

  const rejectedPrescription = await createPrescriptionUpload(backendBaseUrl, patientSession, `reject-${runId}.png`)
  const rejectResult = await requestJson(`${backendBaseUrl}/admin-api/prescriptions/${rejectedPrescription.id}/reject`, {
    method: 'POST',
    headers: adminHeaders(pharmacistSession.accessToken),
    body: {
      reason: 'Image needs re-upload'
    }
  })

  const rejectedDetail = await requestJson(`${backendBaseUrl}/admin-api/prescriptions/${rejectedPrescription.id}`, {
    headers: adminHeaders(pharmacistSession.accessToken)
  })

  console.log(
    JSON.stringify({
      success: true,
      patientBaseUrl,
      pharmacistBaseUrl,
      backendBaseUrl,
      legacyBaseUrl,
      uploadedPrescriptionId,
      pendingConfirmCode: pendingConfirm.code,
      pendingConfirmMessage: pendingConfirm.msg,
      ddiScreeningId: screeningId,
      ddiConfirmStatus: ddiConfirm.payload.data.confirmStatus,
      confirmedStatus: prescriptionConfirm.payload.data.status,
      rejectedPrescriptionId: rejectedPrescription.id,
      rejectedStatus: rejectResult.payload.data.status,
      rejectedReason: rejectedDetail.payload.data.rejectReason,
      visibleDdiCards: await ddiCards.count(),
      uploadedRecordVisible: await patientPage.locator('.record-card').count()
    })
  )
} finally {
  await patientContext.close()
  await pharmacistContext.close()
  await browser.close()
}
