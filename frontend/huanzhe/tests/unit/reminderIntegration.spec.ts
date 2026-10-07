import { beforeEach, afterEach, expect, test, vi } from 'vitest'

const mocks = vi.hoisted(() => ({ subject: 'patient-1' as string | null, generation: 1, status: 'authenticated',
  load: vi.fn(), schedule: vi.fn(), submit: vi.fn() }))
vi.mock('@/app/session', () => ({ patientSession: {
  getAccountSubject: () => mocks.subject, getAccountType: () => 'PATIENT',
  getSessionGeneration: () => mocks.generation, getAuthStatus: () => mocks.status,
  getGateState: () => ({ isBound: true })
} }))
vi.mock('@capacitor/core', () => ({ Capacitor: { isNativePlatform: () => false } }))
vi.mock('@/modules/patient/services/offlineMedication', () => ({ loadMedicationTasks: mocks.load, submitOrQueueCheckin: mocks.submit }))
vi.mock('@/modules/patient/services/localMedicationReminders', () => ({
  enableAndScheduleMedicationReminders: mocks.schedule, reminderOptions: { sound: true, vibration: true }
}))
import { startReminders, syncReminders, reminderState, recordTaken, setReminderOption } from '@/modules/patient/reminders/service'

beforeEach(() => {
  vi.useFakeTimers(); localStorage.clear(); mocks.subject = 'patient-1'; mocks.status = 'authenticated'; mocks.generation++
  mocks.load.mockReset(); mocks.schedule.mockReset(); mocks.submit.mockReset()
  window.dispatchEvent(new Event('patient-account-switched'))
})
afterEach(() => vi.useRealTimers())

test('loads seven days and sends foreground actions through the existing encrypted queue', async () => {
  const task = { id: 7, status: 'PENDING', scheduledAt: new Date(Date.now() + 1000).toISOString(), drugName: 'PRIVATE' }
  mocks.load.mockResolvedValue({ tasks: [task], offline: false }); mocks.submit.mockResolvedValue('queued')
  startReminders(); await vi.waitFor(() => expect(mocks.schedule).toHaveBeenCalled())
  await vi.advanceTimersByTimeAsync(1500)
  expect(reminderState.active.map(t => t.id)).toEqual([7])
  await recordTaken(task as any)
  await vi.advanceTimersByTimeAsync(2000)
  expect(reminderState.active).toEqual([])
  expect(mocks.load).toHaveBeenCalledWith(7)
  expect(mocks.submit).toHaveBeenCalledWith(task, 'TAKEN', { actualTakenAt: expect.any(String) })
  await setReminderOption('sound', false)
  expect(JSON.stringify(localStorage)).not.toContain('PRIVATE')
  expect(JSON.parse(localStorage.getItem('patient-reminder-options')!)).toEqual({ sound: false, vibration: true })
})

test('rejects late task loads after logout and never restores medical data into anonymous UI', async () => {
  let resolve!: (value: any) => void
  mocks.load.mockReturnValue(new Promise(r => { resolve = r }))
  const pending = syncReminders()
  mocks.subject = null; mocks.status = 'anonymous'; mocks.generation++
  window.dispatchEvent(new Event('patient-session-anonymous'))
  resolve({ tasks: [{ id: 7, status: 'PENDING' }], offline: false }); await pending
  expect(mocks.schedule).not.toHaveBeenCalled()
  expect(reminderState.active).toEqual([])
  await syncReminders()
  expect(mocks.load).toHaveBeenCalledTimes(1)
})
