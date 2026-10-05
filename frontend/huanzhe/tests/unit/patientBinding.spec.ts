import { flushPromises, shallowMount } from '@vue/test-utils'
import { beforeEach, expect, test, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  subject: 'patient-a',
  enter: null as null | (() => void),
  getCurrentPatient: vi.fn(), bindPatient: vi.fn(), getPlan: vi.fn(), updateProfile: vi.fn(),
  setBindingStatus: vi.fn(), setActivePlanStatus: vi.fn(), replace: vi.fn(), login: vi.fn()
}))
vi.mock('@ionic/vue', async (importOriginal) => ({
  ...await importOriginal<typeof import('@ionic/vue')>(),
  onIonViewWillEnter: (handler: () => void) => { mocks.enter = handler }
}))
vi.mock('vue-router', () => ({ useRouter: () => ({ replace: mocks.replace }) }))
vi.mock('@/app/session', () => ({ patientSession: {
  getAccountSubject: () => mocks.subject, getMobile: () => '13800000201', setMobile: vi.fn(),
  setBindingStatus: mocks.setBindingStatus, setActivePlanStatus: mocks.setActivePlanStatus,
  ensureDeviceId: () => 'test-device'
} }))
vi.mock('@/modules/patient/api/mvp', () => ({
  patientService: { getCurrentPatient: mocks.getCurrentPatient, bindPatient: mocks.bindPatient, updateProfile: mocks.updateProfile },
  medicationService: { getActiveMedicationPlan: mocks.getPlan },
  appAuthService: { login: mocks.login }
}))
vi.mock('@/services/userDataService', () => ({ userDataService: {
  getUserProfile: () => ({ ethnicity: '' }), updateRealNameInfo: vi.fn()
} }))

import RealnamePage from '@/modules/auth/pages/RealnamePage.vue'
import LoginPage from '@/modules/auth/pages/LoginPage.vue'

const mountPage = () => shallowMount(RealnamePage, { global: { renderStubDefaultSlot: true, stubs: { AppIcon: true } } })

beforeEach(() => {
  vi.clearAllMocks()
  mocks.subject = 'patient-a'
  mocks.getPlan.mockResolvedValue({ data: null })
  mocks.getCurrentPatient.mockResolvedValue({ data: { name: '合成患者', mobile: '13800000201', hospitalPatientId: 'T9-201', bound: false, bindingStatus: 'PENDING' } })
})

test('keeps pending patients on the confirmation screen without loading medication or verifying identity', async () => {
  const wrapper = mountPage()
  mocks.enter?.()
  await flushPromises()
  expect(wrapper.text()).toContain('等待药师确认')
  expect(mocks.setBindingStatus).toHaveBeenLastCalledWith(false)
  expect(mocks.getPlan).not.toHaveBeenCalled()
  expect(mocks.updateProfile).not.toHaveBeenCalled()
  expect(mocks.replace).not.toHaveBeenCalled()
  wrapper.unmount()
})

test('enters home only after the server reports confirmed binding', async () => {
  const wrapper = mountPage()
  mocks.enter?.()
  await flushPromises()
  mocks.getCurrentPatient.mockResolvedValue({ data: { bound: true, bindingStatus: 'BOUND' } })
  await wrapper.findAll('ion-button-stub').find(button => button.text().includes('刷新确认状态'))?.trigger('click')
  await flushPromises()
  expect(mocks.setBindingStatus).toHaveBeenLastCalledWith(true)
  expect(mocks.getPlan).toHaveBeenCalledOnce()
  expect(mocks.replace).toHaveBeenCalledWith('/tabs/home')
  wrapper.unmount()
})

test('ignores a previous account confirmation response', async () => {
  let resolve!: (value: unknown) => void
  mocks.getCurrentPatient.mockReturnValue(new Promise(done => { resolve = done }))
  const wrapper = mountPage()
  mocks.enter?.()
  mocks.subject = 'patient-b'
  resolve({ data: { bound: true, bindingStatus: 'BOUND' } })
  await flushPromises()
  expect(mocks.setBindingStatus).not.toHaveBeenCalled()
  expect(mocks.replace).not.toHaveBeenCalled()
  wrapper.unmount()
})

test('does not write plan state when the account changes during confirmed binding refresh', async () => {
  let resolve!: (value: unknown) => void
  mocks.getCurrentPatient.mockResolvedValue({ data: { bound: true, bindingStatus: 'BOUND' } })
  mocks.getPlan.mockReturnValue(new Promise(done => { resolve = done }))
  const wrapper = mountPage()
  mocks.enter?.()
  await flushPromises()
  mocks.subject = 'patient-b'
  resolve({ data: { id: 1 } })
  await flushPromises()
  expect(mocks.setActivePlanStatus).not.toHaveBeenCalled()
  expect(mocks.replace).not.toHaveBeenCalled()
  wrapper.unmount()
})

test.each([
  ['PATIENT_BIND_REQUIRES_PHARMACIST_CONFIRM', '该院内编号已关联其他患者，请联系药师核验后再绑定'],
  ['PATIENT_BIND_CONFLICT', '当前账号已绑定其他院内编号，请联系药师核验']
])('explains binding failure %s and allows retry', async (code, message) => {
  const wrapper = mountPage()
  mocks.bindPatient.mockRejectedValue(new Error(code))
  wrapper.getComponent('#name').vm.$emit('update:modelValue', '合成患者')
  wrapper.getComponent('#hospitalPatientId').vm.$emit('update:modelValue', 'T9-201')
  await wrapper.findAll('ion-button-stub').find(button => button.text().includes('提交绑定'))?.trigger('click')
  await flushPromises()
  expect(wrapper.getComponent('ion-toast-stub').props('message')).toBe(message)
  expect(wrapper.getComponent('ion-toast-stub').props('isOpen')).toBe(true)
  expect(mocks.replace).not.toHaveBeenCalled()
  expect(wrapper.text()).toContain('提交绑定')
  wrapper.unmount()
})

test.each([
  ['APP_AUTH_OTP_EXPIRED', '验证码已过期，请重新获取'],
  ['APP_AUTH_OTP_INVALID', '验证码不正确，请核对后重试']
])('explains login failure %s and stays on login', async (code, message) => {
  const wrapper = shallowMount(LoginPage, { global: { renderStubDefaultSlot: true } })
  mocks.login.mockRejectedValue(new Error(code))
  wrapper.getComponent('#otpCode').vm.$emit('update:modelValue', '123456')
  await wrapper.findAll('ion-button-stub').find(button => button.text().trim() === '登录')?.trigger('click')
  await flushPromises()
  expect(wrapper.getComponent('ion-toast-stub').props('message')).toBe(message)
  expect(mocks.replace).not.toHaveBeenCalled()
  wrapper.unmount()
})
