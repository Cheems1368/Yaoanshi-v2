import { chromium } from 'playwright'
import {
  adminHeaders,
  appHeaders,
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
  const patientMe = await requestJson(`${backendBaseUrl}/app-api/patients/me`, {
    headers: appHeaders(patientSession.accessToken)
  })
  const patientId = patientMe.payload.data.id

  const pharmacistSession = await professionalLogin(legacyBaseUrl, {
    username: pharmacistUsername,
    password: pharmacistPassword,
    role: 'pharmacist'
  })

  await requestJson(`${backendBaseUrl}/admin-api/medication-plans`, {
    method: 'POST',
    headers: adminHeaders(pharmacistSession.accessToken),
    body: {
      patientId,
      planName: `DDI Upload Plan ${runId}`,
      status: 'ACTIVE',
      sourceType: 'MANUAL',
      effectiveFrom: new Date().toISOString(),
      notes: 'Playwright prescription DDI modal',
      items: [
        {
          drugName: 'Aspirin',
          dosage: '100mg',
          frequencyCode: 'QD',
          timePoints: ['08:00'],
          mealRelation: 'AFTER_MEAL',
          route: 'PO',
          instructions: 'Keep existing antiplatelet therapy'
        }
      ]
    }
  })

  await primePatientPage(patientPage, patientBaseUrl, patientSession, { bound: true, hasActivePlan: false })
  await patientPage.goto(`${patientBaseUrl}/tabs/prescription`, { waitUntil: 'networkidle' })

  await patientPage.locator('input[type="file"]').setInputFiles({
    name: `prescription-${runId}.png`,
    mimeType: 'image/png',
    buffer: tinyPngBuffer
  })
  await patientPage.locator('.drug-names-input').fill('Warfarin')
  await patientPage.locator('.upload-submit-button').click()
  await patientPage.locator('.ddi-modal').waitFor()
  await patientPage.locator('.severity-pill--high').first().waitFor()

  const uploadResponsePromise = patientPage.waitForResponse((response) => response.url().includes('/app-api/prescriptions/uploads'))
  await patientPage.locator('.ddi-modal ion-button').last().click()

  const uploadResponse = await uploadResponsePromise
  const uploadedPayload = await uploadResponse.json()
  const uploadedPrescriptionId = uploadedPayload.data.id
  await patientPage.locator('.record-card').first().waitFor()

  await patientPage.route('**/app-api/ddi/screen', (route) => route.abort())
  await patientPage.locator('input[type="file"]').setInputFiles({
    name: `prescription-degraded-${runId}.png`,
    mimeType: 'image/png',
    buffer: tinyPngBuffer
  })
  await patientPage.locator('.drug-names-input').fill('Warfarin')
  const degradedUploadPromise = patientPage.waitForResponse((response) => response.url().includes('/app-api/prescriptions/uploads'))
  await patientPage.locator('.upload-submit-button').click()
  const degradedUploadResponse = await degradedUploadPromise
  const degradedUploadPayload = await degradedUploadResponse.json()
  const degradedPrescriptionId = degradedUploadPayload.data.id
  await patientPage.unroute('**/app-api/ddi/screen')

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

  const detailWithScreening = await requestJson(`${backendBaseUrl}/admin-api/prescriptions/${uploadedPrescriptionId}`, {
    headers: adminHeaders(pharmacistSession.accessToken)
  })
  if (!detailWithScreening.payload.data.pendingDdiScreenings?.length) {
    throw new Error(`Prescription ${uploadedPrescriptionId} has no pending DDI screenings before UI assertion`)
  }

  await primeProfessionalPage(pharmacistPage, pharmacistBaseUrl, pharmacistSession)
  await pharmacistPage.goto(`${pharmacistBaseUrl}/prescriptions`, { waitUntil: 'networkidle' })
  const prescriptionIdCell = pharmacistPage.getByRole('cell', { name: String(uploadedPrescriptionId), exact: true }).first()
  const prescriptionRow = prescriptionIdCell.locator('xpath=ancestor::tr[1]')
  await prescriptionRow.waitFor()
  await prescriptionRow.locator('.el-button').first().click()
  await pharmacistPage.locator('.el-drawer').last().waitFor()
  const ddiCards = pharmacistPage.locator('.ddi-card')
  await ddiCards.first().waitFor()

  const screeningId = detailWithScreening.payload.data.pendingDdiScreenings[0].id
  const pharmacistActionSession = await professionalLogin(legacyBaseUrl, {
    username: pharmacistUsername,
    password: pharmacistPassword,
    role: 'pharmacist'
  })

  const ddiConfirm = await requestJson(`${backendBaseUrl}/admin-api/ddi/screenings/${screeningId}/confirm`, {
    method: 'POST',
    headers: adminHeaders(pharmacistActionSession.accessToken),
    body: {
      note: 'Playwright confirmed continue'
    }
  })

  const prescriptionConfirm = await requestJson(`${backendBaseUrl}/admin-api/prescriptions/${uploadedPrescriptionId}/confirm`, {
    method: 'POST',
    headers: adminHeaders(pharmacistActionSession.accessToken),
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
    headers: adminHeaders(pharmacistActionSession.accessToken),
    body: {
      reason: 'Image needs re-upload'
    }
  })

  const rejectedDetail = await requestJson(`${backendBaseUrl}/admin-api/prescriptions/${rejectedPrescription.id}`, {
    headers: adminHeaders(pharmacistActionSession.accessToken)
  })

  console.log(
    JSON.stringify({
      success: true,
      patientBaseUrl,
      pharmacistBaseUrl,
      backendBaseUrl,
      legacyBaseUrl,
      uploadedPrescriptionId,
      degradedPrescriptionId,
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
