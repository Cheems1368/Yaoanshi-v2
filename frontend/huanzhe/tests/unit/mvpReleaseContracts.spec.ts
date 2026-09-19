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
    expect(homeSource).toContain('enableAndScheduleMedicationReminders(tasksResponse.tasks)')
    expect(reminderSource).toContain('cancelMedicationReminder(task.id)')
    expect(tabSource).toContain('cancelMedicationReminder(task.id)')
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
})
