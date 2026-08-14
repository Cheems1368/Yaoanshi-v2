import { chromium } from 'playwright'

const runId = Date.now().toString().slice(-6)
const baseUrl = process.env.E2E_BASE_URL || 'http://127.0.0.1:3001'
const waitForExpiryMs = Number(process.env.E2E_ACCESS_EXPIRY_WAIT_MS || '4000')
const mobile = process.env.E2E_MOBILE || `138${runId.padStart(8, '0')}`
const hospitalPatientId = process.env.E2E_HOSPITAL_PATIENT_ID || `HZY-EXPIRY-${runId}`
const patientName = process.env.E2E_PATIENT_NAME || `Expiry Patient ${runId}`

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
    refreshToken: sessionStorage.getItem('patient_refresh_token'),
    deviceId: localStorage.getItem('patient_device_id')
  }))

  await page.waitForTimeout(waitForExpiryMs)

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
    refreshToken: sessionStorage.getItem('patient_refresh_token'),
    deviceId: localStorage.getItem('patient_device_id'),
    bindingStatus: localStorage.getItem('patient_binding_status')
  }))

  await page.goto(`${baseUrl}/tabs/profile`, { waitUntil: 'networkidle' })
  await page.waitForURL(/\/tabs\/profile$/, { timeout: 15000 })
  await page.locator('.logout-button').waitFor()
  await page.locator('.logout-button').click()
  await page.waitForURL(/\/login$/, { timeout: 15000 })

  const afterLogout = await page.evaluate(() => ({
    refreshToken: sessionStorage.getItem('patient_refresh_token'),
    bindingStatus: localStorage.getItem('patient_binding_status')
  }))

  console.log(
    JSON.stringify({
      success: true,
      finalUrl: page.url(),
      mobile,
      hospitalPatientId,
      waitForExpiryMs,
      refreshProbe,
      refreshTokenRotated: Boolean(beforeRefresh.refreshToken && afterRefresh.refreshToken && beforeRefresh.refreshToken !== afterRefresh.refreshToken),
      beforeRefresh: {
        refreshTokenPresent: Boolean(beforeRefresh.refreshToken),
        deviceId: beforeRefresh.deviceId
      },
      afterRefresh: {
        refreshTokenPresent: Boolean(afterRefresh.refreshToken),
        deviceId: afterRefresh.deviceId,
        bindingStatus: afterRefresh.bindingStatus
      },
      afterLogout: {
        refreshTokenPresent: Boolean(afterLogout.refreshToken),
        bindingStatus: afterLogout.bindingStatus
      }
    })
  )
} finally {
  await context.close()
  await browser.close()
}
