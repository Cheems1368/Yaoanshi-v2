<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/tabs/help-order" />
        </ion-buttons>
        <ion-title>工单详情</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="page-content">
      <div class="page-container">
        <ion-card v-if="loading && !order" class="empty-card">
          <ion-card-content class="empty-state">
            <ion-spinner name="crescent" />
          </ion-card-content>
        </ion-card>

        <template v-else-if="order">
          <ion-card class="summary-card">
            <ion-card-content>
              <div class="summary-top">
                <div>
                  <div class="summary-title">{{ order.title }}</div>
                  <div class="summary-meta">
                    {{ categoryText(order.category) }} · {{ formatDateTime(order.createdAt) }}
                  </div>
                </div>
                <ion-badge :color="statusColor(order.status)">{{ statusText(order.status) }}</ion-badge>
              </div>

              <div class="summary-grid">
                <div class="summary-item">
                  <span class="summary-label">当前处理</span>
                  <span>{{ order.currentHandlerName || handlerText(order.currentHandlerType) || '待分配' }}</span>
                </div>
                <div class="summary-item">
                  <span class="summary-label">优先级</span>
                  <span>{{ priorityText(order.priority) }}</span>
                </div>
              </div>

              <div v-if="order.description" class="summary-body">{{ order.description }}</div>
              <div v-if="order.requiresOfflineEmergency" class="summary-notice summary-notice--warning">
                已标记线下急诊提醒，如症状明显加重请优先线下就医。
              </div>
              <div v-if="order.rejectReason" class="summary-notice summary-notice--danger">
                驳回原因：{{ order.rejectReason }}
              </div>
              <div v-if="order.serviceNotice" class="summary-notice">
                服务说明：{{ order.serviceNotice }}
              </div>
            </ion-card-content>
          </ion-card>

          <ion-card class="timeline-card">
            <ion-card-header>
              <ion-card-title>会话记录</ion-card-title>
              <ion-card-subtitle>支持轮询刷新，收到推送会立即同步</ion-card-subtitle>
            </ion-card-header>
            <ion-card-content>
              <div v-if="events.length === 0" class="empty-state">
                <ion-icon :icon="chatboxEllipsesOutline" class="empty-icon" />
                <p class="empty-text">暂时还没有会话记录</p>
              </div>

              <div v-else class="event-list">
                <div
                  v-for="event in events"
                  :key="event.id"
                  class="event-row"
                  :class="eventRowClass(event)"
                >
                  <div class="event-bubble" :class="eventBubbleClass(event)">
                    <div class="event-title">{{ eventTitle(event) }}</div>
                    <div class="event-meta">
                      <span>{{ eventActor(event) }}</span>
                      <span>{{ formatDateTime(event.eventAt) }}</span>
                    </div>
                    <div v-if="event.content" class="event-content">{{ event.content }}</div>
                  </div>
                </div>
              </div>
            </ion-card-content>
          </ion-card>
        </template>
      </div>

      <ion-toast :is-open="showToast" :message="toastMessage" :duration="2400" @did-dismiss="showToast = false" />
    </ion-content>

    <ion-footer v-if="order">
      <ion-toolbar class="composer-toolbar">
        <div v-if="isTerminalStatus" class="terminal-banner">
          {{ order.status === 'CLOSED' ? '工单已关闭，当前仅支持查看会话记录。' : '工单已驳回，当前不可继续发送消息。' }}
        </div>
        <div v-else class="composer">
          <ion-textarea
            v-model="replyContent"
            class="composer-input"
            auto-grow
            :rows="2"
            placeholder="继续补充药名、症状变化或想确认的问题"
          />
          <ion-button class="composer-button" :disabled="replying || !canReply" @click="sendReply">
            {{ replying ? '发送中...' : '发送' }}
          </ion-button>
        </div>
      </ion-toolbar>
    </ion-footer>
  </ion-page>
</template>

<script setup lang="ts">
import {
  IonBackButton,
  IonBadge,
  IonButton,
  IonButtons,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardSubtitle,
  IonCardTitle,
  IonContent,
  IonFooter,
  IonHeader,
  IonIcon,
  IonPage,
  IonSpinner,
  IonTextarea,
  IonTitle,
  IonToast,
  IonToolbar,
  onIonViewDidLeave,
  onIonViewWillEnter
} from '@ionic/vue'
import { chatboxEllipsesOutline } from 'ionicons/icons'
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { helpOrderService, type HelpOrderDTO, type HelpOrderEventDTO } from '@/modules/patient/api/mvp'
import { patientSession } from '@/app/session'
import { APP_NOTIFICATION_EVENT, createAppNotificationClient } from '@/utils/appNotifications'

const route = useRoute()
const order = ref<HelpOrderDTO | null>(null)
const loading = ref(false)
const replying = ref(false)
const replyContent = ref('')
const showToast = ref(false)
const toastMessage = ref('')

let pollTimer: number | null = null
let notificationClient: ReturnType<typeof createAppNotificationClient> | null = null

const orderId = computed(() => Number(route.params.id))
const events = computed(() => order.value?.events ?? [])
const isTerminalStatus = computed(() => ['CLOSED', 'REJECTED'].includes(order.value?.status || ''))
const canReply = computed(() => Boolean(replyContent.value.trim()) && !isTerminalStatus.value)

const showMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}

const loadDetail = async (silent = false) => {
  if (!silent) {
    loading.value = true
  }
  try {
    const response = await helpOrderService.getHelpOrderDetail(orderId.value)
    order.value = response.data ?? null
    connectRealtime()
  } catch (error: any) {
    if (!silent) {
      order.value = null
      showMessage(error?.message || '工单详情加载失败')
    }
  } finally {
    if (!silent) {
      loading.value = false
    }
  }
}

const sendReply = async () => {
  if (!canReply.value) {
    showMessage('请输入要补充的内容')
    return
  }

  replying.value = true
  try {
    const response = await helpOrderService.replyHelpOrder(orderId.value, {
      content: replyContent.value.trim()
    })
    order.value = response.data ?? order.value
    replyContent.value = ''
  } catch (error: any) {
    showMessage(error?.message || '发送失败')
  } finally {
    replying.value = false
  }
}

const startPolling = () => {
  if (pollTimer !== null) {
    return
  }
  pollTimer = window.setInterval(() => {
    void loadDetail(true)
  }, 5000)
}

const stopPolling = () => {
  if (pollTimer !== null) {
    window.clearInterval(pollTimer)
    pollTimer = null
  }
}

const connectRealtime = () => {
  if (notificationClient) {
    return
  }
  notificationClient = createAppNotificationClient(() => patientSession.getAccessToken())
  void notificationClient.connect()
}

const disconnectRealtime = () => {
  notificationClient?.disconnect()
  notificationClient = null
}

const handleRealtimeEvent = (event: Event) => {
  const detail = (event as CustomEvent).detail as { businessId?: number | string } | undefined
  if (!detail) {
    return
  }
  if (Number(detail.businessId) !== orderId.value) {
    return
  }
  void loadDetail(true)
}

const categoryText = (category: string) => {
  const map: Record<string, string> = {
    MEDICATION_CONSULTATION: '用药咨询',
    MISSED_OR_WRONG_DOSE: '漏服 / 错服',
    ADVERSE_REACTION: '不良反应',
    PLAN_CHANGE: '计划调整',
    OTHER: '其他'
  }
  return map[category] || category
}

const statusText = (status: string) => {
  const map: Record<string, string> = {
    SUBMITTED: '待药师处理',
    PHARMACIST_IN_PROGRESS: '药师处理中',
    WAITING_DOCTOR: '待医生处理',
    DOCTOR_IN_PROGRESS: '医生处理中',
    CLOSED: '已关闭',
    REJECTED: '已驳回'
  }
  return map[status] || status
}

const statusColor = (status: string) => {
  if (status === 'CLOSED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'WAITING_DOCTOR' || status === 'DOCTOR_IN_PROGRESS') return 'warning'
  return 'primary'
}

const priorityText = (priority?: string | null) => {
  if (priority === 'URGENT') return '紧急'
  if (priority === 'HIGH') return '高'
  return '普通'
}

const handlerText = (handlerType?: string | null) => {
  if (handlerType === 'PHARMACIST') return '药师'
  if (handlerType === 'DOCTOR') return '医生'
  return ''
}

const formatDateTime = (value?: string | null) => {
  if (!value) return '--'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

const eventRowClass = (event: HelpOrderEventDTO) => {
  if (event.operatorType === 'PATIENT' || event.operatorType === 'FAMILY') {
    return 'event-row--outgoing'
  }
  if (event.operatorType === 'SYSTEM') {
    return 'event-row--system'
  }
  return 'event-row--incoming'
}

const eventBubbleClass = (event: HelpOrderEventDTO) => {
  if (event.operatorType === 'PATIENT' || event.operatorType === 'FAMILY') {
    return 'event-bubble--outgoing'
  }
  if (event.operatorType === 'SYSTEM') {
    return 'event-bubble--system'
  }
  return 'event-bubble--incoming'
}

const eventTitle = (event: HelpOrderEventDTO) => {
  const map: Record<string, string> = {
    SUBMITTED: '已提交工单',
    PATIENT_REPLY: '补充消息',
    ASSIGNED: '药师已接单',
    PHARMACIST_REPLY: '药师回复',
    ESCALATED_TO_DOCTOR: '已升级给医生',
    DOCTOR_REPLY: '医生回复',
    REJECTED: '工单已驳回',
    CLOSED: '工单已关闭',
    SYSTEM_NOTICE: '系统通知'
  }
  return map[event.eventType] || event.eventType
}

const eventActor = (event: HelpOrderEventDTO) => {
  if (event.operatorType === 'PATIENT') return event.operatorName || '本人'
  if (event.operatorType === 'FAMILY') return event.operatorName || '家属'
  if (event.operatorType === 'PHARMACIST') return event.operatorName || '药师'
  if (event.operatorType === 'DOCTOR') return event.operatorName || '医生'
  return '系统'
}

onIonViewWillEnter(async () => {
  window.addEventListener(APP_NOTIFICATION_EVENT, handleRealtimeEvent as EventListener)
  await loadDetail()
  startPolling()
})

onIonViewDidLeave(() => {
  window.removeEventListener(APP_NOTIFICATION_EVENT, handleRealtimeEvent as EventListener)
  stopPolling()
  disconnectRealtime()
})
</script>

<style scoped>
.page-content {
  --background: #f6f8fb;
}

.page-container {
  padding: 16px;
}

.summary-card,
.timeline-card,
.empty-card {
  border-radius: 18px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.summary-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.summary-title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
}

.summary-meta,
.summary-label,
.empty-text,
.event-meta {
  color: #64748b;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
}

.summary-item {
  display: grid;
  gap: 6px;
  padding: 12px;
  border-radius: 14px;
  background: #f8fafc;
  color: #0f172a;
}

.summary-body {
  margin-top: 14px;
  color: #334155;
  line-height: 1.7;
  white-space: pre-wrap;
}

.summary-notice {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #eff6ff;
  color: #1d4ed8;
  line-height: 1.6;
}

.summary-notice--warning {
  background: #fff7ed;
  color: #c2410c;
}

.summary-notice--danger {
  background: #fef2f2;
  color: #b91c1c;
}

.event-list {
  display: grid;
  gap: 12px;
}

.event-row {
  display: flex;
}

.event-row--incoming {
  justify-content: flex-start;
}

.event-row--outgoing {
  justify-content: flex-end;
}

.event-row--system {
  justify-content: center;
}

.event-bubble {
  max-width: min(86%, 460px);
  padding: 14px;
  border-radius: 18px;
  box-shadow: 0 10px 18px rgba(15, 23, 42, 0.06);
}

.event-bubble--incoming {
  background: #ffffff;
  color: #0f172a;
}

.event-bubble--outgoing {
  background: linear-gradient(135deg, #1d4ed8, #0f766e);
  color: #ffffff;
}

.event-bubble--outgoing .event-meta,
.event-bubble--outgoing .event-content {
  color: rgba(255, 255, 255, 0.88);
}

.event-bubble--system {
  background: #e2e8f0;
  color: #334155;
}

.event-title {
  font-weight: 700;
}

.event-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 8px;
  font-size: 12px;
}

.event-content {
  margin-top: 10px;
  line-height: 1.7;
  white-space: pre-wrap;
}

.composer-toolbar {
  --padding-top: 8px;
  --padding-bottom: 8px;
  --padding-start: 16px;
  --padding-end: 16px;
}

.composer {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.composer-input {
  flex: 1;
  --background: #f8fafc;
  --border-radius: 16px;
  border: 1px solid #dbe3f0;
  border-radius: 16px;
  padding: 4px 8px;
}

.composer-button {
  align-self: stretch;
}

.terminal-banner {
  padding: 10px 0;
  color: #64748b;
  line-height: 1.6;
}

.empty-state {
  text-align: center;
  padding: 32px 12px;
}

.empty-icon {
  font-size: 36px;
  color: #94a3b8;
}
</style>
