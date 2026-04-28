import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ADMIN_NOTIFICATION_EVENT } from '@/utils/adminNotifications'

const STATUS_TEXT_PHARMACIST = {
  SUBMITTED: '待药师处理',
  PHARMACIST_IN_PROGRESS: '药师处理中',
  WAITING_DOCTOR: '待医生处理',
  DOCTOR_IN_PROGRESS: '医生处理中',
  CLOSED: '已关闭',
  REJECTED: '已驳回'
}

const STATUS_TEXT_DOCTOR = {
  WAITING_DOCTOR: '待医生处理',
  DOCTOR_IN_PROGRESS: '医生处理中',
  CLOSED: '已关闭',
  REJECTED: '已驳回'
}

const CATEGORY_TEXT = {
  MEDICATION_CONSULTATION: '用药咨询',
  MISSED_OR_WRONG_DOSE: '漏服 / 错服',
  ADVERSE_REACTION: '不良反应',
  PLAN_CHANGE: '计划调整',
  OTHER: '其他'
}

const EVENT_TEXT = {
  SUBMITTED: '患者提交',
  PATIENT_REPLY: '患者追问',
  ASSIGNED: '药师认领',
  PHARMACIST_REPLY: '药师回复',
  ESCALATED_TO_DOCTOR: '升级医生',
  DOCTOR_REPLY: '医生回复',
  REJECTED: '工单驳回',
  CLOSED: '工单关闭'
}

export function statusTagType(status) {
  if (status === 'CLOSED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'WAITING_DOCTOR' || status === 'DOCTOR_IN_PROGRESS') return 'warning'
  return 'primary'
}

export function statusText(status, role = 'pharmacist') {
  const map = role === 'doctor' ? STATUS_TEXT_DOCTOR : STATUS_TEXT_PHARMACIST
  return map[status] || status
}

export function categoryText(category) {
  return CATEGORY_TEXT[category] || category
}

export function eventText(eventType) {
  return EVENT_TEXT[eventType] || eventType
}

export function formatDate(value) {
  if (!value) return '--'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

export function relativeTime(value) {
  if (!value) return ''
  const now = Date.now()
  const then = new Date(value).getTime()
  if (Number.isNaN(then)) return value
  const diff = Math.floor((now - then) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  return `${Math.floor(diff / 86400)}天前`
}

export function useWorkOrders(role, api) {
  const route = useRoute()
  const router = useRouter()

  const query = reactive({ status: '', pageNo: 1, pageSize: 20 })
  const orders = ref([])
  const total = ref(0)
  const loading = ref(false)

  const selectedId = ref(null)
  const detail = ref(null)
  const detailLoading = ref(false)

  const drafts = reactive(new Map())
  const unreadIds = reactive(new Set())

  async function loadOrders() {
    loading.value = true
    try {
      const response = await api.getWorkOrders({ ...query })
      orders.value = response.data?.list || []
      total.value = response.data?.total || 0
    } catch {
      orders.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  async function loadDetail(id) {
    detailLoading.value = true
    try {
      const response = await api.getWorkOrderDetail(id)
      detail.value = response.data
    } finally {
      detailLoading.value = false
    }
  }

  async function selectOrder(id) {
    selectedId.value = id
    unreadIds.delete(id)
    await loadDetail(id)
    router.replace({ query: { ...route.query, id } })
  }

  async function claim(id) {
    if (!api.claimWorkOrder) return
    await api.claimWorkOrder(id)
    ElMessage.success('工单已认领')
    await refreshAfterAction(id)
  }

  async function reply(id, content) {
    await api.replyWorkOrder(id, { content })
    ElMessage.success('回复已提交')
    clearDraft(id)
    await refreshAfterAction(id)
  }

  async function escalate(id, note) {
    if (!api.escalateWorkOrder) return
    await api.escalateWorkOrder(id, { note: note || '' })
    ElMessage.success('已升级到医生队列')
    clearDraft(id)
    await refreshAfterAction(id)
  }

  async function reject(id, reason) {
    await api.rejectWorkOrder(id, { reason })
    ElMessage.success('工单已驳回')
    clearDraft(id)
    await refreshAfterAction(id)
  }

  async function close(id, note) {
    await api.closeWorkOrder(id, { note: note || '' })
    ElMessage.success('工单已关闭')
    clearDraft(id)
    await refreshAfterAction(id)
  }

  async function refreshAfterAction(id) {
    await loadOrders()
    if (selectedId.value === id) {
      await loadDetail(id)
    }
  }

  function saveDraft(id, action, text) {
    if (text?.trim()) {
      drafts.set(id, { action, text })
    }
  }

  function getDraft(id) {
    return drafts.get(id) || null
  }

  function clearDraft(id) {
    drafts.delete(id)
  }

  function handleNotification(event) {
    if (!event?.detail?.businessType?.startsWith('HELP_ORDER_')) return
    loadOrders()
    const notifOrderId = event.detail?.businessId
    if (notifOrderId && notifOrderId !== selectedId.value) {
      unreadIds.add(notifOrderId)
    }
    if (notifOrderId && notifOrderId === selectedId.value) {
      loadDetail(selectedId.value)
    }
  }

  function initFromRoute() {
    const idFromQuery = route.query.id
    if (idFromQuery) {
      selectOrder(Number(idFromQuery) || idFromQuery)
    }
  }

  onMounted(() => {
    loadOrders().then(initFromRoute)
    window.addEventListener(ADMIN_NOTIFICATION_EVENT, handleNotification)
  })

  onBeforeUnmount(() => {
    window.removeEventListener(ADMIN_NOTIFICATION_EVENT, handleNotification)
  })

  return {
    query, orders, total, loading,
    selectedId, detail, detailLoading,
    drafts, unreadIds,
    loadOrders, selectOrder, loadDetail,
    claim, reply, escalate, reject, close,
    saveDraft, getDraft, clearDraft
  }
}
