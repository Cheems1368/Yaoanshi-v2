import { appNotificationService } from '@/modules/patient/api/mvp'

const EVENT_NAME = 'app-notification'

function resolveWebSocketUrl() {
  const configuredBase = typeof __API_BASE_URL__ === 'string' && __API_BASE_URL__
    ? __API_BASE_URL__
    : window.location.origin
  const normalizedBase = configuredBase.replace(/\/$/, '')
  const wsBase = normalizedBase.startsWith('ws')
    ? normalizedBase
    : normalizedBase.replace(/^http/, 'ws')
  return `${wsBase}/ws/app-notifications`
}

export function createAppNotificationClient(getToken?: () => string | null) {
  let socket: WebSocket | null = null
  let reconnectTimer: number | null = null
  let manualClose = false
  let connecting = false
  let pollingTimer: number | null = null
  let lastSeenId: number | undefined

  const dispatch = (payload: unknown) => {
    window.dispatchEvent(new CustomEvent(EVENT_NAME, { detail: payload }))
  }

  const pollLogs = async () => {
    if (manualClose) return
    try {
      const response = await appNotificationService.getLogs(lastSeenId, 50)
      for (const log of response.data ?? []) {
        lastSeenId = Math.max(lastSeenId ?? 0, log.id)
        let payload: Record<string, unknown> = {}
        if (log.payloadJson) {
          try { payload = JSON.parse(log.payloadJson) } catch { payload = {} }
        }
        dispatch({ ...payload, id: log.id, businessType: log.businessType, businessId: log.businessId })
      }
    } catch {
      // The next bounded poll retries; the main UI remains usable.
    }
  }

  const startPolling = () => {
    if (manualClose || pollingTimer !== null) return
    void pollLogs()
    pollingTimer = window.setInterval(() => void pollLogs(), 5000)
  }

  const stopPolling = () => {
    if (pollingTimer !== null) {
      window.clearInterval(pollingTimer)
      pollingTimer = null
    }
  }

  const scheduleReconnect = () => {
    if (manualClose || reconnectTimer !== null) {
      return
    }
    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null
      void connect()
    }, 3000)
  }

  const disconnect = () => {
    manualClose = true
    stopPolling()
    if (reconnectTimer !== null) {
      window.clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (socket) {
      socket.close()
      socket = null
    }
  }

  const connect = async () => {
    const token = getToken?.()
    if (!token) {
      disconnect()
      return
    }
    if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
      return
    }
    if (connecting) {
      return
    }

    manualClose = false
    connecting = true

    try {
      const response = await appNotificationService.createSocketTicket()
      const ticket = response.data?.ticket
      if (!ticket) {
        throw new Error('Missing app notification socket ticket')
      }
      socket = new WebSocket(`${resolveWebSocketUrl()}?ticket=${encodeURIComponent(ticket)}`)
    } catch (error) {
      startPolling()
      scheduleReconnect()
      return
    } finally {
      connecting = false
    }

    socket.onmessage = (event) => {
      try {
        const payload = JSON.parse(event.data)
        if (typeof payload?.id === 'number') lastSeenId = Math.max(lastSeenId ?? 0, payload.id)
        dispatch(payload)
      } catch { /* malformed realtime events are ignored */ }
    }

    socket.onopen = stopPolling

    socket.onerror = () => {
      if (socket && socket.readyState === WebSocket.OPEN) {
        socket.close()
      }
    }

    socket.onclose = () => {
      socket = null
      startPolling()
      scheduleReconnect()
    }
  }

  return {
    connect,
    disconnect
  }
}

export const APP_NOTIFICATION_EVENT = EVENT_NAME
