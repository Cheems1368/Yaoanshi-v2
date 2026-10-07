import { reactive } from 'vue'
import { Capacitor } from '@capacitor/core'
import { patientSession } from '@/app/session'
import type { DoseTaskDTO } from '@/modules/patient/api/mvp'
import { loadMedicationTasks, submitOrQueueCheckin } from '@/modules/patient/services/offlineMedication'
import { enableAndScheduleMedicationReminders, reminderOptions } from '@/modules/patient/services/localMedicationReminders'

const OPTIONS_KEY = 'patient-reminder-options'
export const reminderState = reactive({
  active: [] as DoseTaskDTO[], options: reminderOptions, settingsOpen: false,
  message: '', capability: '', busy: false
})
let tasks: DoseTaskDTO[] = []
let owner: string | null = null
let syncing = false
let started = false
let lastTick = Date.now()
let audio: AudioContext | null = null
const dismissed = new Set<number>()

export async function syncReminders() {
  const subject = patientSession.getAccountSubject()
  if (syncing || !subject || patientSession.getAccountType() !== 'PATIENT'
    || !['authenticated', 'offline'].includes(patientSession.getAuthStatus())
    || !patientSession.getGateState().isBound) return
  syncing = true
  const generation = patientSession.getSessionGeneration()
  try {
    const result = await loadMedicationTasks(7)
    if (patientSession.getAccountSubject() !== subject || patientSession.getSessionGeneration() !== generation) return
    if (owner !== subject) { dismissed.clear(); reminderState.active = []; lastTick = Date.now() }
    owner = subject
    tasks = result.tasks
    reminderState.active = reminderState.active.filter(t => tasks.some(x => x.id === t.id && x.status === 'PENDING'))
    if (!result.offline) await enableAndScheduleMedicationReminders(tasks, 'PATIENT')
  } catch {
    reminderState.message = '提醒暂时无法同步，请联网后重试。'
  } finally { syncing = false }
}

export async function enableReminders() {
  if (!Capacitor.isNativePlatform()) {
    try { audio ??= new AudioContext(); await audio.resume() }
    catch { reminderState.capability = '此浏览器无法播放提醒声音，仍可查看前台提醒。'; return }
    reminderState.capability = '网页仅在前台提醒；震动效果取决于浏览器和设备。'
    await syncReminders()
    return
  }
  await syncReminders()
  const result = await enableAndScheduleMedicationReminders(tasks, patientSession.getAccountType() || undefined)
  reminderState.capability = result === 'exact' ? '系统提醒已启用；声音和震动遵循手机通知、静音及勿扰设置。'
    : result === 'inexact' ? '精确提醒未授权，后台通知可能延迟。' : '系统通知未授权，仍可在前台查看提醒。'
}

export async function setReminderOption(name: 'sound' | 'vibration', value: boolean) {
  reminderState.options[name] = value
  reminderOptions[name] = value
  // Only two nonmedical preferences persist here. Tasks and checkins use the existing encrypted cache.
  localStorage.setItem(OPTIONS_KEY, JSON.stringify(reminderState.options))
  await enableAndScheduleMedicationReminders(tasks, patientSession.getAccountType() || undefined)
}

export function closeReminder() { reminderState.active = [] }

export async function recordTaken(task: DoseTaskDTO) {
  if (reminderState.busy || owner !== patientSession.getAccountSubject()) return
  reminderState.busy = true
  try {
    const result = await submitOrQueueCheckin(task, 'TAKEN', { actualTakenAt: new Date().toISOString() })
    reminderState.message = result === 'queued' ? '已加密保存服药记录，待联网同步。' : '服药记录已同步。'
    reminderState.active = reminderState.active.filter(t => t.id !== task.id)
    window.dispatchEvent(new Event('medication-checkin-updated'))
    await syncReminders()
  } catch {
    reminderState.message = '记录未保存，请稍后重试或在服药记录页确认。'
  } finally { reminderState.busy = false }
}

function reset() {
  owner = null; tasks = []; dismissed.clear()
  reminderState.active = []; reminderState.settingsOpen = false; reminderState.message = ''
}

function tick() {
  const now = Date.now()
  if (owner === patientSession.getAccountSubject() && document.visibilityState === 'visible'
    && ['authenticated', 'offline'].includes(patientSession.getAuthStatus())) {
    const due = tasks.filter(t => t.status === 'PENDING' && t.scheduledAt && !dismissed.has(t.id)
      && Date.parse(t.scheduledAt) > lastTick && Date.parse(t.scheduledAt) <= now)
    if (due.length) {
      due.forEach(t => dismissed.add(t.id))
      reminderState.active = [...reminderState.active, ...due]
      if (!Capacitor.isNativePlatform() && reminderState.options.sound && audio?.state === 'running') {
        const oscillator = audio.createOscillator(); const gain = audio.createGain()
        gain.gain.value = 0.12; oscillator.frequency.value = 880
        oscillator.connect(gain).connect(audio.destination)
        oscillator.start(); oscillator.stop(audio.currentTime + 0.7)
      }
      if (!Capacitor.isNativePlatform() && reminderState.options.vibration && typeof navigator.vibrate === 'function') {
        navigator.vibrate([300, 150, 300])
      }
    }
  }
  lastTick = now
}

export function startReminders() {
  if (started) return
  started = true
  try {
    const saved = JSON.parse(localStorage.getItem(OPTIONS_KEY) || '{}')
    for (const name of ['sound', 'vibration'] as const) {
      if (typeof saved[name] === 'boolean') { reminderState.options[name] = saved[name]; reminderOptions[name] = saved[name] }
    }
  } catch { /* Invalid nonmedical preferences retain defaults. */ }
  for (const event of ['patient-account-switched', 'patient-session-anonymous', 'patient-session-expired']) {
    window.addEventListener(event, reset)
  }
  for (const event of ['patient-session-authenticated', 'online', 'medication-checkin-updated']) {
    window.addEventListener(event, () => void syncReminders())
  }
  document.addEventListener('visibilitychange', () => { if (document.visibilityState === 'visible') void syncReminders() })
  window.setInterval(tick, 1000)
  window.setInterval(() => { if (document.visibilityState === 'visible') void syncReminders() }, 60000)
  void syncReminders()
}
