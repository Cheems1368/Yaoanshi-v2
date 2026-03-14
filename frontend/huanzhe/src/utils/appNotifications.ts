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
      console.error('Failed to create app notification socket ticket:', error)
      scheduleReconnect()
      return
    } finally {
      connecting = false
    }

    socket.onmessage = (event) => {
      try {
        const payload = JSON.parse(event.data)
        window.dispatchEvent(new CustomEvent(EVENT_NAME, { detail: payload }))
      } catch (error) {
        console.error('Failed to parse app notification:', error)
      }
    }

    socket.onerror = () => {
      if (socket && socket.readyState === WebSocket.OPEN) {
        socket.close()
      }
    }

    socket.onclose = () => {
      socket = null
      scheduleReconnect()
    }
  }

  return {
    connect,
    disconnect
  }
}

export const APP_NOTIFICATION_EVENT = EVENT_NAME
