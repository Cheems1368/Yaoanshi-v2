import { Capacitor, registerPlugin } from '@capacitor/core'
import { medicationService, type DoseTaskDTO } from '@/modules/patient/api/mvp'
import { patientSession } from '@/app/session'

const CACHE_TTL_MS = 48 * 60 * 60 * 1000

interface PendingCheckin {
  clientRequestId: string
  doseTaskId: number
  result: 'TAKEN' | 'MISSED'
  actualTakenAt: string
  note?: string
  skipReason?: string
}

interface MedicalCacheState {
  version: 1
  accountSubject: string
  expiresAt: number
  tasks: DoseTaskDTO[]
  pendingCheckins: PendingCheckin[]
}

interface SecureMedicalCachePlugin {
  read(): Promise<{ value: string }>
  write(options: { value: string }): Promise<void>
  clear(): Promise<void>
}

const SecureMedicalCache = registerPlugin<SecureMedicalCachePlugin>('SecureMedicalCache')
const isNative = () => Capacitor.isNativePlatform()

const emptyState = (): MedicalCacheState => ({
  version: 1,
  accountSubject: patientSession.getAccountSubject() ?? '',
  expiresAt: Date.now() + CACHE_TTL_MS,
  tasks: [],
  pendingCheckins: []
})

async function readState(): Promise<MedicalCacheState> {
  if (!isNative()) return emptyState()
  try {
    const { value } = await SecureMedicalCache.read()
    if (!value) return emptyState()
    const parsed = JSON.parse(value) as MedicalCacheState
    const accountSubject = patientSession.getAccountSubject()
    if (parsed.version !== 1 || parsed.expiresAt <= Date.now()) {
      await SecureMedicalCache.clear()
      return emptyState()
    }
    if (!accountSubject) return emptyState()
    if (parsed.accountSubject !== accountSubject) {
      await SecureMedicalCache.clear()
      return emptyState()
    }
    return parsed
  } catch {
    await SecureMedicalCache.clear().catch(() => undefined)
    return emptyState()
  }
}

async function writeState(state: MedicalCacheState) {
  if (!isNative()) return
  const accountSubject = patientSession.getAccountSubject()
  if (!accountSubject || !state.accountSubject || state.accountSubject !== accountSubject) {
    throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
  }
  state.expiresAt = Date.now() + CACHE_TTL_MS
  await SecureMedicalCache.write({ value: JSON.stringify(state) })
}

const makeRequestId = () => globalThis.crypto?.randomUUID?.()
  ?? `checkin-${Date.now()}-${Math.random().toString(16).slice(2)}`

export async function cacheDoseTasks(tasks: DoseTaskDTO[]) {
  const state = await readState()
  state.tasks = tasks
  await writeState(state)
}

export async function getCachedDoseTasks() {
  return (await readState()).tasks
}

const formatLocalDate = (date: Date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export async function loadMedicationTasks(days = 3): Promise<{ tasks: DoseTaskDTO[]; offline: boolean }> {
  const dateFrom = new Date()
  const dateTo = new Date()
  dateTo.setDate(dateTo.getDate() + Math.min(Math.max(days, 1), 7) - 1)
  try {
    await replayPendingCheckins()
    const response = await medicationService.getDoseTasks(formatLocalDate(dateFrom), formatLocalDate(dateTo))
    const tasks = response.data ?? []
    await cacheDoseTasks(tasks)
    return { tasks, offline: false }
  } catch (error) {
    if (!isNative()) throw error
    return { tasks: await getCachedDoseTasks(), offline: true }
  }
}

export async function submitOrQueueCheckin(
  task: DoseTaskDTO,
  result: 'TAKEN' | 'MISSED'
): Promise<'submitted' | 'queued'> {
  const payload: PendingCheckin = {
    clientRequestId: makeRequestId(),
    doseTaskId: task.id,
    result,
    actualTakenAt: new Date().toISOString(),
    note: result === 'TAKEN' ? '患者端提交' : '',
    skipReason: result === 'MISSED' ? 'PATIENT_REPORTED' : ''
  }

  try {
    await medicationService.submitCheckin(payload)
    return 'submitted'
  } catch (error) {
    const message = error instanceof Error ? error.message : ''
    if (!isNative() || (navigator.onLine && message !== 'Network error')) throw error
    const state = await readState()
    if (!state.pendingCheckins.some((item) => item.clientRequestId === payload.clientRequestId)) {
      state.pendingCheckins.push(payload)
    }
    state.tasks = state.tasks.map((item) => item.id === task.id
      ? { ...item, status: result === 'TAKEN' ? 'COMPLETED' : 'MISSED' }
      : item)
    await writeState(state)
    return 'queued'
  }
}

interface ReplayAttempt {
  accountSubject: string
  promise: Promise<number>
}

let replayAttempt: ReplayAttempt | null = null

async function replayPendingCheckinsInternal(accountSubject: string) {
  if (!isNative() || !navigator.onLine || !patientSession.getAccessToken()) return 0
  const state = await readState()
  if (state.accountSubject !== accountSubject || patientSession.getAccountSubject() !== accountSubject) return 0
  let synced = 0
  for (const pending of [...state.pendingCheckins]) {
    try {
      await medicationService.submitCheckin(pending)
      if (patientSession.getAccountSubject() !== accountSubject) return synced
      state.pendingCheckins = state.pendingCheckins.filter(
        (item) => item.clientRequestId !== pending.clientRequestId
      )
      synced += 1
      await writeState(state)
    } catch {
      break
    }
  }
  return synced
}

export function replayPendingCheckins() {
  const accountSubject = patientSession.getAccountSubject()
  if (!accountSubject) return Promise.resolve(0)
  if (replayAttempt?.accountSubject === accountSubject) return replayAttempt.promise

  const promise = replayPendingCheckinsInternal(accountSubject).finally(() => {
    if (replayAttempt?.promise === promise) replayAttempt = null
  })
  replayAttempt = { accountSubject, promise }
  return promise
}

export async function clearMedicalCache() {
  if (isNative()) await SecureMedicalCache.clear()
}
