import { ElNotification } from 'element-plus'
import { createNotificationSocketTicket } from '@/api/auth'

const EVENT_NAME = 'admin-notification'

function resolveWebSocketUrl() {
  const configuredBase = import.meta.env.VITE_BACKEND_V2_WS_BASE_URL || ''
  const base = configuredBase || window.location.origin
  const normalizedBase = base.replace(/\/$/, '')
  const wsBase = normalizedBase.startsWith('ws')
    ? normalizedBase
    : normalizedBase.replace(/^http/, 'ws')
  return `${wsBase}/ws/admin-notifications`
}

function notificationTitle(businessType) {
  const map = {
    HELP_ORDER_SUBMITTED: '鏂板伐鍗曞埌杈?',
    HELP_ORDER_ESCALATED: '宸ュ崟宸插崌绾?',
    HELP_ORDER_REPLY: '宸ュ崟鏈夋柊鍥炲',
    HELP_ORDER_CLOSED: '宸ュ崟宸插叧闂?'
  }
  return map[businessType] || '鏂伴€氱煡'
}

function notificationMessage(payload) {
  if (payload.businessType === 'HELP_ORDER_ESCALATED') {
    return '鏈夊伐鍗曞凡鍗囩骇鍒板尰鐢熼槦鍒楋紝璇峰敖蹇鐞嗐€?'
  }
  if (payload.businessType === 'HELP_ORDER_REPLY') {
    return '宸ュ崟閾捐矾鍑虹幇鏂扮殑鍥炲銆?'
  }
  if (payload.businessType === 'HELP_ORDER_CLOSED') {
    return '鏈夊伐鍗曞凡缁忓叧闂紝鍙埛鏂版煡鐪嬬粨鏋溿€?'
  }
  return '鏀跺埌鏂扮殑涓氬姟閫氱煡銆?'
}

export function createAdminNotificationClient(getToken) {
  let socket = null
  let reconnectTimer = null
  let manualClose = false
  let connecting = false

  const scheduleReconnect = () => {
    if (!manualClose && !reconnectTimer) {
      reconnectTimer = window.setTimeout(() => {
        reconnectTimer = null
        connect()
      }, 3000)
    }
  }

  const disconnect = () => {
    manualClose = true
    if (reconnectTimer) {
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
      const response = await createNotificationSocketTicket()
      const ticket = response.data?.ticket
      if (!ticket) {
        throw new Error('Missing notification socket ticket')
      }

      socket = new WebSocket(`${resolveWebSocketUrl()}?ticket=${encodeURIComponent(ticket)}`)
    } catch (error) {
      console.error('Failed to create admin notification socket ticket:', error)
      scheduleReconnect()
      return
    } finally {
      connecting = false
    }

    socket.onmessage = (event) => {
      try {
        const payload = JSON.parse(event.data)
        window.dispatchEvent(new CustomEvent(EVENT_NAME, { detail: payload }))
        ElNotification({
          title: notificationTitle(payload.businessType),
          message: notificationMessage(payload),
          type: 'info',
          duration: 4000
        })
      } catch (error) {
        console.error('Failed to parse admin notification:', error)
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

export const ADMIN_NOTIFICATION_EVENT = EVENT_NAME
