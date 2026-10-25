import { beforeEach, afterEach, expect, test, vi } from 'vitest'

const mocks = vi.hoisted(() => ({ subject: 'patient-1' as string | null, generation: 1, status: 'authenticated', accountType: 'PATIENT', bound: true, native: false,
  load: vi.fn(), schedule: vi.fn(), submit: vi.fn() }))
vi.mock('@/app/session', () => ({ patientSession: {
  getAccountSubject: () => mocks.subject, getAccountType: () => mocks.accountType,
  getSessionGeneration: () => mocks.generation, getAuthStatus: () => mocks.status,
  getGateState: () => ({ isBound: mocks.bound })
} }))
vi.mock('@capacitor/core', () => ({ Capacitor: { isNativePlatform: () => mocks.native } }))
vi.mock('@/modules/patient/services/offlineMedication', () => ({ loadMedicationTasks: mocks.load, submitOrQueueCheckin: mocks.submit }))
vi.mock('@/modules/patient/services/localMedicationReminders', () => ({
  enableAndScheduleMedicationReminders: mocks.schedule, reminderOptions: { sound: true, vibration: true }
}))
let service: typeof import('@/modules/patient/reminders/service')
let windowListeners: ReturnType<typeof vi.spyOn>
let documentListeners: ReturnType<typeof vi.spyOn>

beforeEach(async () => {
  vi.resetModules()
  vi.useFakeTimers(); localStorage.clear(); mocks.subject = 'patient-1'; mocks.status = 'authenticated'; mocks.generation++
  mocks.load.mockReset(); mocks.schedule.mockReset(); mocks.submit.mockReset()
  mocks.accountType = 'PATIENT'; mocks.bound = true; mocks.native = false
  mocks.schedule.mockResolvedValue('exact')
  windowListeners = vi.spyOn(window, 'addEventListener')
  documentListeners = vi.spyOn(document, 'addEventListener')
  service = await import('@/modules/patient/reminders/service')
  service.reminderState.options.sound = true; service.reminderState.options.vibration = true
})
afterEach(() => {
  for (const [type, listener] of windowListeners.mock.calls) window.removeEventListener(type, listener)
  for (const [type, listener] of documentListeners.mock.calls) document.removeEventListener(type, listener)
  vi.restoreAllMocks(); vi.useRealTimers()
})

test('loads seven days and sends foreground actions through the existing encrypted queue', async () => {
  const { startReminders, reminderState, recordTaken, setReminderOption } = service
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
  const { startReminders, syncReminders, reminderState } = service
  let resolve!: (value: any) => void
  mocks.load.mockReturnValue(new Promise(r => { resolve = r }))
  startReminders()
  mocks.subject = null; mocks.status = 'anonymous'; mocks.generation++
  window.dispatchEvent(new Event('patient-session-anonymous'))
  resolve({ tasks: [{ id: 7, status: 'PENDING' }], offline: false }); await vi.advanceTimersByTimeAsync(0)
  expect(mocks.schedule).not.toHaveBeenCalled()
  expect(reminderState.active).toEqual([])
  await syncReminders()
  expect(mocks.load).toHaveBeenCalledTimes(1)
})


test.each(['enable', 'sound', 'vibration'])('loads family tasks before %s settings reschedule', async (action) => {
  mocks.native = true; mocks.accountType = 'FAMILY'
  const task = { id: 8, status: 'PENDING', scheduledAt: new Date(Date.now() + 1000).toISOString() }
  mocks.load.mockResolvedValue({ tasks: [task], offline: false })
  service.startReminders(); await vi.advanceTimersByTimeAsync(0)
  mocks.schedule.mockClear()
  if (action === 'enable') await service.enableReminders()
  else await service.setReminderOption(action as 'sound' | 'vibration', false)
  expect(mocks.load).toHaveBeenCalledWith(7)
  expect(mocks.schedule).toHaveBeenCalledWith([task], 'FAMILY')
  expect(mocks.schedule.mock.calls.every(([tasks]) => tasks.length === 1)).toBe(true)
  await vi.advanceTimersByTimeAsync(2000)
  expect(service.reminderState.active).toEqual([])
})

test('removes a task checked in elsewhere before a slow refresh can show its foreground reminder', async () => {
  const task = { id: 7, status: 'PENDING', scheduledAt: new Date(Date.now() + 2000).toISOString() }
  mocks.load.mockResolvedValue({ tasks: [task], offline: false })
  service.startReminders(); await vi.advanceTimersByTimeAsync(0)
  let resolve!: (value: any) => void
  mocks.load.mockReturnValue(new Promise(r => { resolve = r }))
  window.dispatchEvent(new CustomEvent('medication-checkin-updated', { detail: { taskId: 7, accountSubject: mocks.subject } }))
  await vi.advanceTimersByTimeAsync(3000)
  expect(service.reminderState.active).toEqual([])
  resolve({ tasks: [task], offline: false }); await vi.advanceTimersByTimeAsync(1000)
  expect(service.reminderState.active).toEqual([])
})

test.each(['PATIENT_BIND_CONFLICT', 'PATIENT_NOT_FOUND'])('clears active and future tasks after %s', async (message) => {
  const tasks = [1000, 4000].map((delay, index) => ({ id: index + 7, status: 'PENDING',
    scheduledAt: new Date(Date.now() + delay).toISOString() }))
  mocks.load.mockResolvedValue({ tasks, offline: false })
  service.startReminders(); await vi.advanceTimersByTimeAsync(1500)
  expect(service.reminderState.active.map(t => t.id)).toEqual([7])
  mocks.load.mockImplementation(async () => { mocks.bound = false; throw new Error(message) })
  await service.syncReminders()
  expect(service.reminderState.active).toEqual([])
  await vi.advanceTimersByTimeAsync(4000)
  expect(service.reminderState.active).toEqual([])
})

test('drops an existing popup when binding disappears through another page', async () => {
  mocks.load.mockResolvedValue({ tasks: [{ id: 7, status: 'PENDING', scheduledAt: new Date(Date.now() + 1000).toISOString() }], offline: false })
  service.startReminders(); await vi.advanceTimersByTimeAsync(1500)
  expect(service.reminderState.active).toHaveLength(1)
  mocks.bound = false
  await vi.advanceTimersByTimeAsync(1000)
  expect(service.reminderState.active).toEqual([])
})


test('reschedules valid offline family tasks when a setting changes', async () => {
  mocks.native = true; mocks.accountType = 'FAMILY'
  const task = { id: 8, status: 'PENDING', scheduledAt: new Date(Date.now() + 1000).toISOString() }
  mocks.load.mockResolvedValue({ tasks: [task], offline: true })
  service.startReminders(); await vi.advanceTimersByTimeAsync(0)
  expect(mocks.schedule).not.toHaveBeenCalled()
  await service.setReminderOption('vibration', false)
  expect(mocks.schedule).toHaveBeenCalledWith([task], 'FAMILY')
})

test('keeps existing native reminders when family task loading fails', async () => {
  mocks.native = true; mocks.accountType = 'FAMILY'
  mocks.load.mockRejectedValue(new Error('Network error'))
  await service.setReminderOption('sound', false)
  expect(mocks.schedule).not.toHaveBeenCalled()
})

test('rejects tasks returned after binding was cleared during loading', async () => {
  let resolve!: (value: any) => void
  mocks.load.mockReturnValue(new Promise(r => { resolve = r }))
  service.startReminders(); mocks.bound = false
  resolve({ tasks: [{ id: 7, status: 'PENDING', scheduledAt: new Date(Date.now() + 1000).toISOString() }], offline: false })
  await vi.advanceTimersByTimeAsync(2000)
  expect(mocks.schedule).not.toHaveBeenCalled()
  expect(service.reminderState.active).toEqual([])
})

test('ignores a prior account check-in event when task ids overlap', async () => {
  const task = { id: 7, status: 'PENDING', scheduledAt: new Date(Date.now() + 1000).toISOString() }
  mocks.load.mockResolvedValue({ tasks: [task], offline: false })
  service.startReminders(); await vi.advanceTimersByTimeAsync(0)
  window.dispatchEvent(new CustomEvent('medication-checkin-updated', { detail: { taskId: 7, accountSubject: 'another-account' } }))
  await vi.advanceTimersByTimeAsync(1500)
  expect(service.reminderState.active.map(t => t.id)).toEqual([7])
})

test('does not reset a new account after the old load fails', async () => {
  let reject!: (value: Error) => void
  mocks.load.mockReturnValue(new Promise((_, r) => { reject = r }))
  const pending = service.syncReminders()
  mocks.subject = 'patient-2'; mocks.generation++
  service.reminderState.message = 'NEW ACCOUNT'
  reject(new Error('PATIENT_BIND_CONFLICT')); await pending
  expect(service.reminderState.message).toBe('NEW ACCOUNT')
})


test('does not restore a checked-in native alarm from an older pending snapshot', async () => {
  mocks.native = true
  const task = { id: 7, status: 'PENDING', scheduledAt: new Date(Date.now() + 10000).toISOString() }
  mocks.load.mockResolvedValue({ tasks: [task], offline: false })
  service.startReminders(); await vi.advanceTimersByTimeAsync(0)
  window.dispatchEvent(new CustomEvent('medication-checkin-updated', { detail: { taskId: 7, accountSubject: mocks.subject } }))
  await vi.advanceTimersByTimeAsync(0)
  expect(mocks.schedule).toHaveBeenLastCalledWith([], 'PATIENT')
})

test('clears memory on an authoritative binding error even before another layer updates the gate', async () => {
  mocks.load.mockResolvedValue({ tasks: [{ id: 7, status: 'PENDING', scheduledAt: new Date(Date.now() + 1000).toISOString() }], offline: false })
  service.startReminders(); await vi.advanceTimersByTimeAsync(1500)
  mocks.load.mockRejectedValue(new Error('PATIENT_NOT_FOUND'))
  await service.syncReminders()
  expect(service.reminderState.active).toEqual([])
})


test('clears an offline popup when another page invalidates the binding', async () => {
  mocks.status = 'offline'
  mocks.load.mockResolvedValue({ tasks: [{ id: 7, status: 'PENDING', scheduledAt: new Date(Date.now() + 1000).toISOString() }], offline: true })
  service.startReminders(); await vi.advanceTimersByTimeAsync(1500)
  expect(service.reminderState.active).toHaveLength(1)
  window.dispatchEvent(new CustomEvent('patient-binding-invalidated', { detail: { accountSubject: mocks.subject } }))
  expect(service.reminderState.active).toEqual([])
})
