import { chromium } from 'playwright'

const runId = Date.now().toString().slice(-6)
const baseUrl = process.env.E2E_BASE_URL || 'http://127.0.0.1:3001'
const mobile = process.env.E2E_MOBILE || `138${runId.padStart(8, '0')}`
const hospitalPatientId = process.env.E2E_HOSPITAL_PATIENT_ID || `HZY-REFRESH-${runId}`
const patientName = process.env.E2E_PATIENT_NAME || `Refresh Patient ${runId}`

const browser = await chromium.launch({ headless: true })
const context = await browser.newContext({
  viewport: { width: 430, height: 932 }
})

const page = await context.newPage()

try {
  await page.goto(`${baseUrl}/login`, { waitUntil: 'networkidle' })

  await page.evaluate(() => {
    localStorage.clear()
    sessionStorage.clear()
  })
  await page.reload({ waitUntil: 'networkidle' })

  await page.locator('ion-input#mobile input').waitFor()
  await page.locator('ion-input#mobile input').fill(mobile)
  await page.locator('.otp-button').click()
  await page.waitForTimeout(1200)

  await page.locator('ion-input#otpCode input').fill('123456')
  await page.locator('.login-button').click()

  await page.waitForURL(/\/realname$/, { timeout: 15000 })
  await page.locator('ion-input#name input').waitFor()
  await page.locator('ion-input#name input').fill(patientName)
  await page.locator('ion-input#mobile input').fill(mobile)
  await page.locator('ion-input#hospitalPatientId input').fill(hospitalPatientId)
  await page.locator('.submit-btn').click()

  await page.waitForURL(/\/tabs\/home$/, { timeout: 15000 })

  const beforeRefresh = await page.evaluate(() => ({
    accessToken: localStorage.getItem('patient_access_token'),
    refreshToken: localStorage.getItem('patient_refresh_token'),
    deviceId: localStorage.getItem('patient_device_id')
  }))

  await page.evaluate(() => {
    localStorage.setItem('patient_access_token', 'invalid-access-token-for-refresh-probe')
  })

  const refreshProbe = await page.evaluate(async () => {
    const runtimeWindow = window
    const devHelper = runtimeWindow.__patientDev
    if (!devHelper) {
      return { ok: false, error: 'window.__patientDev is not available' }
    }

    try {
      const response = await devHelper.getCurrentAccount()
      return {
        ok: true,
        code: response.code,
        bound: response.data?.bound ?? null,
        mobile: response.data?.mobile ?? null
      }
    } catch (error) {
      return {
        ok: false,
        error: error?.message || String(error)
      }
    }
  })

  const afterRefresh = await page.evaluate(() => ({
    accessToken: localStorage.getItem('patient_access_token'),
    refreshToken: localStorage.getItem('patient_refresh_token'),
    deviceId: localStorage.getItem('patient_device_id')
  }))

  console.log(
    JSON.stringify({
      success: true,
      finalUrl: page.url(),
      mobile,
      hospitalPatientId,
      refreshProbe,
      accessTokenRotated: Boolean(beforeRefresh.accessToken && afterRefresh.accessToken && beforeRefresh.accessToken !== afterRefresh.accessToken),
      refreshTokenRotated: Boolean(beforeRefresh.refreshToken && afterRefresh.refreshToken && beforeRefresh.refreshToken !== afterRefresh.refreshToken),
      beforeRefresh,
      afterRefresh
    })
  )
} finally {
  await context.close()
  await browser.close()
}
