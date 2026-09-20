import { afterEach, describe, expect, test, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  createSocketTicket: vi.fn(),
  getLogs: vi.fn()
}))

vi.mock('@/modules/patient/api/mvp', () => ({
  appNotificationService: mocks
}))

import { APP_NOTIFICATION_EVENT, createAppNotificationClient } from '@/utils/appNotifications'

describe('patient notification fallback', () => {
  afterEach(() => {
    mocks.createSocketTicket.mockReset()
    mocks.getLogs.mockReset()
  })

  test('drains every notification-log page before relying on reconnect', async () => {
    const firstPage = Array.from({ length: 50 }, (_, index) => ({
      id: index + 1,
      channel: 'IN_APP',
      businessType: 'HELP_ORDER',
      businessId: index + 1,
      deliveryStatus: 'SENT'
    }))
    mocks.createSocketTicket.mockRejectedValue(new Error('unavailable'))
    mocks.getLogs
      .mockResolvedValueOnce({ data: firstPage })
      .mockResolvedValueOnce({ data: [{ ...firstPage[0], id: 51, businessId: 51 }] })
    const received: number[] = []
    const listener = (event: Event) => received.push((event as CustomEvent).detail.id)
    window.addEventListener(APP_NOTIFICATION_EVENT, listener)
    const client = createAppNotificationClient(() => 'token')

    await client.connect()
    await vi.waitFor(() => expect(mocks.getLogs).toHaveBeenCalledTimes(2))

    expect(mocks.getLogs).toHaveBeenNthCalledWith(1, undefined, 50)
    expect(mocks.getLogs).toHaveBeenNthCalledWith(2, 50, 50)
    expect(received).toEqual(Array.from({ length: 51 }, (_, index) => index + 1))
    client.disconnect()
    window.removeEventListener(APP_NOTIFICATION_EVENT, listener)
  })
})
