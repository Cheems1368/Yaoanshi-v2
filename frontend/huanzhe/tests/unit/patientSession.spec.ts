import { beforeEach, describe, expect, test } from 'vitest'
import { patientSession } from '@/app/session'

describe('patientSession', () => {
  beforeEach(() => {
    localStorage.clear()
    sessionStorage.clear()
    patientSession.resetAuthStatus()
  })

  test('clears all authentication state', () => {
    patientSession.setAccessToken('access-token')
    patientSession.setMobile('13800000000')

    patientSession.clearAuthState()

    expect(patientSession.getAccessToken()).toBeNull()
    expect(patientSession.getAuthStatus()).toBe('anonymous')
    expect(patientSession.getMobile()).toBeNull()
  })

  test('reports the patient route gate state', () => {
    patientSession.setAccessToken('access-token')
    patientSession.setBindingStatus(true)
    patientSession.setActivePlanStatus(true)

    expect(patientSession.getGateState()).toEqual({
      loggedIn: true,
      isBound: true,
      hasActivePlan: true
    })
  })

  test('creates one stable device identifier', () => {
    const firstDeviceId = patientSession.ensureDeviceId()

    expect(firstDeviceId).toBeTruthy()
    expect(patientSession.ensureDeviceId()).toBe(firstDeviceId)
  })

  test('signals secure-cache clearing only when the authenticated subject changes', () => {
    let clears = 0
    const listener = () => { clears += 1 }
    const token = (subject: string) => `header.${btoa(JSON.stringify({ sub: subject }))}.signature`
    window.addEventListener('patient-account-switched', listener)
    try {
      patientSession.setAccessToken(token('patient-1'))
      expect(patientSession.getAccountSubject()).toBe('patient-1')
      patientSession.setAccessToken(token('patient-2'))
      patientSession.markAnonymous()
      expect(clears).toBe(1)
      expect(patientSession.getAccountSubject()).toBeNull()
    } finally {
      window.removeEventListener('patient-account-switched', listener)
    }
  })
})
