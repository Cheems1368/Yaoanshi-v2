import { beforeEach, describe, expect, test, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  checkPermissions: vi.fn(), requestPermissions: vi.fn(), getPending: vi.fn(), cancel: vi.fn(), schedule: vi.fn(), addListener: vi.fn()
}))

vi.mock('@capacitor/core', () => ({ Capacitor: { isNativePlatform: () => true } }))
vi.mock('@capacitor/local-notifications', () => ({ LocalNotifications: mocks }))

import {
  cancelAllMedicationReminders,
  cancelMedicationReminder,
  enableAndScheduleMedicationReminders,
  registerMedicationReminderActionHandler
} from '@/modules/patient/services/localMedicationReminders'

describe('local medication reminder lifecycle', () => {
  beforeEach(() => {
    Object.values(mocks).forEach((mock) => mock.mockReset())
    mocks.checkPermissions.mockResolvedValue({ display: 'granted' })
    mocks.cancel.mockResolvedValue(undefined)
    mocks.schedule.mockResolvedValue(undefined)
    mocks.addListener.mockResolvedValue({ remove: vi.fn() })
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

  test('cancels the matching reminder immediately after an offline checkin', async () => {
    await cancelMedicationReminder(7)
    expect(mocks.cancel).toHaveBeenCalledWith({ notifications: [{ id: 8 }] })
  })

  test('cancels every medication reminder while preserving unrelated notifications', async () => {
    mocks.getPending.mockResolvedValue({
      notifications: [{ id: 8, extra: { kind: 'MEDICATION_TASK' } }, { id: 99, extra: { kind: 'OTHER' } }]
    })

    await cancelAllMedicationReminders()

    expect(mocks.cancel).toHaveBeenCalledWith({ notifications: [{ id: 8 }] })
  })

  test('waits for in-flight scheduling before clearing all medication reminders', async () => {
    mocks.getPending
      .mockResolvedValueOnce({ notifications: [] })
      .mockResolvedValueOnce({ notifications: [{ id: 8, extra: { kind: 'MEDICATION_TASK' } }] })
    let releaseSchedule!: () => void
    mocks.schedule.mockImplementation(() => new Promise<void>((resolve) => { releaseSchedule = resolve }))

    const scheduling = enableAndScheduleMedicationReminders([{
      id: 7,
      status: 'PENDING',
      scheduledAt: new Date(Date.now() + 60_000).toISOString()
    } as any])
    await vi.waitFor(() => expect(mocks.schedule).toHaveBeenCalledTimes(1))
    const clearing = cancelAllMedicationReminders()
    expect(mocks.getPending).toHaveBeenCalledTimes(1)

    releaseSchedule()
    await Promise.all([scheduling, clearing])

    expect(mocks.cancel).toHaveBeenCalledWith({ notifications: [{ id: 8 }] })
    expect(mocks.schedule.mock.invocationCallOrder[0]).toBeLessThan(mocks.cancel.mock.invocationCallOrder[0])
  })

  test('opens the formal reminder route when a medication notification is tapped', async () => {
    const navigate = vi.fn().mockResolvedValue(undefined)
    await registerMedicationReminderActionHandler(navigate)
    const listener = mocks.addListener.mock.calls[0][1]

    await listener({ notification: { extra: { kind: 'MEDICATION_TASK', route: '/untrusted' } } })

    expect(mocks.addListener).toHaveBeenCalledWith('localNotificationActionPerformed', expect.any(Function))
    expect(navigate).toHaveBeenCalledWith('/medication-reminder')
  })
})
