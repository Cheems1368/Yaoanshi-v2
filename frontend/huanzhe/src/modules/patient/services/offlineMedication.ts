import { medicationService, type DoseTaskDTO } from '@/modules/patient/api/mvp'
import { patientSession } from '@/app/session'
import { isNativeMedicalCache, secureMedicalCache } from '@/app/secureMedicalCache'
import { cancelAllMedicationReminders, cancelMedicationReminder } from '@/modules/patient/services/localMedicationReminders'

const CACHE_TTL_MS = 48 * 60 * 60 * 1000
export const OFFLINE_CHECKIN_CONFLICT_EVENT = 'patient-offline-checkin-conflict'
let offlineCheckinConflictCount = 0

export function consumeOfflineCheckinConflictCount() {
  const count = offlineCheckinConflictCount
  offlineCheckinConflictCount = 0
  return count
}

function notifyOfflineCheckinConflict() {
  offlineCheckinConflictCount += 1
  if (typeof window !== 'undefined') window.dispatchEvent(new Event(OFFLINE_CHECKIN_CONFLICT_EVENT))
}

interface PendingCheckin {
  clientRequestId: string
  doseTaskId: number
  existingCheckinId?: number
  result: 'TAKEN' | 'MISSED'
  actualTakenAt: string
  note?: string
  skipReason?: string
}

interface CachedPendingCheckin extends PendingCheckin {
  expiresAt: number
  correction?: Omit<PendingCheckin, 'doseTaskId'>
}

interface MedicalCacheState {
  version: 2
  accountSubject: string
  accountType?: string
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

const isNative = isNativeMedicalCache

const canUseOfflineCache = (message: string) => message === 'Network error'
  || (patientSession.getAuthStatus() === 'offline'
    && (message === 'Server error' || message === 'Session temporarily unavailable'))

const emptyState = (): MedicalCacheState => ({
  version: 2,
  accountSubject: patientSession.getAccountSubject() ?? '',
  accountType: patientSession.getAccountType() ?? undefined,
  tasksExpiresAt: 0,
  tasks: [],
  pendingCheckins: []
})

async function readState(): Promise<MedicalCacheState> {
  if (!isNative()) return emptyState()
  try {
    const { value } = await secureMedicalCache.read()
    if (!value) return emptyState()
    const parsed = JSON.parse(value) as MedicalCacheState | LegacyMedicalCacheState
    const accountSubject = patientSession.getAccountSubject()
    if (parsed.version !== 1 && parsed.version !== 2) {
      await secureMedicalCache.clear()
      return emptyState()
    }
    if (!accountSubject) return emptyState()
    if (parsed.accountSubject !== accountSubject) {
      await secureMedicalCache.clear()
      return emptyState()
    }
    const normalized: MedicalCacheState = parsed.version === 1
      ? {
          version: 2,
          accountSubject: parsed.accountSubject,
          accountType: undefined,
          tasksExpiresAt: parsed.expiresAt,
          tasks: parsed.tasks,
          pendingCheckins: parsed.pendingCheckins.map((pending) => ({ ...pending, expiresAt: parsed.expiresAt }))
        }
      : parsed
    const now = Date.now()
    const pruned: MedicalCacheState = {
      ...normalized,
      tasks: normalized.tasksExpiresAt > now ? normalized.tasks : [],
      pendingCheckins: normalized.pendingCheckins.filter((pending) => pending.expiresAt > now)
    }
    const expiredDataRemoved = pruned.tasks.length !== normalized.tasks.length
      || pruned.pendingCheckins.length !== normalized.pendingCheckins.length
    if (expiredDataRemoved && patientSession.getAccountSubject() === accountSubject) {
      try {
        if (pruned.tasks.length === 0 && pruned.pendingCheckins.length === 0) {
          await secureMedicalCache.clear()
        } else {
          await secureMedicalCache.write({ value: JSON.stringify(pruned) })
        }
      } catch {
        // AtomicFile preserves the previous ciphertext; do not delete still-valid entries on cleanup I/O failure.
      }
    }
    return pruned
  } catch {
    await secureMedicalCache.clear().catch(() => undefined)
    return emptyState()
  }
}

async function writeState(state: MedicalCacheState) {
  if (!isNative()) return
  const accountSubject = patientSession.getAccountSubject()
  if (!accountSubject || !state.accountSubject || state.accountSubject !== accountSubject) {
    throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
  }
  if (state.tasks.length === 0 && state.pendingCheckins.length === 0) {
    await secureMedicalCache.clear()
    return
  }
  await secureMedicalCache.write({ value: JSON.stringify(state) })
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

function applyPendingCheckins(tasks: DoseTaskDTO[], pendingCheckins: CachedPendingCheckin[]) {
  const pendingByTaskId = new Map<number, CachedPendingCheckin>()
  pendingCheckins.forEach((pending) => pendingByTaskId.set(pending.doseTaskId, pending))
  return tasks.map((task) => {
    const pending = pendingByTaskId.get(task.id)
    if (!pending) return task
    const visibleResult = pending.correction?.result ?? pending.result
    return { ...task, status: visibleResult === 'TAKEN' ? 'COMPLETED' : 'MISSED' }
  })
}

const makeRequestId = () => globalThis.crypto?.randomUUID?.()
  ?? `checkin-${Date.now()}-${Math.random().toString(16).slice(2)}`

async function mergePendingCheckin(
  payload: PendingCheckin,
  expectedAccountSubject: string,
  enqueueIfMissing: boolean
) {
  const operation = stateMutationTail.then(async () => {
    const state = await readState()
    if (patientSession.getAccountSubject() !== expectedAccountSubject
      || state.accountSubject !== expectedAccountSubject) {
      throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
    }
    const existing = state.pendingCheckins.find((item) => item.doseTaskId === payload.doseTaskId)
    if (!existing) {
      if (enqueueIfMissing) {
        state.pendingCheckins.push({ ...payload, expiresAt: Date.now() + CACHE_TTL_MS })
        await writeState(state)
      }
      return enqueueIfMissing
    }
    existing.correction = {
      clientRequestId: payload.clientRequestId,
      result: payload.result,
      actualTakenAt: payload.actualTakenAt,
      note: payload.note,
      skipReason: payload.skipReason
    }
    await writeState(state)
    return true
  })
  stateMutationTail = operation.then(() => undefined, () => undefined)
  return operation
}

export async function cacheDoseTasks(tasks: DoseTaskDTO[], expectedAccountSubject: string) {
  if (!isNative()) return tasks
  return mutateState((state) => {
    if (patientSession.getAccountSubject() !== expectedAccountSubject || state.accountSubject !== expectedAccountSubject) {
      throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
    }
    state.tasks = tasks
    state.tasksExpiresAt = Date.now() + CACHE_TTL_MS
    return applyPendingCheckins(state.tasks, state.pendingCheckins)
  })
}

export async function cacheOfflineAccountType(accountType: string, expectedAccountSubject: string) {
  if (!isNative()) return
  const normalized = accountType.toUpperCase()
  if (normalized !== 'PATIENT' && normalized !== 'FAMILY') return
  await mutateState((state) => {
    if (patientSession.getAccountSubject() !== expectedAccountSubject || state.accountSubject !== expectedAccountSubject) {
      throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
    }
    state.accountType = normalized
  })
}

export async function getCachedDoseTasks() {
  const state = await readState()
  return applyPendingCheckins(state.tasks, state.pendingCheckins)
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
    const visibleTasks = isNative() ? await cacheDoseTasks(tasks, accountSubject as string) : tasks
    return { tasks: visibleTasks, offline: false }
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
    if (!canUseOfflineCache(message)) throw error
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
    existingCheckinId: task.lastEffectiveCheckinId ?? undefined,
    result,
    actualTakenAt: new Date().toISOString(),
    note: result === 'TAKEN' ? '患者端提交' : '',
    skipReason: result === 'MISSED' ? 'PATIENT_REPORTED' : ''
  }

  if (isNative() && expectedAccountSubject) {
    const merged = await mergePendingCheckin(payload, expectedAccountSubject, false)
    if (merged) {
      if (patientSession.getAccountSubject() !== expectedAccountSubject) {
        throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
      }
      if (navigator.onLine && patientSession.getAccessToken()) await replayPendingCheckins()
      if (patientSession.getAccountSubject() !== expectedAccountSubject) {
        throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
      }
      const latestState = await readState()
      return latestState.pendingCheckins.some((item) => item.doseTaskId === task.id) ? 'queued' : 'submitted'
    }
  }

  let submitted: Awaited<ReturnType<typeof medicationService.submitCheckin>>
  try {
    if (payload.existingCheckinId) {
      submitted = await medicationService.correctCheckin(payload.existingCheckinId, {
        clientRequestId: payload.clientRequestId,
        result: payload.result,
        actualTakenAt: payload.actualTakenAt,
        note: payload.note,
        skipReason: payload.skipReason
      })
    } else {
      submitted = await medicationService.submitCheckin(payload)
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : ''
    if (!isNative() || !canUseOfflineCache(message)) throw error
    if (!expectedAccountSubject || patientSession.getAccountSubject() !== expectedAccountSubject) {
      throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
    }
    await mergePendingCheckin(payload, expectedAccountSubject, true)
    return 'queued'
  }
  if (isNative() && expectedAccountSubject && submitted.data?.id) {
    if (patientSession.getAccountSubject() !== expectedAccountSubject) {
      throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
    }
    let authoritativeTask: DoseTaskDTO | null | undefined
    if (submitted.data.effective === false) {
      if (task.taskDate) {
        const response = await medicationService.getDoseTasks(task.taskDate, task.taskDate)
        authoritativeTask = response.data.find((item) => item.id === task.id) ?? null
      } else {
        authoritativeTask = null
      }
    }
    await mutateState((state) => {
      if (state.accountSubject !== expectedAccountSubject
        || patientSession.getAccountSubject() !== expectedAccountSubject) {
        throw new Error('AUTHENTICATED_ACCOUNT_CHANGED')
      }
      const taskIndex = state.tasks.findIndex((item) => item.id === task.id)
      if (taskIndex < 0) return
      if (authoritativeTask === null) state.tasks.splice(taskIndex, 1)
      else if (authoritativeTask) state.tasks[taskIndex] = authoritativeTask
      else {
        const cachedTask = state.tasks[taskIndex]
        cachedTask.status = (submitted.data.result || payload.result) === 'MISSED' ? 'MISSED' : 'COMPLETED'
        cachedTask.lastEffectiveCheckinId = submitted.data.id
        cachedTask.notificationStatus = 'ACKED'
        cachedTask.reminderStage = 'HANDLED'
      }
    })
    if (patientSession.getAccountSubject() === expectedAccountSubject) {
      await cancelMedicationReminder(task.id).catch(() => undefined)
    }
  }
  return 'submitted'
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
    let replaySnapshot: CachedPendingCheckin | undefined = pending
    try {
      while (replaySnapshot) {
        const submitted = replaySnapshot.existingCheckinId
          ? await medicationService.correctCheckin(replaySnapshot.existingCheckinId, {
              clientRequestId: replaySnapshot.clientRequestId,
              result: replaySnapshot.result,
              actualTakenAt: replaySnapshot.actualTakenAt,
              note: replaySnapshot.note,
              skipReason: replaySnapshot.skipReason
            })
          : await medicationService.submitCheckin({
              clientRequestId: replaySnapshot.clientRequestId,
              doseTaskId: replaySnapshot.doseTaskId,
              result: replaySnapshot.result,
              actualTakenAt: replaySnapshot.actualTakenAt,
              note: replaySnapshot.note,
              skipReason: replaySnapshot.skipReason
            })
        let effectiveRecord = submitted.data
        if (replaySnapshot.correction) {
          let correctionTargetId: number | null | undefined = submitted.data?.id ?? replaySnapshot.existingCheckinId
          if (submitted.data?.effective === false) {
            const cachedTask = (await readState()).tasks.find((task) => task.id === replaySnapshot?.doseTaskId)
            if (!cachedTask?.taskDate) break
            const response = await medicationService.getDoseTasks(cachedTask.taskDate, cachedTask.taskDate)
            correctionTargetId = response.data.find((task) => task.id === replaySnapshot?.doseTaskId)?.lastEffectiveCheckinId
          }
          if (!correctionTargetId) break
          const corrected = await medicationService.correctCheckin(correctionTargetId, replaySnapshot.correction)
          effectiveRecord = corrected.data
        }
        if (!effectiveRecord?.id) break
        let authoritativeTask: DoseTaskDTO | null | undefined
        if (effectiveRecord.effective === false) {
          const cachedTask = (await readState()).tasks.find((task) => task.id === replaySnapshot?.doseTaskId)
          if (cachedTask?.taskDate) {
            const response = await medicationService.getDoseTasks(cachedTask.taskDate, cachedTask.taskDate)
            if (!Array.isArray(response.data)) break
            authoritativeTask = response.data.find((task) => task.id === replaySnapshot?.doseTaskId) ?? null
          } else {
            authoritativeTask = null
          }
        }
        if (patientSession.getAccountSubject() !== accountSubject) return synced
        const outcome = await mutateState((currentState) => {
          const currentIndex = currentState.pendingCheckins.findIndex(
            (item) => item.clientRequestId === replaySnapshot?.clientRequestId
          )
          if (currentIndex < 0) return { removed: true, cancelReminder: false }
          const current = currentState.pendingCheckins[currentIndex]
          if (current.correction?.clientRequestId !== replaySnapshot?.correction?.clientRequestId) {
            return { removed: false, cancelReminder: false }
          }
          currentState.pendingCheckins.splice(currentIndex, 1)
          const taskIndex = currentState.tasks.findIndex((task) => task.id === current.doseTaskId)
          if (authoritativeTask === null) {
            if (taskIndex >= 0) currentState.tasks.splice(taskIndex, 1)
            return { removed: true, cancelReminder: true }
          }
          if (taskIndex >= 0 && authoritativeTask) {
            currentState.tasks[taskIndex] = authoritativeTask
          } else if (taskIndex >= 0) {
            const cachedTask = currentState.tasks[taskIndex]
            const result = current.correction?.result ?? current.result
            cachedTask.status = result === 'TAKEN' ? 'COMPLETED' : 'MISSED'
            cachedTask.lastEffectiveCheckinId = effectiveRecord.id
            cachedTask.notificationStatus = 'ACKED'
            cachedTask.reminderStage = 'HANDLED'
          }
          return { removed: true, cancelReminder: false }
        })
        if (outcome.cancelReminder && patientSession.getAccountSubject() === accountSubject) {
          await cancelMedicationReminder(replaySnapshot.doseTaskId).catch(() => undefined)
        }
        if (outcome.removed) {
          synced += 1
          break
        }
        const latestState = await readState()
        replaySnapshot = latestState.pendingCheckins.find(
          (item) => item.clientRequestId === pending.clientRequestId
        )
      }
    } catch (error) {
      if (patientSession.getAccountSubject() !== accountSubject) return synced
      const errorCode = (error as { code?: number })?.code
      const message = error instanceof Error ? error.message : ''
      if (errorCode === 40009 || message === 'DOSE_TASK_EDIT_WINDOW_EXPIRED') {
        const cachedTask = (await readState()).tasks.find((task) => task.id === pending.doseTaskId)
        let authoritativeTask: DoseTaskDTO | null = null
        if (cachedTask?.taskDate) {
          try {
            const response = await medicationService.getDoseTasks(cachedTask.taskDate, cachedTask.taskDate)
            if (!Array.isArray(response.data)) break
            authoritativeTask = response.data.find((task) => task.id === pending.doseTaskId) ?? null
          } catch {
            break
          }
        }
        if (patientSession.getAccountSubject() !== accountSubject) return synced
        const removed = await mutateState((currentState) => {
          if (currentState.accountSubject !== accountSubject) return false
          const currentIndex = currentState.pendingCheckins.findIndex(
            (item) => item.clientRequestId === pending.clientRequestId
          )
          if (currentIndex < 0
            || currentState.pendingCheckins[currentIndex].correction?.clientRequestId !== replaySnapshot?.correction?.clientRequestId) {
            return false
          }
          currentState.pendingCheckins.splice(currentIndex, 1)
          const taskIndex = currentState.tasks.findIndex((task) => task.id === pending.doseTaskId)
          if (taskIndex >= 0 && authoritativeTask) currentState.tasks[taskIndex] = authoritativeTask
          else if (taskIndex >= 0) currentState.tasks.splice(taskIndex, 1)
          return true
        })
        if (removed && patientSession.getAccountSubject() === accountSubject) notifyOfflineCheckinConflict()
        continue
      }
      if (errorCode === 40025 || message === 'DOSE_TASK_NOT_FOUND') {
        await mutateState((currentState) => {
          if (currentState.accountSubject !== accountSubject) return
          currentState.pendingCheckins = currentState.pendingCheckins.filter(
            (item) => item.doseTaskId !== pending.doseTaskId
          )
          currentState.tasks = currentState.tasks.filter((task) => task.id !== pending.doseTaskId)
        })
        if (patientSession.getAccountSubject() === accountSubject) {
          await cancelMedicationReminder(pending.doseTaskId).catch(() => undefined)
        }
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
  const operation = stateMutationTail.then(() => secureMedicalCache.clear())
  stateMutationTail = operation.then(() => undefined, () => undefined)
  await operation
}
