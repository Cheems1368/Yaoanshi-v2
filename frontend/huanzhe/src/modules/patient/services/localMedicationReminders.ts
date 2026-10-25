import { Capacitor } from '@capacitor/core'
import { LocalNotifications } from '@capacitor/local-notifications'
import type { DoseTaskDTO } from '@/modules/patient/api/mvp'

export const GENERIC_REMINDER_TITLE = '用药提醒'
export const GENERIC_REMINDER_BODY = '您有一项用药任务待处理，请打开药按时查看。'
const MEDICATION_REMINDER_ROUTE = '/medication-reminder'
export interface ReminderOptions { sound: boolean; vibration: boolean }
export const reminderOptions: ReminderOptions = { sound: true, vibration: true }

const notificationId = (taskId: number) => Math.abs(taskId % 2_000_000_000) + 1
const isLegacyReminder = (notification: { title?: string; extra?: Record<string, unknown> }) =>
  typeof notification.extra?.scope === 'string' && Array.isArray(notification.extra?.ids)
  && notification.title?.startsWith('该服药了') === true
const isDeliveredMedicationReminder = (notification: { title?: string; body?: string; extra?: Record<string, unknown> }) =>
  notification.extra?.kind === 'MEDICATION_TASK' || isLegacyReminder(notification)
  || (notification.title === GENERIC_REMINDER_TITLE && notification.body === GENERIC_REMINDER_BODY)
  || notification.title?.startsWith('该服药了 · ') === true
const groupIdsByTask = new Map<number, number>()

let reminderOperationTail: Promise<void> = Promise.resolve()
let exactSettingRequested = false

function enqueueReminderOperation<T>(operation: () => Promise<T>): Promise<T> {
  const current = reminderOperationTail.then(operation)
  reminderOperationTail = current.then(() => undefined, () => undefined)
  return current
}

export async function cancelMedicationReminder(taskId: number) {
  if (!Capacitor.isNativePlatform()) return
  await enqueueReminderOperation(async () => {
    const pending = await LocalNotifications.getPending()
    const grouped = pending.notifications.find(n => n.extra?.kind === 'MEDICATION_TASK'
      && n.extra?.taskIds?.includes(taskId))
    const delivered = await LocalNotifications.getDeliveredNotifications()
    const deliveredGroup = delivered.notifications.find(n => isDeliveredMedicationReminder(n)
      && (n.extra?.taskIds?.includes(taskId) || n.id === groupIdsByTask.get(taskId)))
    await LocalNotifications.cancel({ notifications: [{ id: grouped?.id ?? deliveredGroup?.id ?? notificationId(taskId) }] })
    if (grouped) {
      const remaining = (grouped.extra.taskIds as number[]).filter(id => id !== taskId)
      if (remaining.length) await LocalNotifications.schedule({ notifications: [{ ...grouped,
        extra: { ...grouped.extra, taskIds: remaining } }] })
    }
  })
}

export async function cancelAllMedicationReminders() {
  if (!Capacitor.isNativePlatform()) return
  await enqueueReminderOperation(async () => {
    const pending = await LocalNotifications.getPending()
    const medicationNotifications = pending.notifications
      .filter((notification) => notification.extra?.kind === 'MEDICATION_TASK' || isLegacyReminder(notification))
      .map((notification) => ({ id: notification.id }))
    if (medicationNotifications.length) {
      await LocalNotifications.cancel({ notifications: medicationNotifications })
    }
    const delivered = await LocalNotifications.getDeliveredNotifications()
    await LocalNotifications.removeDeliveredNotifications({ notifications: delivered.notifications
      .filter(isDeliveredMedicationReminder) })
    groupIdsByTask.clear()
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

export async function enableAndScheduleMedicationReminders(tasks: DoseTaskDTO[], accountType?: string,
  options: ReminderOptions = reminderOptions) {
  if (!Capacitor.isNativePlatform()) return false
  return enqueueReminderOperation(async () => {
    const now = Date.now()
    const normalizedAccountType = accountType?.toUpperCase()
    const reminderDelayMs = normalizedAccountType === 'FAMILY' ? 30 * 60 * 1000 : 0
    const pendingTasks = tasks
      .filter((task) => task.status === 'PENDING')
      .filter((task) => Boolean(task.scheduledAt))
    const taskGroups = new Map<string, DoseTaskDTO[]>()
    for (const task of tasks.filter(t => t.status !== 'CANCELLED' && t.scheduledAt)) {
      const group = taskGroups.get(task.scheduledAt as string) || []
      group.push(task); taskGroups.set(task.scheduledAt as string, group)
    }
    for (const group of taskGroups.values()) {
      const id = notificationId(Math.max(...group.map(t => t.id)))
      for (const task of group) groupIdsByTask.set(task.id, id)
    }
    const reminderCandidates = normalizedAccountType ? pendingTasks
      .map((task) => ({ task, at: new Date(new Date(task.scheduledAt as string).getTime() + reminderDelayMs) }))
      .filter(({ at }) => !Number.isNaN(at.getTime()) && at.getTime() > now && at.getTime() <= now + 7 * 24 * 60 * 60 * 1000) : []
    const groups = new Map<number, typeof reminderCandidates>()
    for (const candidate of reminderCandidates) {
      const group = groups.get(candidate.at.getTime()) || []
      group.push(candidate)
      groups.set(candidate.at.getTime(), group)
    }

    const desiredIds = new Set(
      (normalizedAccountType ? [...groups.values()].map(group => group[0].task) : pendingTasks)
        .map((task) => normalizedAccountType ? (groupIdsByTask.get(task.id) ?? notificationId(task.id)) : notificationId(task.id))
    )
    const pending = await LocalNotifications.getPending()
    const obsolete = pending.notifications
      .filter((notification) => isLegacyReminder(notification)
        || (notification.extra?.kind === 'MEDICATION_TASK' && !desiredIds.has(notification.id)
          && (normalizedAccountType || !notification.extra?.taskIds?.some((id: number) => pendingTasks.some(task => task.id === id)))))
      .map((notification) => ({ id: notification.id }))
    if (obsolete.length) await LocalNotifications.cancel({ notifications: obsolete })
    const delivered = await LocalNotifications.getDeliveredNotifications()
    const obsoleteDelivered = delivered.notifications.filter(notification => isDeliveredMedicationReminder(notification)
      && (notification.title?.startsWith('该服药了 · ')
        || !pendingTasks.some(task => notification.extra?.taskIds?.includes(task.id)
          || notification.id === (groupIdsByTask.get(task.id) ?? notificationId(task.id)))))
    if (obsoleteDelivered.length) await LocalNotifications.removeDeliveredNotifications({ notifications: obsoleteDelivered })
    if (!normalizedAccountType || !reminderCandidates.length) return true

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

    const channelId = `medication-${Number(options.sound)}-${Number(options.vibration)}`
    if (Capacitor.getPlatform() === 'android') {
      await LocalNotifications.createChannel({ id: channelId, name: '用药提醒', importance: 4,
        visibility: 1, vibration: options.vibration, sound: options.sound ? 'medication_reminder.wav' : undefined })
    }
    const notifications = [...groups.values()].map(group => ({
        id: groupIdsByTask.get(group[0].task.id) ?? notificationId(group[0].task.id),
        title: GENERIC_REMINDER_TITLE,
        body: GENERIC_REMINDER_BODY,
        channelId,
        schedule: { at: group[0].at, allowWhileIdle: exactAlarmGranted },
        extra: { kind: 'MEDICATION_TASK', route: MEDICATION_REMINDER_ROUTE, taskIds: group.map(({ task }) => task.id) }
      }))

    await LocalNotifications.schedule({ notifications })
    return exactAlarmGranted ? 'exact' : 'inexact'
  })
}
