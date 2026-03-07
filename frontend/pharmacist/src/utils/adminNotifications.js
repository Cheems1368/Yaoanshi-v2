import { ElNotification } from 'element-plus'

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
    HELP_ORDER_SUBMITTED: '新工单到达',
    HELP_ORDER_ESCALATED: '工单已升级',
    HELP_ORDER_REPLY: '工单有新回复',
    HELP_ORDER_CLOSED: '工单已关闭'
  }
  return map[businessType] || '新通知'
}

function notificationMessage(payload) {
  if (payload.businessType === 'HELP_ORDER_SUBMITTED') {
    return '患者提交了新的帮助工单，请及时处理。'
  }
  if (payload.businessType === 'HELP_ORDER_ESCALATED') {
    return '有工单已进入医生处理队列。'
  }
  if (payload.businessType === 'HELP_ORDER_REPLY') {
    return '工单处理链路有新的回复内容。'
  }
  if (payload.businessType === 'HELP_ORDER_CLOSED') {
    return '工单已关闭，可查看最终结果。'
  }
  return '收到新的业务通知。'
}

export function createAdminNotificationClient(getToken) {
  let socket = null
  let reconnectTimer = null
  let manualClose = false

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

  const connect = () => {
    const token = getToken?.()
    if (!token) {
      disconnect()
      return
    }
    if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
      return
    }

    manualClose = false
    socket = new WebSocket(`${resolveWebSocketUrl()}?access_token=${encodeURIComponent(token)}`)

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
      if (!manualClose) {
        reconnectTimer = window.setTimeout(connect, 3000)
      }
    }
  }

  return {
    connect,
    disconnect
  }
}

export const ADMIN_NOTIFICATION_EVENT = EVENT_NAME
