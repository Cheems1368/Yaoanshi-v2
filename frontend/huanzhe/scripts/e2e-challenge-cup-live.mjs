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
const expectedAiModel = process.env.E2E_EXPECTED_AI_MODEL || 'zai-org/GLM-4.5-Air'
const aiPrompt = process.env.E2E_AI_PROMPT || '长期服药为什么还要复查？'
const aiTimeoutMs = Number.parseInt(process.env.E2E_AI_TIMEOUT_MS || '30000', 10)
const usePresetPrompt = !process.env.E2E_AI_PROMPT
const loadingText = '正在生成回答'

const patientIdentity = createPatientIdentity(runId, 'Challenge Cup Live Patient')

const browser = await chromium.launch({ headless: true })
const context = await browser.newContext({ viewport: { width: 430, height: 932 } })
const page = await context.newPage()

function hasSafetyDisclaimer(text) {
  if (!text) {
    return false
  }

  const hasScienceScope = /仅供.*科普|本回答仅用于健康科普/.test(text)
  const hasOfflineCareHint = /线下就医|及时就医/.test(text)
  return hasScienceScope && hasOfflineCareHint
}

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

  const aiResponsePromise = page.waitForResponse((response) => {
    return response.url().includes('/app-api/ai/chat')
  }, { timeout: aiTimeoutMs })

  if (usePresetPrompt) {
    await page.getByTestId('ai-home-question-2').click()
  } else {
    const promptButton = page.getByText(aiPrompt, { exact: true })
    if (await promptButton.count()) {
      await promptButton.first().click()
    } else {
      await page.getByTestId('ai-input').locator('input').fill(aiPrompt)
      await page.getByTestId('ai-input-row').getByRole('button').click()
    }
  }

  await page.getByTestId('ai-chat-view').waitFor()
  await page.getByTestId('ai-message-assistant-2').waitFor()

  const aiResponse = await aiResponsePromise
  const aiPayload = await aiResponse.json()
  const aiData = aiPayload?.data

  if (aiPayload?.code !== 200) {
    throw new Error(`Expected AI success code 200, received ${aiPayload?.code ?? 'unknown'} (${aiPayload?.msg ?? 'no message'})`)
  }

  if (!aiData?.reply || typeof aiData.reply !== 'string') {
    throw new Error('AI response missing reply text')
  }

  if (!hasSafetyDisclaimer(aiData.reply)) {
    throw new Error('AI reply missing expected safety disclaimer semantics')
  }

  if (aiData.model !== expectedAiModel) {
    throw new Error(`Expected AI model ${expectedAiModel}, received ${aiData.model || 'unknown'}`)
  }

  await page.waitForFunction((text) => {
    const target = document.querySelector('[data-testid="ai-message-assistant-2"]')
    return target && !target.textContent?.includes(text)
  }, loadingText, { timeout: aiTimeoutMs })

  const firstReply = await page.getByTestId('ai-message-assistant-2').textContent()
  if (!firstReply || !hasSafetyDisclaimer(firstReply)) {
    throw new Error('Rendered AI reply is empty or missing safety disclaimer semantics')
  }

  const hasRetryRow = await page.getByTestId('ai-retry-row').isVisible().catch(() => false)
  if (hasRetryRow) {
    throw new Error('AI live flow unexpectedly entered retry state')
  }

  await page.getByTestId('ai-back-button').getByRole('button').click()
  await page.getByTestId('ai-home-view').waitFor()
  const stayedOnAiRouteAfterSoftBack = /\/ai-assistant$/.test(page.url())

  await page.getByTestId('ai-back-button').getByRole('button').click()
  await page.waitForURL(/\/tabs\/home$/, { timeout: 15000 })

  console.log(JSON.stringify({
    success: true,
    patientBaseUrl,
    backendBaseUrl,
    expectedAiModel,
    actualAiModel: aiData.model,
    aiRequestId: aiData.requestId,
    aiPrompt,
    aiReply: aiData.reply,
    renderedReply: firstReply,
    rationalDetailTitle,
    stayedOnAiRouteAfterSoftBack,
    finalUrl: page.url()
  }))
} finally {
  await context.close()
  await browser.close()
}
