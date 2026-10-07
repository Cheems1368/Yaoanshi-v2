import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { execFileSync } from 'node:child_process'
import { chromium } from 'playwright'
import {
  adminHeaders, bindPatient, createRunId, createPatientIdentity, createPatientSession,
  professionalLogin, requestJson, primePatientPage, primeProfessionalPage
} from './e2e-shared.mjs'

const repo = path.resolve(import.meta.dirname, '../../..')
const results = process.env.E2E_OCR_RESULTS_DIR || 'D:/RapidOCR/results'
const variants = process.env.E2E_OCR_VARIANTS_DIR || 'D:/RapidOCR/results'
const password = process.env.E2E_PHARMACIST_PASSWORD
if (!password) throw new Error('Provide E2E_PHARMACIST_PASSWORD through the environment')
const backend = process.env.E2E_BACKEND_URL || 'http://127.0.0.1:8080'
const patientBase = process.env.E2E_PATIENT_URL || 'http://127.0.0.1:3001'
const pharmacistBase = process.env.E2E_PHARMACIST_URL || 'http://127.0.0.1:3003'
const sample = process.env.E2E_OCR_SAMPLE_PATH
if (!sample) throw new Error('Provide E2E_OCR_SAMPLE_PATH for a synthetic prescription')
const identity = createPatientIdentity(createRunId(), 'OCR 验证患者')
const patientSession = await createPatientSession(backend, identity)
const pharmacistSession = await professionalLogin(backend, { username: 'pharmacist', password, role: 'pharmacist' })
const binding = await bindPatient(backend, patientSession, identity)
assert.equal(binding.payload.code, 200)
const confirmation = await requestJson(`${backend}/admin-api/patients/${binding.payload.data.id}/confirm-binding`, {
  method: 'POST', headers: adminHeaders(pharmacistSession.accessToken)
})
assert.equal(confirmation.payload.code, 200)
const browser = await chromium.launch({ headless: true })
const patientContext = await browser.newContext({ viewport: { width: 430, height: 932 } })
const pharmacistContext = await browser.newContext({ viewport: { width: 1440, height: 960 } })
const patient = await patientContext.newPage()
const pharmacist = await pharmacistContext.newPage()
const evidence = { uploaded: [], failureRecovery: false, verifiedAt: new Date().toISOString() }
let createdRule = null
await fs.mkdir(results, { recursive: true })

function ocrProcess(action) {
  execFileSync('powershell.exe', ['-NoProfile', '-ExecutionPolicy', 'Bypass', '-File',
    path.join(repo, `ops/local-ocr/${action}.ps1`), '-OcrOnly'], { timeout: 150000, stdio: 'ignore' })
}

async function uploadFromPage(imagePath = sample) {
  await patient.reload({ waitUntil: 'networkidle' })
  await patient.locator('input[type=file]').nth(1).setInputFiles(imagePath)
  const responsePromise = patient.waitForResponse(response =>
    response.url().endsWith('/app-api/prescriptions/uploads') && response.request().method() === 'POST', { timeout: 45000 })
  await patient.getByRole('button', { name: '提交登记', exact: true }).click()
  const payload = await (await responsePromise).json()
  assert.equal(payload.code, 200)
  return payload.data
}

function assertDraft(draft) {
  assert.equal(draft.status, 'OCR_DONE')
  assert.equal(draft.ocrStatus, 'HIGH_CONFIDENCE')
  assert.equal(draft.items.length, 2)
  const [first, second] = draft.items
  assert.deepEqual([first.drugName, first.drugSpec, first.frequency], ['氨氯地平片', '5mg/片', '每日一次'])
  assert.deepEqual([second.drugName, second.drugSpec, second.frequency], ['二甲双胍片', '0.5g/片', '每日两次'])
  for (const item of draft.items) {
    assert.equal(item.dosage, null)
    assert.equal(item.route, null)
    assert.equal(item.durationDays, null)
  }
  assert.ok(!/患者|DEMO|医生|诊断|72岁|2026-10-05/.test(draft.ocrRawText))
}

try {
  await primePatientPage(patient, patientBase, patientSession)
  await patient.goto(`${patientBase}/tabs/prescription`, { waitUntil: 'networkidle' })
  const draft = await uploadFromPage()
  assertDraft(draft)
  evidence.uploaded.push({ id: draft.id, status: draft.status, ocrStatus: draft.ocrStatus, items: draft.items })
  await patient.locator('.record-card').filter({ hasText: '高置信度' }).first().waitFor()
  await patient.screenshot({ animations: 'disabled', path: path.join(results, 'patient-ocr-prefilled.png'), fullPage: true })

  await primeProfessionalPage(pharmacist, pharmacistBase, pharmacistSession)
  await pharmacist.goto(`${pharmacistBase}/prescriptions`, { waitUntil: 'networkidle' })
  const row = pharmacist.locator('.el-table__body tr').filter({ hasText: identity.patientName })
  await row.getByRole('button', { name: '进入复核' }).click()
  await pharmacist.getByPlaceholder('药品名称').first().waitFor()
  await pharmacist.getByText('查看 OCR 原文', { exact: true }).click()
  await pharmacist.getByPlaceholder('药品名称').first().scrollIntoViewIfNeeded()
  await pharmacist.screenshot({ animations: 'disabled', path: path.join(results, 'pharmacist-ocr-draft.png') })
  const images = pharmacist.locator('.el-drawer img')
  assert.ok(await images.count() > 0)
  await pharmacist.waitForFunction(() => [...document.querySelectorAll('.el-drawer img')].some(image => image.complete && image.naturalWidth > 0))
  assert.ok(await images.first().evaluate(image => image.complete && image.naturalWidth > 0))
  assert.ok((await pharmacist.locator('.el-drawer .el-input-number input').evaluateAll(inputs => inputs.map(input => input.value))).every(value => value === ''))
  await images.first().scrollIntoViewIfNeeded()
  await images.first().click()
  await pharmacist.locator('.el-image-viewer__wrapper').waitFor()
  await pharmacist.screenshot({ animations: 'disabled', path: path.join(results, 'pharmacist-original-prescription.png') })
  await pharmacist.locator('.el-image-viewer__close').click()
  await pharmacist.locator('.el-image-viewer__wrapper').waitFor({ state: 'hidden' })

  await pharmacist.getByPlaceholder('剂量').nth(0).fill('5mg')
  await pharmacist.getByPlaceholder('剂量').nth(1).fill('0.5g')
  await pharmacist.getByPlaceholder('给药途径').nth(0).fill('口服')
  await pharmacist.getByPlaceholder('给药途径').nth(1).fill('口服')
  await pharmacist.screenshot({ animations: 'disabled', path: path.join(results, 'pharmacist-manual-correction.png') })
  const confirmPromise = pharmacist.waitForResponse(response =>
    response.url().endsWith(`/admin-api/prescriptions/${draft.id}/confirm`) && response.request().method() === 'POST')
  await pharmacist.getByRole('button', { name: '提交复核', exact: true }).click()
  const confirmed = await (await confirmPromise).json()
  assert.equal(confirmed.code, 200)
  const detail = await requestJson(`${backend}/admin-api/prescriptions/${draft.id}`, { headers: adminHeaders(pharmacistSession.accessToken) })
  assert.equal(detail.payload.data.status, 'PHARMACIST_CONFIRMED')
  assert.deepEqual(detail.payload.data.items.map(item => item.dosage), ['5mg', '0.5g'])
  assert.ok(detail.payload.data.items.every(item => item.durationDays === null))
  evidence.confirmedPrescriptionId = draft.id
  await patient.reload({ waitUntil: 'networkidle' })
  await patient.getByText('已完成复核', { exact: true }).first().waitFor()
  await patient.screenshot({ animations: 'disabled', path: path.join(results, 'patient-pharmacist-confirmed.png'), fullPage: true })

  ocrProcess('stop')
  try {
    const fallback = await uploadFromPage()
    assert.equal(fallback.status, 'UPLOADED')
    assert.equal(fallback.ocrStatus, 'LOW_CONFIDENCE')
    assert.equal(fallback.items.length, 0)
    assert.ok(fallback.ocrFailureReason)
    evidence.fallback = { id: fallback.id, reason: fallback.ocrFailureReason }
    await patient.locator('.record-card').filter({ hasText: '低置信度，转人工复核' }).first().waitFor()
    await patient.screenshot({ animations: 'disabled', path: path.join(results, 'patient-ocr-unavailable.png'), fullPage: true })
    await pharmacist.goto(`${pharmacistBase}/prescriptions`, { waitUntil: 'networkidle' })
    const fallbackRow = pharmacist.getByRole('row').filter({ has: pharmacist.getByRole('cell', { name: String(fallback.id), exact: true }) })
    await fallbackRow.getByRole('button', { name: '进入复核' }).click()
    await pharmacist.getByPlaceholder('药品名称').first().fill('氨氯地平片')
    await pharmacist.getByPlaceholder('规格').first().fill('5mg/片')
    await pharmacist.getByPlaceholder('剂量').first().fill('5mg')
    await pharmacist.getByPlaceholder('频次').first().fill('每日一次')
    await pharmacist.getByPlaceholder('给药途径').first().fill('口服')
    await pharmacist.screenshot({ animations: 'disabled', path: path.join(results, 'pharmacist-ocr-manual-fallback.png') })
    const manualConfirmPromise = pharmacist.waitForResponse(response =>
      response.url().endsWith(`/admin-api/prescriptions/${fallback.id}/confirm`) && response.request().method() === 'POST')
    await pharmacist.getByRole('button', { name: '提交复核', exact: true }).click()
    assert.equal((await (await manualConfirmPromise).json()).code, 200)
    const manual = await requestJson(`${backend}/admin-api/prescriptions/${fallback.id}`, { headers: adminHeaders(pharmacistSession.accessToken) })
    assert.equal(manual.payload.data.status, 'PHARMACIST_CONFIRMED')
    assert.equal(manual.payload.data.items[0].durationDays, null)
    evidence.fallback.manualConfirmed = true
  } finally {
    const restartStarted = Date.now()
    ocrProcess('start')
    evidence.serviceRestartReadyMs = Date.now() - restartStarted
  }
  const recovered = await uploadFromPage()
  assertDraft(recovered)
  evidence.failureRecovery = true
  evidence.recoveredPrescriptionId = recovered.id
  await patient.screenshot({ animations: 'disabled', path: path.join(results, 'patient-ocr-recovered.png'), fullPage: true })
  evidence.variants = {}
  for (const variant of ['rotated90', 'rotated180', 'blurred', 'blank']) {
    const result = await uploadFromPage(path.join(variants, `${variant}.png`))
    if (result.items.length === 0) {
      assert.equal(result.status, 'UPLOADED')
      assert.equal(result.ocrStatus, 'LOW_CONFIDENCE')
    } else {
      assert.equal(result.status, 'OCR_DONE')
      assert.ok(result.items.length <= 2)
      const expected = { '氨氯地平片': ['5mg/片', '每日一次'], '二甲双胍片': ['0.5g/片', '每日两次'] }
      for (const item of result.items) {
        assert.ok(expected[item.drugName])
        assert.ok(item.drugSpec === null || item.drugSpec === expected[item.drugName][0])
        assert.ok(item.frequency === null || item.frequency === expected[item.drugName][1])
        assert.equal(item.dosage, null)
        assert.equal(item.route, null)
        assert.equal(item.durationDays, null)
      }
    }
    assert.ok(result.ocrRawText === null || !/患者|DEMO|医生|诊断|72岁/.test(result.ocrRawText))
    evidence.variants[variant] = { id: result.id, status: result.status, items: result.items.length, reason: result.ocrFailureReason }
  }
  const riskRule = await requestJson(`${backend}/admin-api/ddi/rules`, {
    method: 'POST', headers: adminHeaders(pharmacistSession.accessToken),
    body: { ruleCode: `OCR-SYNTH-${Date.now()}`, drugAName: '氨氯地平片', drugBName: '二甲双胍片',
      severity: 'HIGH', message: '仅合成验收规则', recommendedAction: '合成验收确认', status: 'ACTIVE' }
  })
  assert.equal(riskRule.payload.code, 200)
  createdRule = riskRule.payload.data
  await pharmacist.reload({ waitUntil: 'networkidle' })
  await pharmacist.locator('.el-table__body tr').filter({ hasText: identity.patientName })
    .filter({ has: pharmacist.getByText(String(recovered.id), { exact: true }) }).getByRole('button', { name: '进入复核' }).click()
  await pharmacist.getByPlaceholder('药品名称').first().waitFor()
  const riskResponse = pharmacist.waitForResponse(response => response.url().endsWith(`/admin-api/prescriptions/${recovered.id}/confirm`)
    && response.request().method() === 'POST')
  await pharmacist.getByRole('button', { name: '提交复核', exact: true }).click()
  assert.equal((await (await riskResponse).json()).msg, 'DDI_CONFIRM_REQUIRED')
  await pharmacist.getByRole('heading', { name: 'DDI 风险提示', exact: true }).waitFor()
  evidence.firstSubmitRiskVisible = true
  await pharmacist.screenshot({ animations: 'disabled', path: path.join(results, 'first-submit-ddi-risk.png') })
  await pharmacist.getByRole('button', { name: '确认继续', exact: true }).click()
  await pharmacist.getByText('DDI 风险已确认', { exact: true }).waitFor()
  const riskConfirmed = pharmacist.waitForResponse(response => response.url().endsWith(`/admin-api/prescriptions/${recovered.id}/confirm`)
    && response.request().method() === 'POST')
  await pharmacist.getByRole('button', { name: '提交复核', exact: true }).click()
  assert.equal((await (await riskConfirmed).json()).code, 200)
  evidence.riskConfirmedWithoutReopening = true
  await fs.writeFile(path.join(results, 'web-e2e.json'), JSON.stringify(evidence, null, 2), 'utf8')
  console.log(JSON.stringify({ passed: true, prescriptionId: draft.id, failureRecovery: true, results }))
} finally {
  if (createdRule) {
    await requestJson(`${backend}/admin-api/ddi/rules/${createdRule.id}`, {
      method: 'PUT', headers: adminHeaders(pharmacistSession.accessToken), body: { ...createdRule, status: 'INACTIVE' }
    })
  }
  await browser.close()
}
