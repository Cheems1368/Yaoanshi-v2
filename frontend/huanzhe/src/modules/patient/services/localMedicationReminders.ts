import { Capacitor } from '@capacitor/core'
import { LocalNotifications } from '@capacitor/local-notifications'
import type { DoseTaskDTO } from '@/modules/patient/api/mvp'

export const GENERIC_REMINDER_TITLE = '用药提醒'
export const GENERIC_REMINDER_BODY = '您有一项用药任务待处理，请打开药按时查看。'
const MEDICATION_REMINDER_ROUTE = '/medication-reminder'

const notificationId = (taskId: number) => Math.abs(taskId % 2_000_000_000) + 1

let reminderOperationTail: Promise<void> = Promise.resolve()
let exactSettingRequested = false

function enqueueReminderOperation<T>(operation: () => Promise<T>): Promise<T> {
  const current = reminderOperationTail.then(operation)
  reminderOperationTail = current.then(() => undefined, () => undefined)
  return current
}

export async function cancelMedicationReminder(taskId: number) {
  if (!Capacitor.isNativePlatform()) return
  await enqueueReminderOperation(() => LocalNotifications.cancel({ notifications: [{ id: notificationId(taskId) }] }))
}

export async function cancelAllMedicationReminders() {
  if (!Capacitor.isNativePlatform()) return
  await enqueueReminderOperation(async () => {
    const pending = await LocalNotifications.getPending()
    const medicationNotifications = pending.notifications
      .filter((notification) => notification.extra?.kind === 'MEDICATION_TASK')
      .map((notification) => ({ id: notification.id }))
    if (medicationNotifications.length) {
      await LocalNotifications.cancel({ notifications: medicationNotifications })
    }
  })
}

export async function registerMedicationReminderActionHandler(
  navigate: (route: string) => void | Promise<void>
) {
  if (!Capacitor.isNativePlatform()) return
  return LocalNotifications.addListener('localNotificationActionPerformed', (action) => {
    if (action.notification.extra?.kind === 'MEDICATION_TASK') {
      void navigate(MEDICATION_REMINDER_ROUTE)
    }
  })
}

export async function enableAndScheduleMedicationReminders(tasks: DoseTaskDTO[], accountType = 'PATIENT') {
  if (!Capacitor.isNativePlatform()) return false
  return enqueueReminderOperation(async () => {
    const now = Date.now()
    const reminderDelayMs = accountType.toUpperCase() === 'FAMILY' ? 30 * 60 * 1000 : 0
    const reminderCandidates = tasks
      .filter((task) => task.status === 'PENDING')
      .filter((task) => Boolean(task.scheduledAt))
      .map((task) => ({ task, at: new Date(new Date(task.scheduledAt as string).getTime() + reminderDelayMs) }))
      .filter(({ at }) => !Number.isNaN(at.getTime()) && at.getTime() > now && at.getTime() <= now + 48 * 60 * 60 * 1000)

    const desiredIds = new Set(reminderCandidates.map(({ task }) => notificationId(task.id)))
    const pending = await LocalNotifications.getPending()
    const obsolete = pending.notifications
      .filter((notification) => notification.extra?.kind === 'MEDICATION_TASK' && !desiredIds.has(notification.id))
      .map((notification) => ({ id: notification.id }))
    if (obsolete.length) await LocalNotifications.cancel({ notifications: obsolete })
    if (!reminderCandidates.length) return true

    let permission = await LocalNotifications.checkPermissions()
    if (permission.display === 'prompt' || permission.display === 'prompt-with-rationale') {
      permission = await LocalNotifications.requestPermissions()
    }
    if (permission.display !== 'granted') return false

    let exactAlarmGranted = true
    if (Capacitor.getPlatform() === 'android') {
      let exactSetting = await LocalNotifications.checkExactNotificationSetting()
      if (exactSetting.exact_alarm !== 'granted' && !exactSettingRequested) {
        exactSettingRequested = true
        await LocalNotifications.changeExactNotificationSetting()
        exactSetting = await LocalNotifications.checkExactNotificationSetting()
      }
      exactAlarmGranted = exactSetting.exact_alarm === 'granted'
    }

    const notifications = reminderCandidates.map(({ task, at }) => ({
        id: notificationId(task.id),
        title: GENERIC_REMINDER_TITLE,
        body: GENERIC_REMINDER_BODY,
        schedule: { at, allowWhileIdle: exactAlarmGranted },
        extra: { kind: 'MEDICATION_TASK', route: MEDICATION_REMINDER_ROUTE }
      }))

    await LocalNotifications.schedule({ notifications })
    return exactAlarmGranted ? 'exact' : 'inexact'
  })
}
