import { chromium } from 'playwright'

const patientBaseUrl = process.env.E2E_PATIENT_BASE_URL || 'http://127.0.0.1:3001'
const browser = await chromium.launch({ headless: true })
const context = await browser.newContext({ viewport: { width: 390, height: 844 } })
const page = await context.newPage()
let familyBindings = []
let notificationPolls = 0
let satisfactionSubmitted = false

const ok = (data) => ({ status: 200, contentType: 'application/json', body: JSON.stringify({ code: 200, msg: 'success', data }) })

await page.addInitScript(() => {
  localStorage.setItem('patient_binding_status', 'BOUND')
  localStorage.setItem('patient_has_active_plan', 'true')
  localStorage.setItem('patient_device_id', 'e2e-device')
})

await page.route('**/app-api/**', async (route) => {
  const request = route.request()
  const url = new URL(request.url())
  const path = url.pathname
  if (path.endsWith('/auth/session/refresh')) return route.fulfill(ok({ accessToken: 'e2e-token', expiresIn: 900, refreshExpiresIn: 3600, deviceId: 'e2e-device' }))
  if (path.endsWith('/auth/me')) return route.fulfill(ok({ accountType: 'PATIENT' }))
  if (path.endsWith('/patients/me/profile')) return route.fulfill(ok({ patientId: 1, hospitalPatientId: 'E2E-1', name: '测试患者', phone: '138****0000', realNameVerified: true, identityMaterialArchived: true }))
  if (path.endsWith('/patients/me/health-profile')) return route.fulfill(ok({ patientId: 1, layer1: {}, layer2: {}, layer3: {} }))
  if (path.endsWith('/patients/me/family-bindings')) {
    if (request.method() === 'POST') {
      familyBindings = [{ id: 1, familyAccountId: 2, displayName: '女儿', mobileMasked: '139****0000', relationshipType: 'CHILD', status: 'ACTIVE', authorizedAt: new Date().toISOString() }]
    }
    return route.fulfill(ok(familyBindings))
  }
  if (path.endsWith('/medication-plans/active')) return route.fulfill(ok({ id: 1, planName: '测试计划', versionNo: 1, status: 'ACTIVE' }))
  if (path.endsWith('/dose-tasks/today') || path.endsWith('/dose-tasks')) return route.fulfill(ok([]))
  if (path.endsWith('/assessments/knowledge/status')) return route.fulfill(ok({ questionSetVersion: 'medication-literacy-v1', currentPhase: 'DAY_1', due: true, day1: null, week4: null }))
  if (path.endsWith('/assessments/knowledge/questions')) return route.fulfill(ok(Array.from({ length: 10 }, (_, index) => ({ questionCode: `Q${index + 1}`, questionText: `科普题目 ${index + 1}`, options: [{ code: 'A', text: '选项 A' }, { code: 'B', text: '选项 B' }] }))))
  if (path.endsWith('/assessments/knowledge') && request.method() === 'POST') return route.fulfill(ok({ id: 1, phase: 'DAY_1', questionSetVersion: 'medication-literacy-v1', score: 10, maxScore: 10, submittedAt: new Date().toISOString(), answers: [] }))
  if (path.endsWith('/help-orders/40')) return route.fulfill(ok({ id: 40, title: '已关闭测试工单', category: 'OTHER', priority: 'NORMAL', status: 'CLOSED', createdAt: new Date().toISOString(), events: [] }))
  if (path.endsWith('/assessments/satisfaction/status')) return route.fulfill(ok({ helpOrderId: 40, eligible: true, submitted: satisfactionSubmitted, survey: satisfactionSubmitted ? { helpOrderId: 40, rating: 5 } : null }))
  if (path.endsWith('/assessments/satisfaction') && request.method() === 'POST') { satisfactionSubmitted = true; return route.fulfill(ok({ helpOrderId: 40, rating: 5 })) }
  if (path.endsWith('/notifications/socket-ticket')) return route.fulfill({ status: 503, contentType: 'application/json', body: JSON.stringify({ code: 503, msg: 'unavailable', data: null }) })
  if (path.endsWith('/notifications/logs')) { notificationPolls += 1; return route.fulfill(ok([])) }
  return route.fulfill(ok(null))
})

try {
  await page.goto(`${patientBaseUrl}/tabs/home`, { waitUntil: 'networkidle' })
  await page.getByTestId('knowledge-assessment-task').getByRole('button', { name: '去完成' }).click()
  await page.waitForURL(/knowledge-assessment/)
  await page.getByText('科普题目 10').waitFor()
  await page.waitForTimeout(500)
  const assessmentPage = page.getByText('科普题目 10').locator('xpath=ancestor::*[contains(concat(" ", normalize-space(@class), " "), " ion-page ")][1]')
  const options = assessmentPage.locator('.option-button')
  const optionCount = await options.count()
  if (optionCount < 20) throw new Error(`Expected 20 assessment options, found ${optionCount}`)
  for (let index = 0; index < optionCount; index += 2) await options.nth(index).click({ force: true })
  const selectedCount = await assessmentPage.locator('.option-button--selected').count()
  if (selectedCount < 10) throw new Error(`Expected 10 selected answers, found ${selectedCount}`)
  await assessmentPage.getByTestId('knowledge-submit').click()
  await page.getByText('10 / 10').waitFor()

  await page.goto(`${patientBaseUrl}/tabs/profile`, { waitUntil: 'networkidle' })
  await page.getByTestId('family-mobile').locator('input').fill('13900000000')
  await page.getByTestId('family-display-name').locator('input').fill('女儿')
  await page.getByTestId('family-submit').click()
  await page.getByText('139****0000').waitFor()

  await page.goto(`${patientBaseUrl}/help-order/40`, { waitUntil: 'networkidle' })
  await page.getByRole('button', { name: '评价本次服务' }).click()
  await page.locator('.rating-button').nth(4).click()
  await page.getByRole('button', { name: '提交评价' }).click()
  await page.getByText(/已提交 5 分/).waitFor()
  await page.waitForTimeout(200)

  for (const viewport of [{ width: 390, height: 844 }, { width: 430, height: 932 }]) {
    await page.setViewportSize(viewport)
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth > document.documentElement.clientWidth)
    if (overflow) throw new Error(`Horizontal overflow at ${viewport.width}x${viewport.height}`)
  }
  if (notificationPolls < 1) throw new Error('Notification polling fallback was not exercised')
  console.log(JSON.stringify({ success: true, familyBinding: true, knowledgeAssessment: true, satisfaction: true, notificationPolling: true, viewports: ['390x844', '430x932'] }))
} finally {
  await context.close()
  await browser.close()
}
