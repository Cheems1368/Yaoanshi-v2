import { isNativeMedicalCache, secureMedicalCache } from '@/app/secureMedicalCache'

const DEVICE_ID_KEY = 'patient_device_id'
const MOBILE_KEY = 'patient_mobile'
const BINDING_STATUS_KEY = 'patient_binding_status'
const ACTIVE_PLAN_KEY = 'patient_has_active_plan'
let accessTokenMemory = ''
let accountSubjectMemory = ''
let accountTypeMemory = ''
let authStatus: 'unknown' | 'authenticated' | 'offline' | 'anonymous' = 'unknown'
let sessionGeneration = 0

const getSessionStorage = () => (typeof window !== 'undefined' ? window.sessionStorage : null)

const createDeviceId = () => {
  if (typeof globalThis !== 'undefined' && globalThis.crypto?.randomUUID) {
    return globalThis.crypto.randomUUID()
  }

  return `patient-web-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

const readTokenIdentity = (token: string) => {
  try {
    const payload = token.split('.')[1]
    if (!payload) return { subject: '', accountType: '' }
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const decoded = JSON.parse(atob(normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')))
    const roles = Array.isArray(decoded.roles)
      ? decoded.roles.map((role: unknown) => String(role).toUpperCase())
      : []
    const declaredAccountType = typeof decoded.accountType === 'string' ? decoded.accountType.toUpperCase() : ''
    const accountType = roles.includes('ROLE_FAMILY')
      ? 'FAMILY'
      : roles.includes('ROLE_PATIENT') ? 'PATIENT' : declaredAccountType
    return {
      subject: typeof decoded.sub === 'string' ? decoded.sub : '',
      accountType: accountType === 'PATIENT' || accountType === 'FAMILY' ? accountType : ''
    }
  } catch {
    return { subject: '', accountType: '' }
  }
}

export interface PatientGateState {
  loggedIn: boolean
  isBound: boolean
  hasActivePlan: boolean
}

export const patientSession = {
  getSessionGeneration() {
    return sessionGeneration
  },

  advanceSessionGeneration() {
    sessionGeneration += 1
  },

  getAccessToken(): string | null {
    return accessTokenMemory || null
  },

  setAccessToken(token: string, isRefresh = false) {
    if (!isRefresh) sessionGeneration += 1
    const identity = readTokenIdentity(token)
    const nextSubject = identity.subject
    if (accountSubjectMemory && nextSubject && accountSubjectMemory !== nextSubject) {
      accountTypeMemory = ''
      if (typeof window !== 'undefined') window.dispatchEvent(new Event('patient-account-switched'))
    }
    accessTokenMemory = token || ''
    accountSubjectMemory = nextSubject
    if (identity.accountType) accountTypeMemory = identity.accountType
    authStatus = accessTokenMemory ? 'authenticated' : authStatus
    if (accessTokenMemory && typeof window !== 'undefined') window.dispatchEvent(new Event('patient-session-authenticated'))
  },

  setAccountType(accountType: string) {
    const normalized = accountType.toUpperCase()
    accountTypeMemory = normalized === 'PATIENT' || normalized === 'FAMILY' ? normalized : ''
  },

  getAccountType() {
    return accountTypeMemory || null
  },

  getAccountSubject() {
    return accountSubjectMemory || null
  },

  clearAccessToken() {
    sessionGeneration += 1
    accessTokenMemory = ''
    accountSubjectMemory = ''
    accountTypeMemory = ''
  },

  getAuthStatus() {
    return authStatus
  },

  markAnonymous() {
    sessionGeneration += 1
    authStatus = 'anonymous'
    accessTokenMemory = ''
    accountSubjectMemory = ''
    accountTypeMemory = ''
    if (typeof window !== 'undefined') window.dispatchEvent(new Event('patient-session-anonymous'))
  },

  markSessionExpired() {
    sessionGeneration += 1
    authStatus = 'anonymous'
    accessTokenMemory = ''
    accountSubjectMemory = ''
    accountTypeMemory = ''
    if (typeof window !== 'undefined') window.dispatchEvent(new Event('patient-session-expired'))
  },

  resetAuthStatus() {
    sessionGeneration += 1
    authStatus = 'unknown'
    accessTokenMemory = ''
    accountSubjectMemory = ''
    accountTypeMemory = ''
  },

  async restoreOfflineContext() {
    if (!isNativeMedicalCache() || authStatus === 'anonymous') return false
    try {
      const { value } = await secureMedicalCache.read()
      if (!value) return false
      const parsed = JSON.parse(value) as {
        version?: number
        accountSubject?: string
        accountType?: string
        expiresAt?: number
        tasksExpiresAt?: number
        tasks?: unknown[]
        pendingCheckins?: Array<{ expiresAt?: number }>
        sealedUntilLogin?: boolean
      }
      if ((parsed.version !== 1 && parsed.version !== 2) || !parsed.accountSubject) {
        await secureMedicalCache.clear().catch(() => undefined)
        return false
      }
      if (parsed.sealedUntilLogin) {
        const pendingValid = Array.isArray(parsed.pendingCheckins) && parsed.pendingCheckins.some(
          (pending) => typeof pending.expiresAt === 'number' && pending.expiresAt > Date.now()
        )
        if (!pendingValid) await secureMedicalCache.clear().catch(() => undefined)
        return false
      }
      const now = Date.now()
      const tasksExpiry = parsed.version === 1 ? parsed.expiresAt : parsed.tasksExpiresAt
      const hasValidTasks = Array.isArray(parsed.tasks) && parsed.tasks.length > 0
        && typeof tasksExpiry === 'number' && tasksExpiry > now
      const hasValidPending = Array.isArray(parsed.pendingCheckins) && parsed.pendingCheckins.some((pending) => {
        const expiry = parsed.version === 1 ? parsed.expiresAt : pending.expiresAt
        return typeof expiry === 'number' && expiry > now
      })
      if (!hasValidTasks && !hasValidPending) {
        await secureMedicalCache.clear().catch(() => undefined)
        return false
      }
      accountSubjectMemory = parsed.accountSubject
      sessionGeneration += 1
      this.setAccountType(parsed.accountType ?? '')
      authStatus = 'offline'
      return true
    } catch {
      await secureMedicalCache.clear().catch(() => undefined)
      return false
    }
  },

  getDeviceId(): string | null {
    return localStorage.getItem(DEVICE_ID_KEY)
  },

  setDeviceId(deviceId: string) {
    localStorage.setItem(DEVICE_ID_KEY, deviceId)
  },

  ensureDeviceId(): string {
    const existingDeviceId = this.getDeviceId()
    if (existingDeviceId) {
      return existingDeviceId
    }

    const deviceId = createDeviceId()
    this.setDeviceId(deviceId)
    return deviceId
  },

  getMobile(): string | null {
    return getSessionStorage()?.getItem(MOBILE_KEY) ?? null
  },

  setMobile(mobile: string) {
    const storage = getSessionStorage()
    if (!storage) {
      return
    }
    if (!mobile) {
      storage.removeItem(MOBILE_KEY)
      return
    }
    storage.setItem(MOBILE_KEY, mobile)
  },

  clearMobile() {
    getSessionStorage()?.removeItem(MOBILE_KEY)
  },

  clearAuthState() {
    this.markAnonymous()
    this.clearMobile()
  },

  setBindingStatus(isBound: boolean) {
    localStorage.setItem(BINDING_STATUS_KEY, isBound ? 'BOUND' : 'PENDING')
  },

  setActivePlanStatus(hasActivePlan: boolean) {
    localStorage.setItem(ACTIVE_PLAN_KEY, hasActivePlan ? 'true' : 'false')
  },

  clearGateState() {
    localStorage.removeItem(BINDING_STATUS_KEY)
    localStorage.removeItem(ACTIVE_PLAN_KEY)
  },

  getGateState(): PatientGateState {
    return {
      loggedIn: (authStatus === 'authenticated' && Boolean(accessTokenMemory))
        || (authStatus === 'offline' && Boolean(accountSubjectMemory)),
      isBound: authStatus === 'offline' || localStorage.getItem(BINDING_STATUS_KEY) === 'BOUND',
      hasActivePlan: localStorage.getItem(ACTIVE_PLAN_KEY) === 'true'
    }
  }
}
