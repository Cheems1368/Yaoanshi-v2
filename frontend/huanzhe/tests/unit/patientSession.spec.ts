import { beforeEach, describe, expect, test, vi } from 'vitest'

const secureCacheMocks = vi.hoisted(() => ({ read: vi.fn() }))

vi.mock('@/app/secureMedicalCache', () => ({
  isNativeMedicalCache: () => true,
  secureMedicalCache: { read: secureCacheMocks.read }
}))

import { patientSession } from '@/app/session'

describe('patientSession', () => {
  beforeEach(() => {
    localStorage.clear()
    sessionStorage.clear()
    patientSession.resetAuthStatus()
    secureCacheMocks.read.mockReset()
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
      patientSession.setAccountType('FAMILY')
      expect(patientSession.getAccountSubject()).toBe('patient-1')
      patientSession.setAccessToken(token('patient-2'))
      expect(patientSession.getAccountType()).toBeNull()
      patientSession.markAnonymous()
      expect(clears).toBe(1)
      expect(patientSession.getAccountSubject()).toBeNull()
    } finally {
      window.removeEventListener('patient-account-switched', listener)
    }
  })

  test('signals reminder cleanup whenever the session becomes anonymous', () => {
    let anonymousEvents = 0
    const listener = () => { anonymousEvents += 1 }
    window.addEventListener('patient-session-anonymous', listener)
    try {
      patientSession.markAnonymous()
      expect(anonymousEvents).toBe(1)
    } finally {
      window.removeEventListener('patient-session-anonymous', listener)
    }
  })

  test('restores a bound offline session only from unexpired encrypted native state', async () => {
    secureCacheMocks.read.mockResolvedValue({
      value: JSON.stringify({
        version: 2,
        accountSubject: 'patient-1',
        accountType: 'FAMILY',
        tasksExpiresAt: Date.now() + 60_000,
        tasks: [{ id: 7 }],
        pendingCheckins: []
      })
    })

    await expect(patientSession.restoreOfflineContext()).resolves.toBe(true)

    expect(patientSession.getAccountSubject()).toBe('patient-1')
    expect(patientSession.getAccountType()).toBe('FAMILY')
    expect(patientSession.getAccessToken()).toBeNull()
    expect(patientSession.getGateState()).toEqual({ loggedIn: true, isBound: true, hasActivePlan: false })
  })

  test('rejects expired encrypted native state for offline session restoration', async () => {
    secureCacheMocks.read.mockResolvedValue({
      value: JSON.stringify({
        version: 2,
        accountSubject: 'patient-1',
        tasksExpiresAt: Date.now() - 1,
        tasks: [{ id: 7 }],
        pendingCheckins: []
      })
    })

    await expect(patientSession.restoreOfflineContext()).resolves.toBe(false)
    expect(patientSession.getGateState().loggedIn).toBe(false)
  })
})
