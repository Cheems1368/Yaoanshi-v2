import { beforeEach, describe, expect, test, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  read: vi.fn(), write: vi.fn(), clear: vi.fn(), getDoseTasks: vi.fn(), submitCheckin: vi.fn(), correctCheckin: vi.fn(),
  clearGateState: vi.fn(), cancelAllReminders: vi.fn(), cancelReminder: vi.fn(), accountSubject: 'patient-1', accountType: 'PATIENT',
  authStatus: 'authenticated'
}))

vi.mock('@/app/secureMedicalCache', () => ({
  isNativeMedicalCache: () => true,
  secureMedicalCache: { read: mocks.read, write: mocks.write, clear: mocks.clear }
}))

vi.mock('@/modules/patient/api/mvp', () => ({
  medicationService: {
    getDoseTasks: mocks.getDoseTasks,
    submitCheckin: mocks.submitCheckin,
    correctCheckin: mocks.correctCheckin
  }
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
  cancelAllMedicationReminders: mocks.cancelAllReminders,
  cancelMedicationReminder: mocks.cancelReminder
}))

import {
  cacheOfflineAccountType,
  consumeOfflineCheckinConflictCount,
  getCachedDoseTasks,
  loadMedicationTasks,
  OFFLINE_CHECKIN_CONFLICT_EVENT,
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
    mocks.cancelReminder.mockResolvedValue(undefined)
    consumeOfflineCheckinConflictCount()
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

  test.each(['Server error', 'Session temporarily unavailable'])(
    'queues a check-in during a restored offline session with %s', async (message) => {
      Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
      mocks.authStatus = 'offline'
      mocks.submitCheckin.mockRejectedValue(new Error(message))

      await expect(submitOrQueueCheckin(cachedTask as any, 'TAKEN')).resolves.toBe('queued')

      const saved = JSON.parse(mocks.write.mock.calls[0][0].value)
      expect(saved.pendingCheckins).toHaveLength(1)
      expect(saved.pendingCheckins[0].result).toBe('TAKEN')
    }
  )

  test('does not queue online business failures', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    mocks.submitCheckin.mockRejectedValue(new Error('CHECKIN_WINDOW_EXPIRED'))
    await expect(submitOrQueueCheckin(cachedTask as any, 'TAKEN')).rejects.toThrow('CHECKIN_WINDOW_EXPIRED')
    expect(mocks.write).not.toHaveBeenCalled()
  })

  test('does not queue business failures in a restored offline session', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    mocks.authStatus = 'offline'
    mocks.submitCheckin.mockRejectedValue(new Error('No permission'))

    await expect(submitOrQueueCheckin(cachedTask as any, 'TAKEN')).rejects.toThrow('No permission')
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

  test('does not inspect the new account cache after an old replay fails', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let storedValue = JSON.stringify({
      version: 2, accountSubject: 'patient-1', tasksExpiresAt: Date.now() + 60_000,
      tasks: [cachedTask],
      pendingCheckins: [{
        clientRequestId: 'old-request', doseTaskId: 7, result: 'TAKEN',
        actualTakenAt: new Date().toISOString(), expiresAt: Date.now() + 60_000
      }]
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    let rejectReplay!: (error: Error) => void
    mocks.submitCheckin.mockImplementation(() => new Promise((_resolve, reject) => { rejectReplay = reject }))

    const replay = replayPendingCheckins()
    await vi.waitFor(() => expect(mocks.submitCheckin).toHaveBeenCalledTimes(1))
    mocks.accountSubject = 'patient-2'
    storedValue = JSON.stringify({
      version: 2, accountSubject: 'patient-2', tasksExpiresAt: Date.now() + 60_000,
      tasks: [{ ...cachedTask, id: 8 }], pendingCheckins: []
    })
    rejectReplay(Object.assign(new Error('DOSE_TASK_EDIT_WINDOW_EXPIRED'), { code: 40009 }))

    await expect(replay).resolves.toBe(0)
    expect(mocks.read).toHaveBeenCalledTimes(1)
    expect(JSON.parse(storedValue)).toMatchObject({ accountSubject: 'patient-2', tasks: [{ id: 8 }] })
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
    expect(mocks.submitCheckin).toHaveBeenCalledTimes(1)
    expect(mocks.read).toHaveBeenCalledTimes(2)

    releaseFirstWrite()
    await vi.waitFor(() => expect(mocks.submitCheckin).toHaveBeenCalledTimes(2))
    await Promise.all([first, second])

    const saved = JSON.parse(storedValue)
    expect(saved.pendingCheckins).toHaveLength(2)
    expect(saved.pendingCheckins.map((item: any) => item.doseTaskId)).toEqual([7, 8])
  })

  test('queues an idempotent correction when the result changes for the same task', async () => {
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
    const firstPending = JSON.parse(storedValue).pendingCheckins[0]
    const firstRequestId = firstPending.clientRequestId
    await submitOrQueueCheckin(cachedTask as any, 'MISSED')

    const pending = JSON.parse(storedValue).pendingCheckins
    expect(pending).toHaveLength(1)
    expect(pending[0].clientRequestId).toBe(firstRequestId)
    expect(pending[0].expiresAt).toBe(firstPending.expiresAt)
    expect(pending[0].result).toBe('TAKEN')
    expect(pending[0].correction.result).toBe('MISSED')
    expect(pending[0].correction.clientRequestId).not.toBe(firstRequestId)
  })

  test('queues and replays an existing server check-in correction after a network failure', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    let storedValue = JSON.stringify({
      version: 2, accountSubject: 'patient-1', tasksExpiresAt: Date.now() + 60_000,
      tasks: [{ ...cachedTask, lastEffectiveCheckinId: 701 }], pendingCheckins: []
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    mocks.correctCheckin.mockRejectedValueOnce(new Error('Network error'))

    const task = { ...cachedTask, lastEffectiveCheckinId: 701 }
    await expect(submitOrQueueCheckin(task as any, 'MISSED')).resolves.toBe('queued')
    const firstPending = JSON.parse(storedValue).pendingCheckins[0]
    expect(firstPending.existingCheckinId).toBe(701)
    expect(firstPending.result).toBe('MISSED')
    expect(mocks.submitCheckin).not.toHaveBeenCalled()

    await expect(submitOrQueueCheckin(task as any, 'TAKEN')).resolves.toBe('queued')
    const pending = JSON.parse(storedValue).pendingCheckins[0]
    expect(pending.expiresAt).toBe(firstPending.expiresAt)
    expect(pending.correction.result).toBe('TAKEN')

    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    mocks.correctCheckin
      .mockResolvedValueOnce({ data: { id: 702, result: 'MISSED' } })
      .mockResolvedValueOnce({ data: { id: 703, result: 'TAKEN' } })
    await expect(replayPendingCheckins()).resolves.toBe(1)
    expect(mocks.correctCheckin).toHaveBeenCalledWith(701, expect.objectContaining({
      clientRequestId: firstPending.clientRequestId, result: 'MISSED'
    }))
    expect(mocks.correctCheckin).toHaveBeenCalledWith(702, expect.objectContaining({
      clientRequestId: pending.correction.clientRequestId, result: 'TAKEN'
    }))
    expect(mocks.submitCheckin).not.toHaveBeenCalled()
    expect(JSON.parse(storedValue).pendingCheckins).toEqual([])
    expect(JSON.parse(storedValue).tasks[0]).toMatchObject({ status: 'COMPLETED', lastEffectiveCheckinId: 703 })
  })

  test('targets the current effective record when an idempotent first correction was superseded', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let storedValue = JSON.stringify({
      version: 2, accountSubject: 'patient-1', tasksExpiresAt: Date.now() + 60_000,
      tasks: [{ ...cachedTask, lastEffectiveCheckinId: 701 }],
      pendingCheckins: [{
        clientRequestId: 'first-correction', doseTaskId: 7, existingCheckinId: 701,
        result: 'MISSED', actualTakenAt: new Date().toISOString(), expiresAt: Date.now() + 60_000,
        correction: { clientRequestId: 'second-correction', result: 'TAKEN', actualTakenAt: new Date().toISOString() }
      }]
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    mocks.correctCheckin
      .mockResolvedValueOnce({ data: { id: 702, result: 'MISSED', effective: false } })
      .mockResolvedValueOnce({ data: { id: 889, result: 'TAKEN', effective: true } })
    mocks.getDoseTasks.mockResolvedValue({ data: [{ ...cachedTask, lastEffectiveCheckinId: 888 }] })

    await expect(replayPendingCheckins()).resolves.toBe(1)
    expect(mocks.correctCheckin).toHaveBeenCalledWith(888, expect.objectContaining({
      clientRequestId: 'second-correction'
    }))
    expect(JSON.parse(storedValue).tasks[0].lastEffectiveCheckinId).toBe(889)
  })

  test('corrects an existing server check-in directly when online', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let storedValue = JSON.stringify({
      version: 2, accountSubject: 'patient-1', tasksExpiresAt: Date.now() + 60_000,
      tasks: [{ ...cachedTask, status: 'COMPLETED', lastEffectiveCheckinId: 701 }], pendingCheckins: []
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    mocks.correctCheckin.mockResolvedValue({ data: { id: 702, result: 'MISSED', effective: true } })

    await expect(submitOrQueueCheckin({ ...cachedTask, lastEffectiveCheckinId: 701 } as any, 'MISSED'))
      .resolves.toBe('submitted')
    expect(mocks.correctCheckin).toHaveBeenCalledWith(701, expect.objectContaining({ result: 'MISSED' }))
    expect(mocks.submitCheckin).not.toHaveBeenCalled()
    expect(JSON.parse(storedValue).tasks[0]).toMatchObject({ status: 'MISSED', lastEffectiveCheckinId: 702 })
    expect(mocks.cancelReminder).toHaveBeenCalledWith(7)
  })

  test('queues a record-page correction using only its task and check-in IDs', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    let storedValue = JSON.stringify({
      version: 2, accountSubject: 'patient-1', tasksExpiresAt: Date.now() + 60_000,
      tasks: [], pendingCheckins: []
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    mocks.correctCheckin.mockRejectedValue(new Error('Network error'))

    await expect(submitOrQueueCheckin({ id: 7, lastEffectiveCheckinId: 701 }, 'MISSED'))
      .resolves.toBe('queued')
    expect(mocks.correctCheckin).toHaveBeenCalledWith(701, expect.objectContaining({ result: 'MISSED' }))
    expect(JSON.parse(storedValue).pendingCheckins[0]).toMatchObject({
      doseTaskId: 7, existingCheckinId: 701, result: 'MISSED'
    })
  })

  test('persists a direct check-in before an offline restart without a task refresh', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let storedValue = JSON.stringify({
      version: 2, accountSubject: 'patient-1', tasksExpiresAt: Date.now() + 60_000,
      tasks: [cachedTask], pendingCheckins: []
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    mocks.submitCheckin.mockResolvedValue({ data: { id: 701, result: 'TAKEN', effective: true } })

    await expect(submitOrQueueCheckin(cachedTask as any, 'TAKEN')).resolves.toBe('submitted')
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    await expect(getCachedDoseTasks()).resolves.toEqual([expect.objectContaining({
      id: 7, status: 'COMPLETED', lastEffectiveCheckinId: 701, notificationStatus: 'ACKED', reminderStage: 'HANDLED'
    })])
    expect(mocks.cancelReminder).toHaveBeenCalledWith(7)
  })

  test('merges a new online choice into an existing queue before direct submission', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let storedValue = JSON.stringify({
      version: 2,
      accountSubject: 'patient-1',
      tasksExpiresAt: Date.now() + 60_000,
      tasks: [cachedTask],
      pendingCheckins: [{
        clientRequestId: 'original-request',
        doseTaskId: 7,
        result: 'TAKEN',
        actualTakenAt: new Date().toISOString(),
        expiresAt: Date.now() + 60_000
      }]
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    mocks.submitCheckin.mockResolvedValue({ data: { id: 701 } })
    mocks.correctCheckin.mockResolvedValue({ data: { id: 702 } })

    await expect(submitOrQueueCheckin(cachedTask as any, 'MISSED')).resolves.toBe('submitted')

    expect(mocks.submitCheckin).toHaveBeenCalledTimes(1)
    expect(mocks.submitCheckin).toHaveBeenCalledWith(expect.objectContaining({
      clientRequestId: 'original-request',
      result: 'TAKEN'
    }))
    expect(mocks.correctCheckin).toHaveBeenCalledWith(701, expect.objectContaining({ result: 'MISSED' }))
    expect(JSON.parse(storedValue).pendingCheckins).toEqual([])
  })

  test('replays a changed result as a correction after confirming the original record', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    mocks.read.mockResolvedValue({
      value: JSON.stringify({
        version: 2,
        accountSubject: 'patient-1',
        tasksExpiresAt: Date.now() + 60_000,
        tasks: [cachedTask],
        pendingCheckins: [{
          clientRequestId: 'original-request',
          doseTaskId: 7,
          result: 'TAKEN',
          actualTakenAt: new Date().toISOString(),
          expiresAt: Date.now() + 60_000,
          correction: {
            clientRequestId: 'correction-request',
            result: 'MISSED',
            actualTakenAt: new Date().toISOString(),
            skipReason: 'PATIENT_REPORTED'
          }
        }]
      })
    })
    mocks.submitCheckin.mockResolvedValue({ data: { id: 701 } })
    mocks.correctCheckin.mockResolvedValue({ data: { id: 702 } })

    await expect(replayPendingCheckins()).resolves.toBe(1)

    expect(mocks.correctCheckin).toHaveBeenCalledWith(701, expect.objectContaining({
      clientRequestId: 'correction-request',
      result: 'MISSED'
    }))
  })

  test('keeps the confirmed original task when a queued correction has expired', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let storedValue = JSON.stringify({
      version: 2, accountSubject: 'patient-1', tasksExpiresAt: Date.now() + 60_000,
      tasks: [cachedTask],
      pendingCheckins: [{
        clientRequestId: 'original-request', doseTaskId: 7, result: 'TAKEN',
        actualTakenAt: new Date().toISOString(), expiresAt: Date.now() + 60_000,
        correction: { clientRequestId: 'correction-request', result: 'MISSED', actualTakenAt: new Date().toISOString() }
      }]
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    mocks.submitCheckin.mockResolvedValue({ data: { id: 701, result: 'TAKEN' } })
    mocks.correctCheckin.mockRejectedValue(Object.assign(new Error('DOSE_TASK_EDIT_WINDOW_EXPIRED'), { code: 40009 }))
    mocks.getDoseTasks.mockResolvedValue({ data: [{
      ...cachedTask, status: 'COMPLETED', lastEffectiveCheckinId: 701, notificationStatus: 'ACKED'
    }] })

    await expect(replayPendingCheckins()).resolves.toBe(0)

    expect(mocks.getDoseTasks).toHaveBeenCalledWith(cachedTask.taskDate, cachedTask.taskDate)
    expect(JSON.parse(storedValue).pendingCheckins).toEqual([])
    expect(JSON.parse(storedValue).tasks[0]).toMatchObject({ status: 'COMPLETED', lastEffectiveCheckinId: 701 })
  })

  test('retains an expired correction until the authoritative task can be fetched', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let storedValue = JSON.stringify({
      version: 2, accountSubject: 'patient-1', tasksExpiresAt: Date.now() + 60_000,
      tasks: [cachedTask],
      pendingCheckins: [{
        clientRequestId: 'original-request', doseTaskId: 7, result: 'TAKEN',
        actualTakenAt: new Date().toISOString(), expiresAt: Date.now() + 60_000,
        correction: { clientRequestId: 'correction-request', result: 'MISSED', actualTakenAt: new Date().toISOString() }
      }]
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    mocks.submitCheckin.mockResolvedValue({ data: { id: 701 } })
    mocks.correctCheckin.mockRejectedValue(Object.assign(new Error('DOSE_TASK_EDIT_WINDOW_EXPIRED'), { code: 40009 }))
    mocks.getDoseTasks.mockRejectedValue(new Error('Network error'))

    await expect(replayPendingCheckins()).resolves.toBe(0)
    expect(JSON.parse(storedValue).pendingCheckins).toHaveLength(1)
  })

  test('persists the effective task status and record ID before removing a replayed check-in', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let storedValue = JSON.stringify({
      version: 2, accountSubject: 'patient-1', tasksExpiresAt: Date.now() + 60_000,
      tasks: [cachedTask],
      pendingCheckins: [{
        clientRequestId: 'queued-request', doseTaskId: 7, result: 'TAKEN',
        actualTakenAt: new Date().toISOString(), expiresAt: Date.now() + 60_000
      }]
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    mocks.submitCheckin.mockResolvedValue({ data: { id: 701 } })

    await expect(replayPendingCheckins()).resolves.toBe(1)
    const saved = JSON.parse(storedValue)
    expect(saved.pendingCheckins).toEqual([])
    expect(saved.tasks[0]).toMatchObject({
      status: 'COMPLETED', lastEffectiveCheckinId: 701, notificationStatus: 'ACKED', reminderStage: 'HANDLED'
    })

    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    await expect(getCachedDoseTasks()).resolves.toEqual([expect.objectContaining({
      id: 7, status: 'COMPLETED', lastEffectiveCheckinId: 701
    })])
  })

  test('refreshes an ineffective idempotent replay from the authoritative server task', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let storedValue = JSON.stringify({
      version: 2, accountSubject: 'patient-1', tasksExpiresAt: Date.now() + 60_000,
      tasks: [cachedTask],
      pendingCheckins: [{
        clientRequestId: 'old-request', doseTaskId: 7, result: 'TAKEN',
        actualTakenAt: new Date().toISOString(), expiresAt: Date.now() + 60_000
      }]
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    mocks.submitCheckin.mockResolvedValue({ data: { id: 701, effective: false } })
    mocks.getDoseTasks.mockResolvedValue({ data: [{
      ...cachedTask, status: 'MISSED', lastEffectiveCheckinId: 888, notificationStatus: 'ACKED'
    }] })

    await expect(replayPendingCheckins()).resolves.toBe(1)

    expect(mocks.getDoseTasks).toHaveBeenCalledWith(cachedTask.taskDate, cachedTask.taskDate)
    expect(JSON.parse(storedValue).pendingCheckins).toEqual([])
    expect(JSON.parse(storedValue).tasks[0]).toMatchObject({ status: 'MISSED', lastEffectiveCheckinId: 888 })
  })

  test('retains an ineffective replay in the queue until the authoritative task can be fetched', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let storedValue = JSON.stringify({
      version: 2, accountSubject: 'patient-1', tasksExpiresAt: Date.now() + 60_000,
      tasks: [cachedTask],
      pendingCheckins: [{
        clientRequestId: 'old-request', doseTaskId: 7, result: 'TAKEN',
        actualTakenAt: new Date().toISOString(), expiresAt: Date.now() + 60_000
      }]
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    mocks.submitCheckin.mockResolvedValue({ data: { id: 701, effective: false } })
    mocks.getDoseTasks.mockRejectedValue(new Error('Network error'))

    await expect(replayPendingCheckins()).resolves.toBe(0)

    expect(JSON.parse(storedValue).pendingCheckins).toHaveLength(1)
    expect(JSON.parse(storedValue).tasks[0].lastEffectiveCheckinId).toBeUndefined()
  })

  test('keeps and replays a correction queued while the original request is in flight', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let storedValue = JSON.stringify({
      version: 2,
      accountSubject: 'patient-1',
      tasksExpiresAt: Date.now() + 60_000,
      tasks: [cachedTask],
      pendingCheckins: [{
        clientRequestId: 'original-request',
        doseTaskId: 7,
        result: 'TAKEN',
        actualTakenAt: new Date().toISOString(),
        expiresAt: Date.now() + 60_000
      }]
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    let resolveReplay!: (value: { data: { id: number } }) => void
    mocks.submitCheckin
      .mockImplementationOnce(() => new Promise((resolve) => { resolveReplay = resolve }))
      .mockResolvedValue({ data: { id: 701 } })
    mocks.correctCheckin.mockResolvedValue({ data: { id: 702 } })

    const replay = replayPendingCheckins()
    await vi.waitFor(() => expect(mocks.submitCheckin).toHaveBeenCalledTimes(1))
    const correction = submitOrQueueCheckin(cachedTask as any, 'MISSED')
    await vi.waitFor(() => expect(mocks.write).toHaveBeenCalledTimes(1))
    resolveReplay({ data: { id: 701 } })

    await expect(correction).resolves.toBe('submitted')
    await expect(replay).resolves.toBe(1)
    expect(mocks.submitCheckin).toHaveBeenCalledTimes(2)
    expect(mocks.correctCheckin).toHaveBeenCalledWith(701, expect.objectContaining({ result: 'MISSED' }))
    expect(JSON.parse(storedValue).pendingCheckins).toEqual([])
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
    mocks.clear.mockImplementation(async () => { storedValue = '' })
    mocks.submitCheckin
      .mockRejectedValueOnce(new Error('DOSE_TASK_NOT_FOUND'))
      .mockResolvedValueOnce({ data: { id: 703 } })

    await expect(replayPendingCheckins()).resolves.toBe(1)

    expect(mocks.submitCheckin).toHaveBeenCalledTimes(2)
    expect(JSON.parse(mocks.write.mock.calls[0][0].value).tasks).toEqual([])
    expect(storedValue).toBe('')
    expect(mocks.cancelReminder).toHaveBeenCalledWith(7)
  })

  test('isolates an expired correction, notifies the patient and continues replay', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let storedValue = JSON.stringify({
      version: 2,
      accountSubject: 'patient-1',
      tasksExpiresAt: Date.now() + 60_000,
      tasks: [cachedTask, { ...cachedTask, id: 8 }],
      pendingCheckins: [
        {
          clientRequestId: 'expired-correction-original', doseTaskId: 7, result: 'TAKEN',
          actualTakenAt: new Date().toISOString(), expiresAt: Date.now() + 60_000,
          correction: { clientRequestId: 'expired-correction', result: 'MISSED', actualTakenAt: new Date().toISOString() }
        },
        { clientRequestId: 'valid-task', doseTaskId: 8, result: 'TAKEN', actualTakenAt: new Date().toISOString(), expiresAt: Date.now() + 60_000 }
      ]
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    mocks.submitCheckin
      .mockResolvedValueOnce({ data: { id: 701 } })
      .mockResolvedValueOnce({ data: { id: 703 } })
    mocks.correctCheckin.mockRejectedValue(Object.assign(new Error('DOSE_TASK_EDIT_WINDOW_EXPIRED'), { code: 40009 }))
    mocks.getDoseTasks.mockResolvedValue({ data: [{ ...cachedTask, status: 'COMPLETED', lastEffectiveCheckinId: 701 }] })
    const conflicts: Event[] = []
    const listener = (event: Event) => conflicts.push(event)
    window.addEventListener(OFFLINE_CHECKIN_CONFLICT_EVENT, listener)

    await expect(replayPendingCheckins()).resolves.toBe(1)

    window.removeEventListener(OFFLINE_CHECKIN_CONFLICT_EVENT, listener)
    expect(mocks.submitCheckin).toHaveBeenCalledTimes(2)
    expect(JSON.parse(storedValue).pendingCheckins).toEqual([])
    expect(JSON.parse(storedValue).tasks[0]).toMatchObject({ status: 'COMPLETED', lastEffectiveCheckinId: 701 })
    expect(conflicts).toHaveLength(1)
    expect(consumeOfflineCheckinConflictCount()).toBe(1)
  })

  test('isolates an expired first check-in and continues replaying valid tasks', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    let storedValue = JSON.stringify({
      version: 2, accountSubject: 'patient-1', tasksExpiresAt: Date.now() + 60_000,
      tasks: [cachedTask, { ...cachedTask, id: 8 }],
      pendingCheckins: [
        { clientRequestId: 'expired-task', doseTaskId: 7, result: 'TAKEN', actualTakenAt: new Date().toISOString(), expiresAt: Date.now() + 60_000 },
        { clientRequestId: 'valid-task', doseTaskId: 8, result: 'TAKEN', actualTakenAt: new Date().toISOString(), expiresAt: Date.now() + 60_000 }
      ]
    })
    mocks.read.mockImplementation(async () => ({ value: storedValue }))
    mocks.write.mockImplementation(async ({ value }) => { storedValue = value })
    mocks.submitCheckin
      .mockRejectedValueOnce(Object.assign(new Error('DOSE_TASK_EDIT_WINDOW_EXPIRED'), { code: 40009 }))
      .mockResolvedValueOnce({ data: { id: 703 } })
    mocks.getDoseTasks.mockResolvedValue({ data: [cachedTask] })

    await expect(replayPendingCheckins()).resolves.toBe(1)

    expect(mocks.submitCheckin).toHaveBeenCalledTimes(2)
    expect(JSON.parse(storedValue).pendingCheckins).toEqual([])
    expect(consumeOfflineCheckinConflictCount()).toBe(1)
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
