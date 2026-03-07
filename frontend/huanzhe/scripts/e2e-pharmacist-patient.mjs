import { chromium } from 'playwright'
import { createPatientIdentity, createPatientSession, createRunId, primeProfessionalPage, professionalLogin } from './e2e-shared.mjs'

const runId = createRunId()
const backendBaseUrl = process.env.E2E_BACKEND_BASE_URL || 'http://127.0.0.1:8080'
const legacyBaseUrl = process.env.E2E_LEGACY_BASE_URL || 'http://127.0.0.1:48080'
const pharmacistBaseUrl = process.env.E2E_PHARMACIST_BASE_URL || 'http://127.0.0.1:3003'
const pharmacistUsername = process.env.E2E_PHARMACIST_USERNAME || 'pharmacist'
const pharmacistPassword = process.env.E2E_PHARMACIST_PASSWORD || '123456'
const patientIdentity = createPatientIdentity(runId, 'Pharmacist Smoke')
const diseaseName = `Hypertension ${runId}`

const browser = await chromium.launch({ headless: true })
const pharmacistContext = await browser.newContext({ viewport: { width: 1440, height: 960 } })
const pharmacistPage = await pharmacistContext.newPage()

try {
  await createPatientSession(backendBaseUrl, patientIdentity)

  const pharmacistSession = await professionalLogin(legacyBaseUrl, {
    username: pharmacistUsername,
    password: pharmacistPassword,
    role: 'pharmacist'
  })

  await primeProfessionalPage(pharmacistPage, pharmacistBaseUrl, pharmacistSession)
  await pharmacistPage.goto(`${pharmacistBaseUrl}/patient/list`, { waitUntil: 'networkidle' })

  await pharmacistPage.locator('.search-input input').fill(patientIdentity.hospitalPatientId)
  await pharmacistPage.locator('.patient-search-button').click()

  const patientRow = pharmacistPage.locator('.el-table__row').filter({ hasText: patientIdentity.hospitalPatientId }).first()
  await patientRow.waitFor()
  await patientRow.locator('.patient-detail-button').click()

  await pharmacistPage.waitForURL(/\/patient\/info/, { timeout: 15000 })
  await pharmacistPage.locator('.patient-primary-disease input').fill(diseaseName)
  await pharmacistPage.locator('.patient-bp-switch').click()
  await pharmacistPage.locator('.patient-save-button').click()

  await pharmacistPage.locator('.patient-back-button').click()
  await pharmacistPage.waitForURL(/\/patient\/list/, { timeout: 15000 })
  await pharmacistPage.locator('.search-input input').fill(patientIdentity.hospitalPatientId)
  await pharmacistPage.locator('.patient-search-button').click()

  const refreshedRow = pharmacistPage.locator('.el-table__row').filter({ hasText: diseaseName }).first()
  await refreshedRow.waitFor()

  console.log(
    JSON.stringify({
      success: true,
      patientName: patientIdentity.patientName,
      hospitalPatientId: patientIdentity.hospitalPatientId,
      diseaseName,
      currentUrl: pharmacistPage.url()
    })
  )
} finally {
  await pharmacistContext.close()
  await browser.close()
}
