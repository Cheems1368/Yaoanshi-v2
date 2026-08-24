import { beforeEach, describe, expect, test, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  read: vi.fn(), write: vi.fn(), clear: vi.fn(), getDoseTasks: vi.fn(), submitCheckin: vi.fn()
}))

vi.mock('@capacitor/core', () => ({
  Capacitor: { isNativePlatform: () => true },
  registerPlugin: () => ({ read: mocks.read, write: mocks.write, clear: mocks.clear })
}))

vi.mock('@/modules/patient/api/mvp', () => ({
  medicationService: { getDoseTasks: mocks.getDoseTasks, submitCheckin: mocks.submitCheckin }
}))

vi.mock('@/app/session', () => ({
  patientSession: { getAccountSubject: () => 'patient-1', getAccessToken: () => 'access-token' }
}))

import { loadMedicationTasks, submitOrQueueCheckin } from '@/modules/patient/services/offlineMedication'

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
    Object.values(mocks).forEach((mock) => mock.mockReset())
    mocks.read.mockResolvedValue({
      value: JSON.stringify({ version: 1, accountSubject: 'patient-1', expiresAt: Date.now() + 60_000, tasks: [cachedTask], pendingCheckins: [] })
    })
    mocks.write.mockResolvedValue(undefined)
    mocks.clear.mockResolvedValue(undefined)
  })

  test('keeps encrypted cached tasks when the API is unreachable', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    mocks.getDoseTasks.mockRejectedValue(new Error('Network error'))
    const result = await loadMedicationTasks()
    expect(result).toEqual({ tasks: [cachedTask], offline: true })
  })

  test('queues a stable idempotent request only for an offline network failure', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: false })
    mocks.submitCheckin.mockRejectedValue(new Error('Network error'))
    await expect(submitOrQueueCheckin(cachedTask as any, 'TAKEN')).resolves.toBe('queued')
    const saved = JSON.parse(mocks.write.mock.calls[0][0].value)
    expect(saved.pendingCheckins).toHaveLength(1)
    expect(saved.pendingCheckins[0].clientRequestId).toBeTruthy()
  })

  test('does not queue online business failures', async () => {
    Object.defineProperty(navigator, 'onLine', { configurable: true, value: true })
    mocks.submitCheckin.mockRejectedValue(new Error('CHECKIN_WINDOW_EXPIRED'))
    await expect(submitOrQueueCheckin(cachedTask as any, 'TAKEN')).rejects.toThrow('CHECKIN_WINDOW_EXPIRED')
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
})
