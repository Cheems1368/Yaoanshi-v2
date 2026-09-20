import { appNotificationService } from '@/modules/patient/api/mvp'

const EVENT_NAME = 'app-notification'
const latestCursorByAccount = new Map<string, number>()

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

export function createAppNotificationClient(
  getToken?: () => string | null,
  getAccountSubject?: () => string | null
) {
  const accountSubject = getAccountSubject?.() ?? null
  let socket: WebSocket | null = null
  let reconnectTimer: number | null = null
  let manualClose = false
  let connecting = false
  let pollingTimer: number | null = null
  let lastSeenId = accountSubject ? latestCursorByAccount.get(accountSubject) : undefined
  let pollingAttempt: Promise<boolean> | null = null
  let backfillComplete = false
  const dispatchedIds = new Set<number>()

  const dispatch = (payload: unknown) => {
    window.dispatchEvent(new CustomEvent(EVENT_NAME, { detail: payload }))
  }

  const rememberDispatchedId = (id: number) => {
    dispatchedIds.add(id)
    if (dispatchedIds.size > 200) dispatchedIds.delete(dispatchedIds.values().next().value as number)
  }

  const advanceCursor = (id: number) => {
    lastSeenId = Math.max(lastSeenId ?? 0, id)
    if (accountSubject) latestCursorByAccount.set(accountSubject, lastSeenId)
  }

  const pollLogs = () => {
    if (manualClose) return Promise.resolve(false)
    if (pollingAttempt) return pollingAttempt
    const attempt = (async () => {
      try {
        while (!manualClose) {
          const response = await appNotificationService.getLogs(lastSeenId, 50)
          if (manualClose) return false
          const logs = response.data ?? []
          for (const log of logs) {
            advanceCursor(log.id)
            let payload: Record<string, unknown> = {}
            if (log.payloadJson) {
              try { payload = JSON.parse(log.payloadJson) } catch { payload = {} }
            }
            if (!dispatchedIds.has(log.id)) {
              rememberDispatchedId(log.id)
              dispatch({ ...payload, id: log.id, businessType: log.businessType, businessId: log.businessId })
            }
          }
          if (logs.length < 50) return true
        }
        return false
      } catch {
        // The next bounded poll retries; the main UI remains usable.
        return false
      }
    })().finally(() => {
      if (pollingAttempt === attempt) pollingAttempt = null
    })
    pollingAttempt = attempt
    return attempt
  }

  const startPolling = () => {
    if (manualClose || pollingTimer !== null) return
    backfillComplete = false
    void pollLogs().then((caughtUp) => { backfillComplete = caughtUp })
    pollingTimer = window.setInterval(() => {
      void pollLogs().then((caughtUp) => { if (caughtUp) backfillComplete = true })
    }, 5000)
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
        if (typeof payload?.id === 'number') {
          if (dispatchedIds.has(payload.id)) return
          rememberDispatchedId(payload.id)
          if (backfillComplete && pollingAttempt === null) {
            advanceCursor(payload.id)
          }
        }
        dispatch(payload)
      } catch { /* malformed realtime events are ignored */ }
    }

    socket.onopen = () => {
      void pollLogs().then((caughtUp) => {
        backfillComplete = caughtUp
        if (caughtUp && socket?.readyState === WebSocket.OPEN) stopPolling()
      })
    }

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
