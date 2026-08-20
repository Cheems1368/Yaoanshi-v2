const DEVICE_ID_KEY = 'patient_device_id'
const MOBILE_KEY = 'patient_mobile'
const BINDING_STATUS_KEY = 'patient_binding_status'
const ACTIVE_PLAN_KEY = 'patient_has_active_plan'
let accessTokenMemory = ''
let authStatus: 'unknown' | 'authenticated' | 'anonymous' = 'unknown'

const getSessionStorage = () => (typeof window !== 'undefined' ? window.sessionStorage : null)

const createDeviceId = () => {
  if (typeof globalThis !== 'undefined' && globalThis.crypto?.randomUUID) {
    return globalThis.crypto.randomUUID()
  }

  return `patient-web-${Date.now()}-${Math.random().toString(16).slice(2)}`
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
    accessTokenMemory = token || ''
    authStatus = accessTokenMemory ? 'authenticated' : authStatus
  },

  clearAccessToken() {
    accessTokenMemory = ''
  },

  getAuthStatus() {
    return authStatus
  },

  markAnonymous() {
    authStatus = 'anonymous'
    accessTokenMemory = ''
  },

  resetAuthStatus() {
    authStatus = 'unknown'
    accessTokenMemory = ''
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
      loggedIn: authStatus === 'authenticated' && Boolean(accessTokenMemory),
      isBound: localStorage.getItem(BINDING_STATUS_KEY) === 'BOUND',
      hasActivePlan: localStorage.getItem(ACTIVE_PLAN_KEY) === 'true'
    }
  }
}
