import { afterEach, describe, expect, test, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  createSocketTicket: vi.fn(),
  getLogs: vi.fn(),
  getLatestLogId: vi.fn()
}))

vi.mock('@/modules/patient/api/mvp', () => ({
  appNotificationService: mocks
}))

import { APP_NOTIFICATION_EVENT, createAppNotificationClient } from '@/utils/appNotifications'

describe('patient notification fallback', () => {
  afterEach(() => {
    mocks.createSocketTicket.mockReset()
    mocks.getLogs.mockReset()
    mocks.getLatestLogId.mockReset()
  })

  test('starts after the server latest cursor instead of replaying history on cold start', async () => {
    mocks.createSocketTicket.mockRejectedValue(new Error('unavailable'))
    mocks.getLatestLogId.mockResolvedValue({ data: 50 })
    mocks.getLogs.mockResolvedValue({ data: [{ id: 51, businessType: 'HELP_ORDER', businessId: 51 }] })
    const received: number[] = []
    const listener = (event: Event) => received.push((event as CustomEvent).detail.id)
    window.addEventListener(APP_NOTIFICATION_EVENT, listener)
    const client = createAppNotificationClient(() => 'token')

    await client.connect()
    await vi.waitFor(() => expect(mocks.getLogs).toHaveBeenCalledTimes(1))

    expect(mocks.getLatestLogId).toHaveBeenCalledTimes(1)
    expect(mocks.getLogs).toHaveBeenCalledWith(50, 50)
    expect(received).toEqual([51])
    client.disconnect()
    window.removeEventListener(APP_NOTIFICATION_EVENT, listener)
  })

  test('reuses the latest cursor when the same account opens a new client', async () => {
    mocks.createSocketTicket.mockRejectedValue(new Error('unavailable'))
    mocks.getLatestLogId.mockResolvedValue({ data: 99 })
    mocks.getLogs
      .mockResolvedValueOnce({ data: [{ id: 100, businessType: 'HELP_ORDER', businessId: 1 }] })
      .mockResolvedValueOnce({ data: [{ id: 101, businessType: 'HELP_ORDER', businessId: 2 }] })
    const received: number[] = []
    const listener = (event: Event) => received.push((event as CustomEvent).detail.id)
    window.addEventListener(APP_NOTIFICATION_EVENT, listener)

    const firstClient = createAppNotificationClient(() => 'token', () => 'account-cursor-test')
    await firstClient.connect()
    await vi.waitFor(() => expect(received).toEqual([100]))
    firstClient.disconnect()

    const secondClient = createAppNotificationClient(() => 'token', () => 'account-cursor-test')
    await secondClient.connect()
    await vi.waitFor(() => expect(mocks.getLogs).toHaveBeenCalledTimes(2))

    expect(mocks.getLatestLogId).toHaveBeenCalledTimes(1)
    expect(mocks.getLogs).toHaveBeenNthCalledWith(1, 99, 50)
    expect(mocks.getLogs).toHaveBeenNthCalledWith(2, 100, 50)
    secondClient.disconnect()
    window.removeEventListener(APP_NOTIFICATION_EVENT, listener)
  })
})
