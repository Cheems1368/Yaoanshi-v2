import { Capacitor, registerPlugin } from '@capacitor/core'
import { medicationService, type DoseTaskDTO } from '@/modules/patient/api/mvp'
import { patientSession } from '@/app/session'
import { cancelAllMedicationReminders } from '@/modules/patient/services/localMedicationReminders'

const CACHE_TTL_MS = 48 * 60 * 60 * 1000

interface PendingCheckin {
  clientRequestId: string
  doseTaskId: number
  result: 'TAKEN' | 'MISSED'
  actualTakenAt: string
  note?: string
  skipReason?: string
}

interface CachedPendingCheckin extends PendingCheckin {
  expiresAt: number
}

interface MedicalCacheState {
  version: 2
  accountSubject: string
  tasksExpiresAt: number
  tasks: DoseTaskDTO[]
  pendingCheckins: CachedPendingCheckin[]
}

interface LegacyMedicalCacheState {
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
  version: 2,
  accountSubject: patientSession.getAccountSubject() ?? '',
  tasksExpiresAt: 0,
  tasks: [],
  pendingCheckins: []
})

async function readState(): Promise<MedicalCacheState> {
  if (!isNative()) return emptyState()
  try {
    const { value } = await SecureMedicalCache.read()
    if (!value) return emptyState()
    const parsed = JSON.parse(value) as MedicalCacheState | LegacyMedicalCacheState
    const accountSubject = patientSession.getAccountSubject()
    if (parsed.version !== 1 && parsed.version !== 2) {
      await SecureMedicalCache.clear()
      return emptyState()
    }
    if (!accountSubject) return emptyState()
    if (parsed.accountSubject !== accountSubject) {
      await SecureMedicalCache.clear()
      return emptyState()
    }
    const normalized: MedicalCacheState = parsed.version === 1
      ? {
          version: 2,
          accountSubject: parsed.accountSubject,
          tasksExpiresAt: parsed.expiresAt,
          tasks: parsed.tasks,
          pendingCheckins: parsed.pendingCheckins.map((pending) => ({ ...pending, expiresAt: parsed.expiresAt }))
        }
      : parsed
    const now = Date.now()
    return {
      ...normalized,
      tasks: normalized.tasksExpiresAt > now ? normalized.tasks : [],
      pendingCheckins: normalized.pendingCheckins.filter((pending) => pending.expiresAt > now)
    }
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
  await SecureMedicalCache.write({ value: JSON.stringify(state) })
}

let stateMutationTail: Promise<void> = Promise.resolve()

function mutateState<T>(mutation: (state: MedicalCacheState) => T | Promise<T>): Promise<T> {
  const operation = stateMutationTail.then(async () => {
    const state = await readState()
    const result = await mutation(state)
    await writeState(state)
    return result
  })
  stateMutationTail = operation.then(() => undefined, () => undefined)
  return operation
}

const makeRequestId = () => globalThis.crypto?.randomUUID?.()
  ?? `checkin-${Date.now()}-${Math.random().toString(16).slice(2)}`

export async function cacheDoseTasks(tasks: DoseTaskDTO[], expectedAccountSubject: string) {
  if (!isNative()) return
  await mutateState((state) => {
    if (patientSession.getAccountSubject() !== expectedAccountSubject || state.accountSubject !== expectedAccountSubject) {
      throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
    }
    state.tasks = tasks
    state.tasksExpiresAt = Date.now() + CACHE_TTL_MS
  })
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
  const accountSubject = patientSession.getAccountSubject()
  const dateFrom = new Date()
  const dateTo = new Date()
  dateTo.setDate(dateTo.getDate() + Math.min(Math.max(days, 1), 7) - 1)
  try {
    await replayPendingCheckins()
    if (isNative() && (!accountSubject || patientSession.getAccountSubject() !== accountSubject)) {
      throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
    }
    const response = await medicationService.getDoseTasks(formatLocalDate(dateFrom), formatLocalDate(dateTo))
    if (isNative() && patientSession.getAccountSubject() !== accountSubject) {
      throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
    }
    const tasks = response.data ?? []
    if (isNative()) await cacheDoseTasks(tasks, accountSubject as string)
    return { tasks, offline: false }
  } catch (error) {
    if (!isNative()) throw error
    const message = error instanceof Error ? error.message : ''
    if (message === 'AUTHENTICATED_ACCOUNT_CHANGED') return { tasks: [], offline: true }
    if (message === 'PATIENT_BIND_CONFLICT' || message === 'PATIENT_NOT_FOUND') {
      patientSession.clearGateState()
      await clearMedicalCache()
      await cancelAllMedicationReminders()
      throw error
    }
    if (message !== 'Network error') throw error
    return { tasks: await getCachedDoseTasks(), offline: true }
  }
}

export async function submitOrQueueCheckin(
  task: DoseTaskDTO,
  result: 'TAKEN' | 'MISSED'
): Promise<'submitted' | 'queued'> {
  const expectedAccountSubject = patientSession.getAccountSubject()
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
    if (!expectedAccountSubject || patientSession.getAccountSubject() !== expectedAccountSubject) {
      throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
    }
    await mutateState((state) => {
      if (patientSession.getAccountSubject() !== expectedAccountSubject
        || state.accountSubject !== expectedAccountSubject) {
        throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
      }
      if (!state.pendingCheckins.some((item) => item.clientRequestId === payload.clientRequestId)) {
        state.pendingCheckins.push({ ...payload, expiresAt: Date.now() + CACHE_TTL_MS })
      }
      state.tasks = state.tasks.map((item) => item.id === task.id
        ? { ...item, status: result === 'TAKEN' ? 'COMPLETED' : 'MISSED' }
        : item)
    })
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
      await medicationService.submitCheckin({
        clientRequestId: pending.clientRequestId,
        doseTaskId: pending.doseTaskId,
        result: pending.result,
        actualTakenAt: pending.actualTakenAt,
        note: pending.note,
        skipReason: pending.skipReason
      })
      if (patientSession.getAccountSubject() !== accountSubject) return synced
      await mutateState((currentState) => {
        currentState.pendingCheckins = currentState.pendingCheckins.filter(
          (item) => item.clientRequestId !== pending.clientRequestId
        )
      })
      synced += 1
    } catch (error) {
      const errorCode = (error as { code?: number })?.code
      const message = error instanceof Error ? error.message : ''
      if (errorCode === 40025 || message === 'DOSE_TASK_NOT_FOUND') {
        await mutateState((currentState) => {
          currentState.pendingCheckins = currentState.pendingCheckins.filter(
            (item) => item.clientRequestId !== pending.clientRequestId
          )
        })
        continue
      }
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
  if (!isNative()) return
  const operation = stateMutationTail.then(() => SecureMedicalCache.clear())
  stateMutationTail = operation.then(() => undefined, () => undefined)
  await operation
}
