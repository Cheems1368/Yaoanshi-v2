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
})
