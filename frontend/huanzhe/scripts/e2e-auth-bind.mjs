import { chromium } from 'playwright'

const runId = Date.now().toString().slice(-6)
const baseUrl = process.env.E2E_BASE_URL || 'http://127.0.0.1:3001'
const mobile = process.env.E2E_MOBILE || `138${runId.padStart(8, '0')}`
const hospitalPatientId = process.env.E2E_HOSPITAL_PATIENT_ID || `HZY-${runId}`
const patientName = process.env.E2E_PATIENT_NAME || `Smoke Patient ${runId}`

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

  const loginTitle = page.getByRole('heading', { name: '患者登录' })
  await loginTitle.waitFor()
  const loginTitleVisible = await loginTitle.isVisible()

  await page.locator('ion-input#mobile input').fill(mobile)
  await page.getByRole('button', { name: '发送验证码' }).click()
  await page.getByText('验证码发送成功').waitFor()

  await page.locator('ion-input#otpCode input').fill('123456')
  await page.getByRole('button', { name: '登录' }).click()

  await page.waitForURL(/\/realname$/, { timeout: 15000 })
  await page.getByRole('heading', { name: '绑定患者身份' }).waitFor()

  await page.locator('ion-input#name input').fill(patientName)
  await page.locator('ion-input#mobile input').fill(mobile)
  await page.locator('ion-input#hospitalPatientId input').fill(hospitalPatientId)
  await page.getByRole('button', { name: '提交绑定' }).click()

  await page.waitForURL(/\/tabs\/home$/, { timeout: 15000 })

  const storageStateAfterLogin = await page.evaluate(() => ({
    accessToken: Boolean(localStorage.getItem('patient_access_token')),
    refreshToken: Boolean(sessionStorage.getItem('patient_refresh_token')),
    bindingStatus: localStorage.getItem('patient_binding_status'),
    activePlanStatus: localStorage.getItem('patient_has_active_plan'),
    mobile: sessionStorage.getItem('patient_mobile'),
    deviceId: localStorage.getItem('patient_device_id')
  }))

  await page.goto(`${baseUrl}/tabs/profile`, { waitUntil: 'networkidle' })
  await page.waitForURL(/\/tabs\/profile$/, { timeout: 15000 })
  await page.getByRole('button', { name: '退出登录' }).click()
  await page.waitForURL(/\/login$/, { timeout: 15000 })

  const storageStateAfterLogout = await page.evaluate(() => ({
    accessToken: localStorage.getItem('patient_access_token'),
    refreshToken: sessionStorage.getItem('patient_refresh_token'),
    bindingStatus: localStorage.getItem('patient_binding_status'),
    activePlanStatus: localStorage.getItem('patient_has_active_plan'),
    mobile: sessionStorage.getItem('patient_mobile'),
    deviceId: localStorage.getItem('patient_device_id'),
    userProfile: localStorage.getItem('patient_user_profile'),
    healthData: localStorage.getItem('patient_health_data')
  }))

  console.log(JSON.stringify({
    success: true,
    finalUrl: page.url(),
    loginTitleVisible,
    homeVisible: true,
    loggedOutToLogin: page.url().endsWith('/login'),
    mobile,
    hospitalPatientId,
    deviceId: storageStateAfterLogin.deviceId,
    storageStateAfterLogin,
    storageStateAfterLogout
  }))
} finally {
  await context.close()
  await browser.close()
}
