import { beforeEach, describe, expect, test, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  read: vi.fn(), write: vi.fn(), clear: vi.fn(), getDoseTasks: vi.fn(), submitCheckin: vi.fn(),
  clearGateState: vi.fn(), cancelAllReminders: vi.fn(), accountSubject: 'patient-1', accountType: 'PATIENT',
  authStatus: 'authenticated'
}))

vi.mock('@/app/secureMedicalCache', () => ({
  isNativeMedicalCache: () => true,
  secureMedicalCache: { read: mocks.read, write: mocks.write, clear: mocks.clear }
}))

vi.mock('@/modules/patient/api/mvp', () => ({
  medicationService: { getDoseTasks: mocks.getDoseTasks, submitCheckin: mocks.submitCheckin }
}))

vi.mock('@/app/session', () => ({
  patientSession: {
    getAccountSubject: () => mocks.accountSubject,
    getAccountType: () => mocks.accountType,
    getAuthStatus: () => mocks.authStatus,
    getAccessToken: () => 'access-token',
    clearGateState: mocks.clearGateState
  }
}))

vi.mock('@/modules/patient/services/localMedicationReminders', () => ({
  cancelAllMedicationReminders: mocks.cancelAllReminders
}))

import {
  cacheOfflineAccountType,
  loadMedicationTasks,
  replayPendingCheckins,
  submitOrQueueCheckin
} from '@/modules/patient/services/offlineMedication'

const cachedTask = {
  id: 7,
  taskDate: '2026-08-24',
  scheduledAt: '2026-08-24T08:00:00',
  status: 'PENDING',
  drugName: 'encrypted-only',
  dosage: 'encrypted-only'
}

describe('native offline medication flow', () => {
  beforeEach(() => {
    Object.values(mocks).forEach((mock) => {
      if (typeof mock === 'function' && 'mockReset' in mock) mock.mockReset()
    })
    mocks.accountSubject = 'patient-1'
    mocks.accountType = 'PATIENT'
    mocks.authStatus = 'authenticated'
    mocks.read.mockResolvedValue({
      value: JSON.stringify({ version: 1, accountSubject: 'patient-1', expiresAt: Date.now() + 60_000, tasks: [cachedTask], pendingCheckins: [] })
    })
    mocks.write.mockResolvedValue(undefined)
    mocks.clear.mockResolvedValue(undefined)
    mocks.cancelAllReminders.mockResolvedValue(undefined)
  })

  test('keeps encrypted cached tasks when the API is unreachable', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    mocks.getDoseTasks.mockRejectedValue(new Error('Network error'))
    const result = await loadMedicationTasks()
    expect(result).toEqual({ tasks: [cachedTask], offline: true })
  })

  test.each(['Server error', 'Session temporarily unavailable'])(
    'uses encrypted cached tasks for %s after restoring an offline session',
    async (message) => {
      Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
      mocks.authStatus = 'offline'
      mocks.getDoseTasks.mockRejectedValue(new Error(message))

      await expect(loadMedicationTasks()).resolves.toEqual({ tasks: [cachedTask], offline: true })
    }
  )

  test('does not hide business failures behind the restored offline cache', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    mocks.authStatus = 'offline'
    mocks.getDoseTasks.mockRejectedValue(new Error('No permission'))

    await expect(loadMedicationTasks()).rejects.toThrow('No permission')
  })

  test('persists the verified account type inside the encrypted native cache', async () => {
    await cacheOfflineAccountType('FAMILY', 'patient-1')

    const saved = JSON.parse(mocks.write.mock.calls[0][0].value)
    expect(saved.accountSubject).toBe('patient-1')
    expect(saved.accountType).toBe('FAMILY')
  })

  test('queues a stable idempotent request only for an offline network failure', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    mocks.submitCheckin.mockRejectedValue(new Error('Network error'))
    await expect(submitOrQueueCheckin(cachedTask as any, 'TAKEN')).resolves.toBe('queued')
    const saved = JSON.parse(mocks.write.mock.calls[0][0].value)
    expect(saved.pendingCheckins).toHaveLength(1)
    expect(saved.pendingCheckins[0].clientRequestId).toBeTruthy()
    expect(saved.tasks[0].status).toBe('PENDING')
  })

  test('does not queue online business failures', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    mocks.submitCheckin.mockRejectedValue(new Error('CHECKIN_WINDOW_EXPIRED'))
    await expect(submitOrQueueCheckin(cachedTask as any, 'TAKEN')).rejects.toThrow('CHECKIN_WINDOW_EXPIRED')
    expect(mocks.write).not.toHaveBeenCalled()
  })

  test('does not queue an in-flight check-in after the authenticated account changes', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    let rejectSubmit!: (error: Error) => void
    mocks.submitCheckin.mockImplementation(() => new Promise((_, reject) => { rejectSubmit = reject }))

    const submission = submitOrQueueCheckin(cachedTask as any, 'TAKEN')
    await vi.waitFor(() => expect(mocks.submitCheckin).toHaveBeenCalledTimes(1))
    mocks.accountSubject = 'patient-2'
    rejectSubmit(new Error('Network error'))

    await expect(submission).rejects.toThrow('AUTHENTICATED_ACCOUNT_CHANGED')
    expect(mocks.write).not.toHaveBeenCalled()
  })

  test('rejects another account cache instead of exposing its tasks', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    mocks.read.mockResolvedValue({
      value: JSON.stringify({ version: 1, accountSubject: 'patient-2', expiresAt: Date.now() + 60_000, tasks: [cachedTask], pendingCheckins: [] })
    })
    mocks.getDoseTasks.mockRejectedValue(new Error('Network error'))
    await expect(loadMedicationTasks()).resolves.toEqual({ tasks: [], offline: true })
    expect(mocks.clear).toHaveBeenCalled()
  })

  test('keeps encrypted data sealed while no account is authenticated', async () => {
    mocks.accountSubject = ''
    await expect(loadMedicationTasks()).resolves.toEqual({ tasks: [], offline: true })
    expect(mocks.clear).not.toHaveBeenCalled()
    expect(mocks.write).not.toHaveBeenCalled()
  })

  test('does not rewrite an old account cache after the session changes during replay', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    mocks.read.mockResolvedValue({
      value: JSON.stringify({
        version: 1,
        accountSubject: 'patient-1',
        expiresAt: Date.now() + 60_000,
        tasks: [cachedTask],
        pendingCheckins: [{ clientRequestId: 'request-1', doseTaskId: 7, result: 'TAKEN', actualTakenAt: new Date().toISOString() }]
      })
    })
    let resolveSubmit!: () => void
    mocks.submitCheckin.mockImplementation(() => new Promise<void>((resolve) => { resolveSubmit = resolve }))

    const replay = replayPendingCheckins()
    await vi.waitFor(() => expect(mocks.submitCheckin).toHaveBeenCalledTimes(1))
    mocks.accountSubject = 'patient-2'
    resolveSubmit()
    await replay

    expect(mocks.write).not.toHaveBeenCalled()
  })

  test('serializes concurrent offline check-ins without losing either pending request', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    mocks.submitCheckin.mockRejectedValue(new Error('Network error'))
    let storedValue = JSON.stringify({
      version: 1,
      accountSubject: 'patient-1',
      expiresAt: Date.now() + 60_000,
      tasks: [cachedTask, { ...cachedTask, id: 8 }],
      pendingCheckins: []
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    let releaseFirstWrite!: () => void
    mocks.write
      .mockImplementationOnce(({ value }) => new Promise<void>((resolve) => {
        releaseFirstWrite = () => {
          storedValue = value
          resolve()
        }
      }))
      .mockImplementation(async ({ value }) => { storedValue = value })

    const first = submitOrQueueCheckin(cachedTask as any, 'TAKEN')
    await vi.waitFor(() => expect(mocks.write).toHaveBeenCalledTimes(1))
    const second = submitOrQueueCheckin({ ...cachedTask, id: 8 } as any, 'MISSED')
    await vi.waitFor(() => expect(mocks.submitCheckin).toHaveBeenCalledTimes(2))
    expect(mocks.read).toHaveBeenCalledTimes(1)

    releaseFirstWrite()
    await Promise.all([first, second])

    const saved = JSON.parse(storedValue)
    expect(saved.pendingCheckins).toHaveLength(2)
    expect(saved.pendingCheckins.map((item: any) => item.doseTaskId)).toEqual([7, 8])
  })

  test('replaces a pending check-in for the same task while reusing its idempotency key', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    mocks.submitCheckin.mockRejectedValue(new Error('Network error'))
    let storedValue = JSON.stringify({
      version: 1,
      accountSubject: 'patient-1',
      expiresAt: Date.now() + 60_000,
      tasks: [cachedTask],
      pendingCheckins: []
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })

    await submitOrQueueCheckin(cachedTask as any, 'TAKEN')
    const firstRequestId = JSON.parse(storedValue).pendingCheckins[0].clientRequestId
    await submitOrQueueCheckin(cachedTask as any, 'MISSED')

    const pending = JSON.parse(storedValue).pendingCheckins
    expect(pending).toHaveLength(1)
    expect(pending[0].clientRequestId).toBe(firstRequestId)
    expect(pending[0].result).toBe('MISSED')
  })

  test('gives a newly queued check-in its own fresh expiry without extending cached tasks', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    mocks.submitCheckin.mockRejectedValue(new Error('Network error'))
    const originalExpiry = Date.now() + 60_000
    mocks.read.mockResolvedValue({
      value: JSON.stringify({
        version: 1,
        accountSubject: 'patient-1',
        expiresAt: originalExpiry,
        tasks: [cachedTask],
        pendingCheckins: []
      })
    })

    await submitOrQueueCheckin(cachedTask as any, 'TAKEN')

    const saved = JSON.parse(mocks.write.mock.calls[0][0].value)
    expect(saved.tasksExpiresAt).toBe(originalExpiry)
    expect(saved.pendingCheckins[0].expiresAt).toBeGreaterThan(originalExpiry)
  })

  test('refreshes the task expiry independently from existing pending check-ins', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    const pendingExpiry = Date.now() + 60_000
    mocks.read.mockResolvedValue({
      value: JSON.stringify({
        version: 2,
        accountSubject: 'patient-1',
        tasksExpiresAt: Date.now() + 30_000,
        tasks: [cachedTask],
        pendingCheckins: [{ clientRequestId: 'request-1', doseTaskId: 7, result: 'TAKEN', actualTakenAt: new Date().toISOString(), expiresAt: pendingExpiry }]
      })
    })
    mocks.submitCheckin.mockRejectedValue(new Error('Network error'))
    mocks.getDoseTasks.mockResolvedValue({ data: [{ ...cachedTask, id: 8 }] })

    const result = await loadMedicationTasks()

    expect(result).toEqual({ tasks: [{ ...cachedTask, id: 8 }], offline: false })
    const saved = JSON.parse(mocks.write.mock.calls.at(-1)[0].value)
    expect(saved.tasksExpiresAt).toBeGreaterThan(pendingExpiry)
    expect(saved.pendingCheckins[0].expiresAt).toBe(pendingExpiry)
  })

  test('keeps pending check-in results projected over a fresh server task snapshot', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    mocks.read.mockResolvedValue({
      value: JSON.stringify({
        version: 2,
        accountSubject: 'patient-1',
        tasksExpiresAt: Date.now() + 60_000,
        tasks: [{ ...cachedTask, status: 'COMPLETED' }],
        pendingCheckins: [{ clientRequestId: 'request-1', doseTaskId: 7, result: 'TAKEN', actualTakenAt: new Date().toISOString(), expiresAt: Date.now() + 60_000 }]
      })
    })
    mocks.submitCheckin.mockRejectedValue(new Error('TEMPORARY_SERVER_ERROR'))
    mocks.getDoseTasks.mockResolvedValue({ data: [{ ...cachedTask, status: 'PENDING' }] })

    const result = await loadMedicationTasks()

    expect(result.tasks[0].status).toBe('COMPLETED')
    const saved = JSON.parse(mocks.write.mock.calls.at(-1)[0].value)
    expect(saved.tasks[0].status).toBe('PENDING')
    expect(saved.pendingCheckins).toHaveLength(1)
  })

  test('persists partial expiry pruning without extending remaining medical data', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    mocks.read.mockResolvedValue({
      value: JSON.stringify({
        version: 2,
        accountSubject: 'patient-1',
        tasksExpiresAt: Date.now() - 1,
        tasks: [cachedTask],
        pendingCheckins: [{ clientRequestId: 'request-1', doseTaskId: 7, result: 'TAKEN', actualTakenAt: new Date().toISOString(), expiresAt: Date.now() + 60_000 }]
      })
    })
    mocks.getDoseTasks.mockRejectedValue(new Error('Network error'))

    await expect(loadMedicationTasks()).resolves.toEqual({ tasks: [], offline: true })

    const saved = JSON.parse(mocks.write.mock.calls[0][0].value)
    expect(saved.tasks).toEqual([])
    expect(saved.pendingCheckins).toHaveLength(1)
  })

  test('keeps the atomic cache when expiry-pruning persistence fails', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    mocks.read.mockResolvedValue({
      value: JSON.stringify({
        version: 2,
        accountSubject: 'patient-1',
        tasksExpiresAt: Date.now() - 1,
        tasks: [cachedTask],
        pendingCheckins: [{ clientRequestId: 'request-1', doseTaskId: 7, result: 'TAKEN', actualTakenAt: new Date().toISOString(), expiresAt: Date.now() + 60_000 }]
      })
    })
    mocks.write.mockRejectedValue(new Error('SECURE_CACHE_WRITE_FAILED'))
    mocks.getDoseTasks.mockRejectedValue(new Error('Network error'))

    await expect(loadMedicationTasks()).resolves.toEqual({ tasks: [], offline: true })

    expect(mocks.write).toHaveBeenCalled()
    expect(mocks.clear).not.toHaveBeenCalled()
  })

  test('deletes the native cache and key when all medical entries have expired', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    mocks.read.mockResolvedValue({
      value: JSON.stringify({
        version: 2,
        accountSubject: 'patient-1',
        tasksExpiresAt: Date.now() - 1,
        tasks: [cachedTask],
        pendingCheckins: [{ clientRequestId: 'request-1', doseTaskId: 7, result: 'TAKEN', actualTakenAt: new Date().toISOString(), expiresAt: Date.now() - 1 }]
      })
    })
    mocks.getDoseTasks.mockRejectedValue(new Error('Network error'))

    await expect(loadMedicationTasks()).resolves.toEqual({ tasks: [], offline: true })

    expect(mocks.clear).toHaveBeenCalled()
    expect(mocks.write).not.toHaveBeenCalled()
  })

  test('does not cache a task response after the authenticated account changes', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let resolveTasks!: (value: { data: any[] }) => void
    mocks.getDoseTasks.mockImplementation(() => new Promise((resolve) => { resolveTasks = resolve }))

    const loading = loadMedicationTasks()
    await vi.waitFor(() => expect(mocks.getDoseTasks).toHaveBeenCalledTimes(1))
    mocks.accountSubject = 'patient-2'
    resolveTasks({ data: [cachedTask] })

    await expect(loading).resolves.toEqual({ tasks: [], offline: true })
    expect(mocks.write).not.toHaveBeenCalled()
  })

  test('drops a terminal missing-task checkin and continues replaying the queue', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let storedValue = JSON.stringify({
      version: 1,
      accountSubject: 'patient-1',
      expiresAt: Date.now() + 60_000,
      tasks: [cachedTask],
      pendingCheckins: [
        { clientRequestId: 'missing-task', doseTaskId: 7, result: 'TAKEN', actualTakenAt: new Date().toISOString() },
        { clientRequestId: 'valid-task', doseTaskId: 8, result: 'TAKEN', actualTakenAt: new Date().toISOString() }
      ]
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    mocks.submitCheckin
      .mockRejectedValueOnce(new Error('DOSE_TASK_NOT_FOUND'))
      .mockResolvedValueOnce(undefined)

    await expect(replayPendingCheckins()).resolves.toBe(1)

    expect(mocks.submitCheckin).toHaveBeenCalledTimes(2)
    expect(JSON.parse(storedValue).pendingCheckins).toEqual([])
  })

  test('does not expose cached tasks after the patient binding is revoked online', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    mocks.getDoseTasks.mockRejectedValue(new Error('PATIENT_BIND_CONFLICT'))

    await expect(loadMedicationTasks()).rejects.toThrow('PATIENT_BIND_CONFLICT')

    expect(mocks.clear).toHaveBeenCalled()
    expect(mocks.clearGateState).toHaveBeenCalled()
    expect(mocks.cancelAllReminders).toHaveBeenCalled()
    expect(mocks.write).not.toHaveBeenCalled()
  })
})
