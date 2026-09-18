import { Capacitor } from '@capacitor/core'
import { LocalNotifications } from '@capacitor/local-notifications'
import type { DoseTaskDTO } from '@/modules/patient/api/mvp'

export const GENERIC_REMINDER_TITLE = '用药提醒'
export const GENERIC_REMINDER_BODY = '您有一项用药任务待处理，请打开药按时查看。'

const notificationId = (taskId: number) => Math.abs(taskId % 2_000_000_000) + 1

let reminderOperationTail: Promise<void> = Promise.resolve()

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

export async function enableAndScheduleMedicationReminders(tasks: DoseTaskDTO[]) {
  if (!Capacitor.isNativePlatform()) return false
  return enqueueReminderOperation(async () => {
    let permission = await LocalNotifications.checkPermissions()
    if (permission.display === 'prompt' || permission.display === 'prompt-with-rationale') {
      permission = await LocalNotifications.requestPermissions()
    }
    if (permission.display !== 'granted') return false

    const now = Date.now()
    const notifications = tasks
      .filter((task) => task.status === 'PENDING')
      .filter((task) => Boolean(task.scheduledAt))
      .map((task) => ({ task, at: new Date(task.scheduledAt as string) }))
      .filter(({ at }) => !Number.isNaN(at.getTime()) && at.getTime() > now && at.getTime() <= now + 48 * 60 * 60 * 1000)
      .map(({ task, at }) => ({
        id: notificationId(task.id),
        title: GENERIC_REMINDER_TITLE,
        body: GENERIC_REMINDER_BODY,
        schedule: { at, allowWhileIdle: true },
        extra: { kind: 'MEDICATION_TASK', route: '/tabs/medication' }
      }))

    const desiredIds = new Set(notifications.map((notification) => notification.id))
    const pending = await LocalNotifications.getPending()
    const obsolete = pending.notifications
      .filter((notification) => notification.extra?.kind === 'MEDICATION_TASK' && !desiredIds.has(notification.id))
      .map((notification) => ({ id: notification.id }))
    if (obsolete.length) await LocalNotifications.cancel({ notifications: obsolete })
    if (notifications.length) await LocalNotifications.schedule({ notifications })
    return true
  })
}
