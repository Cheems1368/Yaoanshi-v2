import { beforeEach, describe, expect, test, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  checkPermissions: vi.fn(), requestPermissions: vi.fn(), getPending: vi.fn(), cancel: vi.fn(), schedule: vi.fn(), addListener: vi.fn(),
  checkExactNotificationSetting: vi.fn(), changeExactNotificationSetting: vi.fn(), createChannel: vi.fn(),
  getDeliveredNotifications: vi.fn(), removeDeliveredNotifications: vi.fn()
}))

vi.mock('@capacitor/core', () => ({ Capacitor: { isNativePlatform: () => true, getPlatform: () => 'android' } }))
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
    mocks.checkExactNotificationSetting.mockResolvedValue({ exact_alarm: 'granted' })
    mocks.changeExactNotificationSetting.mockResolvedValue({ exact_alarm: 'granted' })
    mocks.createChannel.mockResolvedValue(undefined)
    mocks.getPending.mockResolvedValue({ notifications: [] })
    mocks.getDeliveredNotifications.mockResolvedValue({ notifications: [] })
    mocks.removeDeliveredNotifications.mockResolvedValue(undefined)
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

  test('cancels obsolete reminders even after display permission is revoked', async () => {
    mocks.checkPermissions.mockResolvedValue({ display: 'denied' })
    mocks.getPending.mockResolvedValue({
      notifications: [{ id: 8, extra: { kind: 'MEDICATION_TASK' } }]
    })

    await enableAndScheduleMedicationReminders([])

    expect(mocks.cancel).toHaveBeenCalledWith({ notifications: [{ id: 8 }] })
    expect(mocks.schedule).not.toHaveBeenCalled()
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
    } as any], 'PATIENT')
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

  test('requests exact-alarm access and explicitly degrades when it remains denied', async () => {
    mocks.checkExactNotificationSetting.mockResolvedValue({ exact_alarm: 'denied' })
    mocks.getPending.mockResolvedValue({ notifications: [] })
    const result = await enableAndScheduleMedicationReminders([{
      id: 7,
      status: 'PENDING',
      scheduledAt: new Date(Date.now() + 60_000).toISOString()
    } as any], 'PATIENT')

    expect(mocks.changeExactNotificationSetting).toHaveBeenCalled()
    expect(mocks.schedule.mock.calls[0][0].notifications[0].schedule.allowWhileIdle).toBe(false)
    expect(result).toBe('inexact')
  })

  test('schedules family reminders thirty minutes after the patient dose time', async () => {
    mocks.getPending.mockResolvedValue({ notifications: [] })
    const scheduledAt = new Date(Date.now() - 10 * 60 * 1000)

    await enableAndScheduleMedicationReminders([{
      id: 7,
      status: 'PENDING',
      scheduledAt: scheduledAt.toISOString()
    } as any], 'FAMILY')

    expect(mocks.schedule.mock.calls[0][0].notifications[0].schedule.at.getTime())
      .toBe(scheduledAt.getTime() + 30 * 60 * 1000)
  })

  test('removes legacy unpublished reminders during upgrade without cancelling unrelated notifications', async () => {
    mocks.getPending.mockResolvedValue({ notifications: [
      { id: 90, title: '该服药了 · 20:13', extra: { scope: 'legacy', ids: [1] } },
      { id: 91, title: '其他提醒', extra: { kind: 'OTHER' } }
    ] })
    await enableAndScheduleMedicationReminders([], 'PATIENT')
    expect(mocks.cancel).toHaveBeenCalledWith({ notifications: [{ id: 90 }] })
  })

  test('groups same-time tasks across seven days without disclosing medication fields', async () => {
    mocks.getPending.mockResolvedValue({ notifications: [] })
    const scheduledAt = new Date(Date.now() + 6 * 86400_000).toISOString()
    await enableAndScheduleMedicationReminders([
      { id: 7, status: 'PENDING', scheduledAt, drugName: 'PRIVATE-A' },
      { id: 8, status: 'PENDING', scheduledAt, drugName: 'PRIVATE-B' }
    ] as any, 'PATIENT', { sound: false, vibration: true })
    const notifications = mocks.schedule.mock.calls[0][0].notifications
    expect(notifications).toHaveLength(1)
    expect(notifications[0].extra.taskIds).toEqual([7, 8])
    expect(notifications[0].channelId).toBe('medication-0-1')
    expect(JSON.stringify(notifications)).not.toContain('PRIVATE')
  })

  test('preserves the grouped reminder for medications not yet taken', async () => {
    mocks.getPending.mockResolvedValue({ notifications: [{ id: 8, title: '用药提醒', body: '待处理',
      extra: { kind: 'MEDICATION_TASK', taskIds: [7, 8] } }] })
    await cancelMedicationReminder(7)
    expect(mocks.cancel).toHaveBeenCalledWith({ notifications: [{ id: 8 }] })
    expect(mocks.schedule.mock.calls[0][0].notifications[0].extra.taskIds).toEqual([8])
  })

  test('cancels an already delivered group when its medication is recorded', async () => {
    mocks.getDeliveredNotifications.mockResolvedValue({ notifications: [{ id: 8,
      extra: { kind: 'MEDICATION_TASK', taskIds: [7, 9] } }] })
    await cancelMedicationReminder(9)
    expect(mocks.cancel).toHaveBeenCalledWith({ notifications: [{ id: 8 }] })
  })

  test.each([[false, false], [false, true], [true, false], [true, true]])(
    'selects the channel for sound=%s and vibration=%s', async (sound, vibration) => {
      await enableAndScheduleMedicationReminders([{
        id: 7, status: 'PENDING', scheduledAt: new Date(Date.now() + 60_000).toISOString()
      } as any], 'PATIENT', { sound, vibration })
      expect(mocks.schedule.mock.calls[0][0].notifications[0].channelId)
        .toBe(`medication-${Number(sound)}-${Number(vibration)}`)
    })

  test('cleans completed reminders but preserves pending reminders when account type is unavailable', async () => {
    mocks.getPending.mockResolvedValue({
      notifications: [
        { id: 8, extra: { kind: 'MEDICATION_TASK' } },
        { id: 9, extra: { kind: 'MEDICATION_TASK' } }
      ]
    })

    await enableAndScheduleMedicationReminders([
      { id: 7, status: 'PENDING', scheduledAt: new Date(Date.now() + 60_000).toISOString() } as any,
      { id: 8, status: 'COMPLETED', scheduledAt: new Date(Date.now() + 60_000).toISOString() } as any
    ])

    expect(mocks.cancel).toHaveBeenCalledWith({ notifications: [{ id: 9 }] })
    expect(mocks.schedule).not.toHaveBeenCalled()
  })
})
