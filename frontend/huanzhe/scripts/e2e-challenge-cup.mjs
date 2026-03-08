import { chromium } from 'playwright'
import {
  createPatientIdentity,
  createPatientSession,
  createRunId,
  primePatientPage
} from './e2e-shared.mjs'

const runId = createRunId()
const patientBaseUrl = process.env.E2E_PATIENT_BASE_URL || 'http://127.0.0.1:3001'
const backendBaseUrl = process.env.E2E_BACKEND_BASE_URL || 'http://127.0.0.1:8080'
const patientIdentity = createPatientIdentity(runId, 'Challenge Cup Patient')

const browser = await chromium.launch({ headless: true })
const context = await browser.newContext({ viewport: { width: 430, height: 932 } })
const page = await context.newPage()

let aiRequestCount = 0

await page.route('**/app-api/ai/chat', async (route) => {
  aiRequestCount += 1

  if (aiRequestCount === 1) {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 200,
        msg: 'success',
        data: {
          reply: '请按时服药，并注意观察头晕和出血等风险信号。',
          model: 'mock-challenge-cup',
          requestId: `mock-success-${runId}`
        }
      })
    })
    return
  }

  if (aiRequestCount === 2) {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        code: 40018,
        msg: 'AI 服务暂不可用',
        data: null
      })
    })
    return
  }

  await route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({
      code: 200,
      msg: 'success',
      data: {
        reply: '如出现持续头晕、黑便或明显出血，请及时线下就医。',
        model: 'mock-challenge-cup',
        requestId: `mock-retry-${runId}`
      }
    })
  })
})

try {
  const patientSession = await createPatientSession(backendBaseUrl, patientIdentity)
  await primePatientPage(page, patientBaseUrl, patientSession, { bound: true, hasActivePlan: false })

  await page.goto(`${patientBaseUrl}/tabs/home`, { waitUntil: 'networkidle' })

  await page.getByTestId('home-quick-rational-medication').click()
  await page.waitForURL(/\/rational-medication$/, { timeout: 15000 })
  await page.getByTestId('rational-category-cardiovascular').click()
  await page.waitForURL(/\/drug-category\/cardiovascular$/, { timeout: 15000 })
  await page.locator('ion-accordion ion-item[slot="header"]').first().click()
  await page.getByTestId('rational-item-aspirin-enteric').waitFor({ state: 'visible' })
  await page.getByTestId('rational-item-aspirin-enteric').click()
  await page.waitForURL(/\/drug-detail\/aspirin-enteric/, { timeout: 15000 })
  await page.getByTestId('rational-detail-page').waitFor()
  const rationalDetailTitle = await page.getByTestId('rational-detail-title').textContent()

  await page.goto(`${patientBaseUrl}/tabs/home`, { waitUntil: 'networkidle' })
  await page.getByTestId('home-quick-ai-assistant').click()
  await page.waitForURL(/\/ai-assistant$/, { timeout: 15000 })
  await page.getByTestId('ai-home-view').waitFor()
  await page.getByTestId('ai-home-question-0').click()
  await page.getByTestId('ai-chat-view').waitFor()
  await page.getByTestId('ai-message-assistant-2').waitFor()

  const firstReply = await page.getByTestId('ai-message-assistant-2').textContent()
  const firstSuggestion = await page.getByTestId('ai-suggestion-0').textContent()

  await page.getByTestId('ai-input').locator('input').fill('如果服务失败怎么办？')
  await page.getByTestId('ai-input-row').getByRole('button').click()
  await page.getByTestId('ai-retry-row').waitFor()
  const failedReply = await page.getByTestId('ai-message-assistant-4').textContent()

  await page.getByTestId('ai-retry-row').getByRole('button').click()
  await page.getByTestId('ai-message-assistant-6').waitFor()
  await page.waitForFunction(() => {
    const target = document.querySelector('[data-testid="ai-message-assistant-6"]')
    return target && !target.textContent?.includes('正在生成回答')
  })
  const retryReply = await page.getByTestId('ai-message-assistant-6').textContent()

  await page.getByTestId('ai-back-button').getByRole('button').click()
  await page.getByTestId('ai-home-view').waitFor()
  const stayedOnAiRouteAfterSoftBack = /\/ai-assistant$/.test(page.url())

  await page.getByTestId('ai-back-button').getByRole('button').click()
  await page.waitForURL(/\/tabs\/home$/, { timeout: 15000 })

  console.log(JSON.stringify({
    success: true,
    patientBaseUrl,
    backendBaseUrl,
    aiRequestCount,
    rationalDetailTitle,
    firstReply,
    firstSuggestion,
    failedReply,
    retryReply,
    stayedOnAiRouteAfterSoftBack,
    finalUrl: page.url()
  }))
} finally {
  await context.close()
  await browser.close()
}
