import { ElNotification } from 'element-plus'
import { createNotificationSocketTicket, getNotificationBaseline, getPersonalNotifications } from '@/api/auth'

const EVENT_NAME = 'admin-notification'
let soundEnabled = false
let audioContext = null

export function isNotificationSoundEnabled() {
  return soundEnabled
}

export function setNotificationSoundEnabled(enabled) {
  soundEnabled = enabled
  if (enabled) {
    const AudioContext = window.AudioContext || window.webkitAudioContext
    if (AudioContext) {
      audioContext ||= new AudioContext()
      audioContext.resume().catch(() => {})
    }
  }
}

function playNotificationSound() {
  if (!soundEnabled || !audioContext || audioContext.state !== 'running') return
  const oscillator = audioContext.createOscillator()
  const gain = audioContext.createGain()
  oscillator.frequency.value = 740
  gain.gain.setValueAtTime(0.06, audioContext.currentTime)
  gain.gain.exponentialRampToValueAtTime(0.001, audioContext.currentTime + 0.15)
  oscillator.connect(gain).connect(audioContext.destination)
  oscillator.start()
  oscillator.stop(audioContext.currentTime + 0.15)
}

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
  let catchupTimer = null
  let identity = ''
  let generation = 0
  let connecting = false
  let catchingUp = false
  let cursor = null
  let seen = new Set()
  let queuedRealtime = []

  const active = (run) => run === generation && Boolean(identity && getToken?.())

  const disconnect = () => {
    generation++
    identity = ''
    connecting = false
    catchingUp = false
    cursor = null
    seen = new Set()
    queuedRealtime = []
    if (reconnectTimer) window.clearTimeout(reconnectTimer)
    if (catchupTimer) window.clearTimeout(catchupTimer)
    reconnectTimer = null
    catchupTimer = null
    if (socket) socket.close()
    socket = null
  }

  const showRealtime = (payload) => {
    const id = Number(payload.logId)
    if (!Number.isSafeInteger(id) || id <= cursor || seen.has(id)) return
    seen.add(id)
    window.dispatchEvent(new CustomEvent(EVENT_NAME, { detail: payload }))
    ElNotification({
      title: notificationTitle(payload.businessType),
      message: notificationMessage(payload),
      type: 'info',
      duration: 4000
    })
    playNotificationSound()
  }

  const showCompensation = (count) => {
    if (!count) return
    ElNotification({
      title: '断线期间有新工单通知',
      message: `已补齐 ${count} 条通知，点击查看工单`,
      type: 'info',
      duration: 6000,
      onClick: () => { window.location.href = '/workorders' }
    })
    window.dispatchEvent(new CustomEvent(EVENT_NAME, { detail: { compensated: count } }))
  }

  const scheduleReconnect = (run) => {
    if (!active(run) || reconnectTimer) return
    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null
      connect(identity)
    }, 3000)
  }

  const catchUp = async (run) => {
    if (!active(run) || catchingUp) return
    catchingUp = true
    let count = 0
    try {
      let hasMore
      do {
        const response = await getPersonalNotifications(cursor, 50)
        if (!active(run)) return
        const page = response.data
        for (const log of page.list || []) {
          const id = Number(log.id)
          if (id > cursor && !seen.has(id)) count++
        }
        cursor = Number(page.nextCursor)
        seen = new Set([...seen].filter((id) => id > cursor))
        hasMore = page.hasMore
      } while (hasMore)
      showCompensation(count)
    } catch (error) {
      if (active(run)) {
        console.error('Failed to compensate admin notifications:', error)
        catchupTimer = window.setTimeout(() => {
          catchupTimer = null
          if (active(run)) catchUp(run)
        }, 3000)
        showCompensation(count)
      }
    } finally {
      if (active(run)) {
        catchingUp = false
        const pending = queuedRealtime
        queuedRealtime = []
        pending.forEach(showRealtime)
      }
    }
  }

  const connect = async (nextIdentity) => {
    if (!nextIdentity || !getToken?.()) {
      disconnect()
      return
    }
    if (identity !== nextIdentity) {
      disconnect()
      identity = nextIdentity
    }
    if (connecting || socket?.readyState === WebSocket.OPEN || socket?.readyState === WebSocket.CONNECTING) return
    const run = generation
    connecting = true
    try {
      if (cursor === null) {
        const baseline = await getNotificationBaseline()
        if (!active(run)) return
        cursor = Number(baseline.data?.cursor || 0)
      }
      const response = await createNotificationSocketTicket()
      if (!active(run)) return
      const ticket = response.data?.ticket
      if (!ticket) throw new Error('Missing notification socket ticket')
      const connection = new WebSocket(`${resolveWebSocketUrl()}?ticket=${encodeURIComponent(ticket)}`)
      socket = connection
      connection.onopen = () => { if (active(run)) catchUp(run) }
      connection.onmessage = (event) => {
        if (!active(run)) return
        try {
          const payload = JSON.parse(event.data)
          if (catchingUp) queuedRealtime.push(payload)
          else showRealtime(payload)
        } catch (error) {
          console.error('Failed to parse admin notification:', error)
        }
      }
      connection.onerror = () => { if (active(run)) connection.close() }
      connection.onclose = () => {
        if (!active(run) || socket !== connection) return
        socket = null
        scheduleReconnect(run)
      }
    } catch (error) {
      if (active(run)) {
        console.error('Failed to connect admin notifications:', error)
        scheduleReconnect(run)
      }
    } finally {
      if (active(run)) connecting = false
    }
  }

  return { connect, disconnect }
}

export const ADMIN_NOTIFICATION_EVENT = EVENT_NAME
