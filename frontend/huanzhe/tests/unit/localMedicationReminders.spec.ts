import { beforeEach, describe, expect, test, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  checkPermissions: vi.fn(), requestPermissions: vi.fn(), getPending: vi.fn(), cancel: vi.fn(), schedule: vi.fn()
}))

vi.mock('@capacitor/core', () => ({ Capacitor: { isNativePlatform: () => true } }))
vi.mock('@capacitor/local-notifications', () => ({ LocalNotifications: mocks }))

import { enableAndScheduleMedicationReminders } from '@/modules/patient/services/localMedicationReminders'

describe('local medication reminder lifecycle', () => {
  beforeEach(() => {
    Object.values(mocks).forEach((mock) => mock.mockReset())
    mocks.checkPermissions.mockResolvedValue({ display: 'granted' })
    mocks.cancel.mockResolvedValue(undefined)
    mocks.schedule.mockResolvedValue(undefined)
  })

  test('cancels a previously scheduled reminder after the task is completed', async () => {
    mocks.getPending.mockResolvedValue({
      notifications: [{ id: 8, extra: { kind: 'MEDICATION_TASK' } }, { id: 99, extra: { kind: 'OTHER' } }]
    })
    await enableAndScheduleMedicationReminders([{
      id: 7,
      status: 'COMPLETED',
      scheduledAt: new Date(Date.now() + 60_000).toISOString()
    } as any])
    expect(mocks.cancel).toHaveBeenCalledWith({ notifications: [{ id: 8 }] })
    expect(mocks.schedule).not.toHaveBeenCalled()
  })
})
