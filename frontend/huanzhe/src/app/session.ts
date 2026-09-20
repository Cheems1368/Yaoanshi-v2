import { isNativeMedicalCache, secureMedicalCache } from '@/app/secureMedicalCache'

const DEVICE_ID_KEY = 'patient_device_id'
const MOBILE_KEY = 'patient_mobile'
const BINDING_STATUS_KEY = 'patient_binding_status'
const ACTIVE_PLAN_KEY = 'patient_has_active_plan'
let accessTokenMemory = ''
let accountSubjectMemory = ''
let accountTypeMemory = ''
let authStatus: 'unknown' | 'authenticated' | 'offline' | 'anonymous' = 'unknown'

const getSessionStorage = () => (typeof window !== 'undefined' ? window.sessionStorage : null)

const createDeviceId = () => {
  if (typeof globalThis !== 'undefined' && globalThis.crypto?.randomUUID) {
    return globalThis.crypto.randomUUID()
  }

  return `patient-web-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

const readTokenSubject = (token: string) => {
  try {
    const payload = token.split('.')[1]
    if (!payload) return ''
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const decoded = JSON.parse(atob(normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')))
    return typeof decoded.sub === 'string' ? decoded.sub : ''
  } catch {
    return ''
  }
}

export interface PatientGateState {
  loggedIn: boolean
  isBound: boolean
  hasActivePlan: boolean
}

export const patientSession = {
  getAccessToken(): string | null {
    return accessTokenMemory || null
  },

  setAccessToken(token: string) {
    const nextSubject = readTokenSubject(token)
    if (accountSubjectMemory && nextSubject && accountSubjectMemory !== nextSubject) {
      accountTypeMemory = ''
      if (typeof window !== 'undefined') window.dispatchEvent(new Event('patient-account-switched'))
    }
    accessTokenMemory = token || ''
    accountSubjectMemory = nextSubject
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
    accessTokenMemory = ''
    accountSubjectMemory = ''
    accountTypeMemory = ''
  },

  getAuthStatus() {
    return authStatus
  },

  markAnonymous() {
    authStatus = 'anonymous'
    accessTokenMemory = ''
    accountSubjectMemory = ''
    accountTypeMemory = ''
    if (typeof window !== 'undefined') window.dispatchEvent(new Event('patient-session-anonymous'))
  },

  resetAuthStatus() {
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
      }
      if ((parsed.version !== 1 && parsed.version !== 2) || !parsed.accountSubject) {
        await secureMedicalCache.clear().catch(() => undefined)
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
