import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, describe, expect, test, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  subject: 'account-a',
  enter: null as null | (() => Promise<void>),
  getProfile: vi.fn(),
  getHealthProfile: vi.fn(),
  getCurrentAccount: vi.fn(),
  clearUserData: vi.fn(),
  clearHealthData: vi.fn(),
  replaceHealthData: vi.fn(),
  updateProfile: vi.fn(),
  replaceRoute: vi.fn()
}))

vi.mock('@ionic/vue', async (importOriginal) => ({
  ...await importOriginal<typeof import('@ionic/vue')>(),
  onIonViewWillEnter: (handler: () => Promise<void>) => { mocks.enter = handler }
}))
vi.mock('vue-router', () => ({ useRouter: () => ({ replace: mocks.replaceRoute, push: vi.fn() }) }))
vi.mock('@/app/session', () => ({
  patientSession: {
    getAccountSubject: () => mocks.subject,
    getGateState: () => ({ loggedIn: Boolean(mocks.subject) })
  }
}))
vi.mock('@/modules/patient/api/mvp', () => ({
  patientService: {
    getProfile: mocks.getProfile,
    getHealthProfile: mocks.getHealthProfile,
    getFamilyBindings: vi.fn(),
    updateProfile: vi.fn()
  },
  appAuthService: { getCurrentAccount: mocks.getCurrentAccount }
}))
vi.mock('@/services/userDataService', () => ({
  userDataService: {
    clearUserData: mocks.clearUserData,
    getUserProfile: () => ({
      userId: 'P2024001', name: '患者', phone: '', age: 0, gender: '', idCard: '', idType: '',
      ethnicity: '', height: 0, weight: 0, bloodType: '', allergies: '', avatar: '',
      emergencyContact: { name: '', phone: '', relationship: '' }, isRealNameVerified: false
    }),
    updateProfile: mocks.updateProfile
  }
}))
vi.mock('@/services/healthDataService', () => ({
  healthDataService: {
    clearHealthData: mocks.clearHealthData,
    getHealthData: () => null,
    replaceHealthData: mocks.replaceHealthData
  }
}))
vi.mock('@/modules/patient/services/offlineMedication', () => ({ clearMedicalCache: vi.fn() }))
vi.mock('@/modules/patient/services/localMedicationReminders', () => ({ cancelAllMedicationReminders: vi.fn() }))

import ProfilePage from '@/modules/patient/pages/ProfilePage.vue'

const deferred = <T>() => {
  let resolve!: (value: T) => void
  const promise = new Promise<T>((done) => { resolve = done })
  return { promise, resolve }
}

describe('profile account isolation', () => {
  beforeEach(() => {
    mocks.subject = 'account-a'
    mocks.enter = null
    mocks.getProfile.mockReset()
    mocks.getHealthProfile.mockReset()
    mocks.getCurrentAccount.mockReset().mockResolvedValue({ data: { accountType: 'FAMILY' } })
    mocks.clearUserData.mockReset()
    mocks.clearHealthData.mockReset()
    mocks.replaceHealthData.mockReset()
    mocks.updateProfile.mockReset()
  })

  test('clears cached page data and discards previous-account profile responses', async () => {
    const profileA = deferred<{ data: { patientId: number; name: string } }>()
    const healthA = deferred<{ data: { submitTime: string; layer1: { diagnosis: string } } }>()
    const profileB = deferred<{ data: { patientId: number; name: string } }>()
    const healthB = deferred<{ data: { submitTime: string; layer1: { diagnosis: string } } }>()
    mocks.getProfile.mockReturnValueOnce(profileA.promise).mockReturnValueOnce(profileB.promise)
    mocks.getHealthProfile.mockReturnValueOnce(healthA.promise).mockReturnValueOnce(healthB.promise)
    const wrapper = shallowMount(ProfilePage)
    expect(mocks.enter).toBeTypeOf('function')
    await mocks.enter!()
    const state = wrapper.vm.$.setupState
    state.editForm.name = '患者 A'

    mocks.subject = 'account-b'
    window.dispatchEvent(new Event('patient-account-switched'))
    await mocks.enter!()
    expect(state.profileData.name).toBe('患者')
    expect(state.healthData).toBeNull()
    expect(state.editForm.name).toBe('')

    profileA.resolve({ data: { patientId: 1, name: '患者 A' } })
    healthA.resolve({ data: { submitTime: '2026-09-23T00:00:00Z', layer1: { diagnosis: 'A 的诊断' } } })
    await flushPromises()
    expect(state.profileData.name).toBe('患者')
    expect(state.healthData).toBeNull()

    profileB.resolve({ data: { patientId: 2, name: '患者 B' } })
    healthB.resolve({ data: { submitTime: '2026-09-23T00:00:00Z', layer1: { diagnosis: 'B 的诊断' } } })
    await flushPromises()
    expect(state.profileData.name).toBe('患者 B')
    expect(state.healthData.layer1.diagnosis).toBe('B 的诊断')
    expect(mocks.updateProfile).toHaveBeenCalledTimes(1)
    expect(mocks.clearUserData).toHaveBeenCalled()
    expect(mocks.clearHealthData).toHaveBeenCalled()
    wrapper.unmount()
  })
})
