import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { beforeEach, describe, expect, test, vi } from 'vitest'

const httpMocks = vi.hoisted(() => ({
  get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn()
}))

vi.mock('@/shared/http/client', () => ({ httpService: httpMocks }))

import { appNotificationService, assessmentService, medicationService, patientService } from '@/modules/patient/api/mvp'
import { GENERIC_REMINDER_BODY } from '@/modules/patient/services/localMedicationReminders'

describe('MVP release patient contracts', () => {
  beforeEach(() => Object.values(httpMocks).forEach((mock) => mock.mockReset()))

  test('uses server-owned assessment questions and selected options only', async () => {
    httpMocks.get.mockResolvedValue({ data: [] })
    httpMocks.post.mockResolvedValue({ data: {} })
    await assessmentService.getKnowledgeQuestions()
    await assessmentService.getKnowledgeStatus()
    await assessmentService.submitKnowledge({
      phase: 'DAY_1', questionSetVersion: 'medication-literacy-v1',
      answers: [{ questionCode: 'Q01', selectedOption: 'A' }]
    })
    expect(httpMocks.get).toHaveBeenCalledWith('/app-api/assessments/knowledge/questions')
    expect(httpMocks.get).toHaveBeenCalledWith('/app-api/assessments/knowledge/status')
    expect(httpMocks.post.mock.calls[0][1]).not.toHaveProperty('correct')
    expect(httpMocks.post.mock.calls[0][1]).not.toHaveProperty('correctOption')
  })

  test('passes idempotency, family, satisfaction and notification-log contracts', async () => {
    httpMocks.get.mockResolvedValue({ data: [] })
    httpMocks.post.mockResolvedValue({ data: {} })
    await medicationService.getDoseTasks('2026-08-24', '2026-08-30')
    await medicationService.submitCheckin({ doseTaskId: 7, result: 'TAKEN', clientRequestId: 'request-1' })
    await patientService.getFamilyBindings()
    await assessmentService.getSatisfactionStatus(40)
    await appNotificationService.getLogs(10, 20)
    expect(httpMocks.get).toHaveBeenCalledWith('/app-api/dose-tasks', { dateFrom: '2026-08-24', dateTo: '2026-08-30' })
    expect(httpMocks.post).toHaveBeenCalledWith('/app-api/checkins', expect.objectContaining({ clientRequestId: 'request-1' }))
    expect(httpMocks.get).toHaveBeenCalledWith('/app-api/patients/me/family-bindings')
    expect(httpMocks.get).toHaveBeenCalledWith('/app-api/assessments/satisfaction/status', { helpOrderId: 40 })
    expect(httpMocks.get).toHaveBeenCalledWith('/app-api/notifications/logs', { afterId: 10, limit: 20 })
  })

  test('keeps native medical cache encrypted and out of Web Storage', () => {
    const webSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/services/offlineMedication.ts'), 'utf8')
    const nativeSource = readFileSync(resolve(process.cwd(), 'android/app/src/main/java/com/yaoanshi/patient/SecureMedicalCachePlugin.java'), 'utf8')
    expect(webSource).not.toMatch(/localStorage|sessionStorage|indexedDB/i)
    expect(nativeSource).toContain('AndroidKeyStore')
    expect(nativeSource).toContain('AES/GCM/NoPadding')
    expect(nativeSource).toContain('AtomicFile')
    expect(nativeSource).toContain('openRead')
    expect(nativeSource).toContain('finishWrite')
    expect(nativeSource).toContain('failWrite')
    expect(nativeSource).not.toContain('MODE_WORLD_READABLE')
  })

  test('uses generic lock-screen wording without identity, drug or dose', () => {
    expect(GENERIC_REMINDER_BODY).toBe('您有一项用药任务待处理，请打开药按时查看。')
    expect(GENERIC_REMINDER_BODY).not.toMatch(/患者|姓名|药名|剂量|毫克|mg/i)
  })

  test('schedules reminders from the default home route and cancels queued checkins immediately', () => {
    const homeSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/HomePage.vue'), 'utf8')
    const reminderSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/MedicationReminderPage.vue'), 'utf8')
    const tabSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/MedicationTabPage.vue'), 'utf8')
    expect(homeSource).toContain('accountType || patientSession.getAccountType() || undefined')
    expect(homeSource).toContain('expectedAccountSubject')
    expect(reminderSource).toContain('cancelMedicationReminder(task.id)')
    expect(reminderSource).toContain('expectedAccountSubject')
    expect(tabSource).toContain('cancelMedicationReminder(task.id)')
    expect(tabSource).toContain('expectedAccountSubject')
    expect(reminderSource).toContain("currentAccountType.value = patientSession.getAccountType() ?? 'UNKNOWN'")
    expect(reminderSource).toContain('请尽快处理')
  })

  test('keeps submitted satisfaction visible and makes offline correction explicit', () => {
    const orderSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/HelpOrderDetailPage.vue'), 'utf8')
    const reminderSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/MedicationReminderPage.vue'), 'utf8')
    const tabSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/MedicationTabPage.vue'), 'utf8')
    expect(orderSource).toContain('satisfactionStatus?.eligible || satisfactionStatus?.submitted')
    expect(reminderSource).toContain("if (offlineMode.value)")
    expect(reminderSource).toContain('已提交记录需联网后更正')
    expect(tabSource).toContain("if (offlineMode.value)")
    expect(tabSource).toContain('已提交记录需联网后更正')
  })

  test('preserves patient offline checkins when a family binding is removed', () => {
    const profileSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/ProfilePage.vue'), 'utf8')
    const unbindFlow = profileSource.slice(
      profileSource.indexOf('const confirmUnbind'),
      profileSource.indexOf('const handleLogout')
    )
    expect(unbindFlow).toContain('deleteFamilyBinding(binding.id)')
    expect(unbindFlow).not.toContain('clearMedicalCache()')
  })

  test('clears encrypted medical state when a session becomes definitively anonymous', () => {
    const mainSource = readFileSync(resolve(process.cwd(), 'src/main.ts'), 'utf8')
    const anonymousHandler = mainSource.slice(
      mainSource.indexOf("window.addEventListener('patient-session-anonymous'"),
      mainSource.indexOf('const clearRevokedMedicalContext')
    )
    expect(anonymousHandler).toContain('clearMedicalCache()')
    expect(anonymousHandler).toContain('cancelAllMedicationReminders()')
  })

  test('preserves offline state for temporary refresh failures and restores before online replay', () => {
    const clientSource = readFileSync(resolve(process.cwd(), 'src/shared/http/client.ts'), 'utf8')
    const mainSource = readFileSync(resolve(process.cwd(), 'src/main.ts'), 'utf8')
    expect(clientSource).toContain('payload.code === 40004')
    expect(clientSource).toContain("error?.response?.status !== 401")
    expect(clientSource).toContain('this.refreshUnavailable = true')
    expect(clientSource).toContain("new Error('Session temporarily unavailable')")
    expect(mainSource).toContain('await httpService.restoreSession()')
    expect(mainSource).toContain("window.addEventListener('online', () => void restoreAndReplay())")
  })

  test('binds automatic retries and page loads to the originating account', () => {
    const clientSource = readFileSync(resolve(process.cwd(), 'src/shared/http/client.ts'), 'utf8')
    const homeSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/HomePage.vue'), 'utf8')
    const reminderSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/MedicationReminderPage.vue'), 'utf8')
    const tabSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/MedicationTabPage.vue'), 'utf8')
    expect(clientSource).toContain('_accountSubject')
    expect(clientSource).toContain("new Error('AUTHENTICATED_ACCOUNT_CHANGED')")
    for (const source of [homeSource, reminderSource, tabSource]) {
      expect(source).toContain('expectedRequestEpoch')
      expect(source).toContain('patientSession.getAccountSubject() !== expectedAccountSubject')
    }
  })

  test('revalidates restored family access and resets cached family UI state', () => {
    const mainSource = readFileSync(resolve(process.cwd(), 'src/main.ts'), 'utf8')
    const profileSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/ProfilePage.vue'), 'utf8')
    expect(mainSource).toContain("patientSession.getAccountType() !== 'FAMILY'")
    expect(mainSource).toContain('if (!currentPatient.data)')
    expect(mainSource).toContain("message === 'PATIENT_BIND_CONFLICT' || message === 'PATIENT_NOT_FOUND'")
    expect(mainSource).toContain('await clearRevokedMedicalContext()')
    expect(mainSource).toContain("window.addEventListener('focus'")
    expect(mainSource).toContain("window.addEventListener('patient-session-authenticated'")
    expect(mainSource).toContain("document.addEventListener('visibilitychange'")
    expect(mainSource).toContain('window.setInterval(() => void revalidateFamilyAccess(), 60_000)')
    expect(profileSource).toContain('onIonViewWillEnter(async () => {')
    expect(profileSource).toContain('resetFamilyState()')
  })

  test('aligns satisfaction length and Android exact-alarm contracts', () => {
    const orderSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/HelpOrderDetailPage.vue'), 'utf8')
    const manifest = readFileSync(resolve(process.cwd(), 'android/app/src/main/AndroidManifest.xml'), 'utf8')
    expect(orderSource).toContain(':maxlength="255"')
    expect(manifest).toContain('android.permission.SCHEDULE_EXACT_ALARM')
  })

  test('isolates knowledge assessment state from account-switch races', () => {
    const assessmentSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/KnowledgeAssessmentPage.vue'), 'utf8')
    expect(assessmentSource).toContain('const expectedAccountSubject = patientSession.getAccountSubject()')
    expect(assessmentSource).toContain('patientSession.getAccountSubject() !== expectedAccountSubject')
    expect(assessmentSource).toContain('const expectedRequestEpoch = ++requestEpoch')
    expect(assessmentSource).toContain('clearAssessmentState()')
    expect(assessmentSource).toContain('delete answers[questionCode]')
  })

  test('isolates family bindings by account and uses a subject-bearing E2E JWT', () => {
    const profileSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/ProfilePage.vue'), 'utf8')
    const e2eSource = readFileSync(resolve(process.cwd(), 'scripts/e2e-mvp-release.mjs'), 'utf8')
    expect(profileSource).toContain('const expectedRequestEpoch = ++familyRequestEpoch')
    expect(profileSource).toContain('patientSession.getAccountSubject() !== expectedAccountSubject')
    expect(profileSource).toContain('familyBindings.value = []')
    expect(e2eSource).toContain("sub: 'e2e-patient'")
    expect(e2eSource).not.toContain("accessToken: 'e2e-token'")
  })

  test('guards satisfaction polling from stale account, order and request responses', () => {
    const orderSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/HelpOrderDetailPage.vue'), 'utf8')
    expect(orderSource).toContain('const expectedRequestEpoch = ++satisfactionRequestEpoch')
    expect(orderSource).toContain('expectedRequestEpoch !== satisfactionRequestEpoch')
    expect(orderSource).toContain('patientSession.getAccountSubject() !== expectedAccountSubject')
    expect(orderSource).toContain('orderId.value !== expectedOrderId')
    expect(orderSource).toContain('if (satisfactionSubmitting.value) return')
    expect(orderSource).toContain('expectedRequestEpoch === satisfactionRequestEpoch')
  })

  test('commits satisfaction from the POST response and clears drafts across contexts', () => {
    const orderSource = readFileSync(resolve(process.cwd(), 'src/modules/patient/pages/HelpOrderDetailPage.vue'), 'utf8')
    const submitFlow = orderSource.slice(
      orderSource.indexOf('const submitSatisfaction'),
      orderSource.indexOf('const sendReplyPayload')
    )
    expect(submitFlow).toContain('const response = await assessmentService.submitSatisfaction')
    expect(submitFlow).toContain('submitted: true')
    expect(submitFlow).not.toContain('getSatisfactionStatus')
    expect(orderSource).toContain('satisfactionExpanded.value = false')
    expect(orderSource).toContain('satisfactionRating.value = 0')
    expect(orderSource).toContain("satisfactionComment.value = ''")
  })
})
