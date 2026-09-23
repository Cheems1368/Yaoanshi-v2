import { beforeEach, describe, expect, test, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  refreshPost: vi.fn(),
  instancePost: vi.fn(),
  responseError: null as null | ((error: unknown) => Promise<unknown>)
}))

vi.mock('axios', () => ({
  default: {
    post: mocks.refreshPost,
    create: () => ({
      defaults: { baseURL: '' },
      interceptors: {
        request: { use: vi.fn() },
        response: { use: (_success: unknown, error: (error: unknown) => Promise<unknown>) => { mocks.responseError = error } }
      },
      post: mocks.instancePost
    })
  }
}))

import { patientSession } from '@/app/session'
import { httpService } from '@/shared/http/client'

const token = (subject: string) => `header.${btoa(JSON.stringify({ sub: subject }))}.signature`

describe('patient session refresh race', () => {
  beforeEach(() => {
    patientSession.resetAuthStatus()
    patientSession.setAccessToken(token('patient-a'))
    mocks.refreshPost.mockReset()
    mocks.instancePost.mockReset()
  })

  test('waits for an old refresh before issuing a new login and never commits its token', async () => {
    let resolveRefresh!: (value: unknown) => void
    mocks.refreshPost.mockReturnValue(new Promise((resolve) => { resolveRefresh = resolve }))
    mocks.instancePost.mockResolvedValue({
      code: 200, data: { accessToken: token('patient-b'), deviceId: 'device-b' }
    })
    const oldGeneration = patientSession.getSessionGeneration()

    const oldRetry = mocks.responseError!({
      response: { status: 401 },
      config: { url: '/app-api/dose-tasks', _accountSubject: 'patient-a', _sessionGeneration: oldGeneration }
    })
    await vi.waitFor(() => expect(mocks.refreshPost).toHaveBeenCalledTimes(1))

    const login = httpService.loginSession({ mobile: '13800000000' })
    expect(mocks.instancePost).not.toHaveBeenCalled()
    resolveRefresh({ data: { code: 200, data: { accessToken: token('patient-a') } } })

    await expect(oldRetry).rejects.toThrow('AUTHENTICATED_ACCOUNT_CHANGED')
    await expect(login).resolves.toMatchObject({ data: { accessToken: token('patient-b') } })
    expect(mocks.instancePost).toHaveBeenCalledWith('/app-api/auth/session/login', { mobile: '13800000000' }, undefined)
    expect(patientSession.getAccountSubject()).toBe('patient-b')
    expect(patientSession.getAccessToken()).toBe(token('patient-b'))

    await expect(mocks.responseError!({
      response: { status: 401 },
      config: { url: '/app-api/dose-tasks', _retry: true, _accountSubject: 'patient-a', _sessionGeneration: oldGeneration }
    })).rejects.toThrow('AUTHENTICATED_ACCOUNT_CHANGED')
    expect(patientSession.getAccessToken()).toBe(token('patient-b'))
  })
})
