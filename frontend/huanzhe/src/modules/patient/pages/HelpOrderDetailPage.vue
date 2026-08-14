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
              <EmptyState
                v-if="events.length === 0"
                type="no-orders"
                title="暂时还没有会话记录"
              />

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
                    <div v-if="event.attachments?.length" class="event-attachments">
                      <template v-for="attachment in event.attachments" :key="attachment.fileAssetId">
                        <a
                          v-if="attachment.attachmentType !== 'AUDIO'"
                          class="event-attachment"
                          :href="attachment.downloadUrl"
                          target="_blank"
                          rel="noreferrer"
                        >
                          <img :src="attachment.downloadUrl" :alt="attachment.originalFilename" />
                        </a>
                        <div v-else class="voice-attachment">
                          <div class="voice-player-row">
                            <audio :src="attachment.downloadUrl" controls preload="metadata" />
                            <span>{{ attachment.durationSeconds || 1 }}″</span>
                          </div>
                          <div v-if="attachment.voiceTranscript?.text" class="voice-transcript">
                            转写：{{ attachment.voiceTranscript.text }}
                          </div>
                          <div v-else class="voice-transcript voice-transcript--muted">
                            {{ voiceStatusText(attachment.voiceTranscript?.status) }}
                          </div>
                          <button
                            v-if="attachment.id && canTranscribe(attachment)"
                            type="button"
                            class="voice-transcribe"
                            :disabled="transcribingIds.has(attachment.id)"
                            @click="transcribeVoice(attachment.id)"
                          >
                            {{ transcribingIds.has(attachment.id) ? '转写中...' : transcriptActionText(attachment.voiceTranscript?.status) }}
                          </button>
                        </div>
                      </template>
                    </div>
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
          <div class="composer-main">
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
          <div class="composer-images">
            <div class="privacy-warning">补充图片前请确认未拍入姓名、身份证、病历号等敏感信息。</div>
            <div class="image-actions">
              <ion-button fill="clear" size="small" :disabled="replying || selectedImages.length >= 3" @click="openCameraInput">
                拍照
              </ion-button>
              <ion-button fill="clear" size="small" :disabled="replying || selectedImages.length >= 3" @click="openGalleryInput">
                选择图片
              </ion-button>
              <span class="image-count">{{ selectedImages.length }}/3</span>
            </div>
            <button
              type="button"
              class="voice-hold-button"
              :class="{ 'voice-hold-button--recording': recording }"
              :disabled="replying || voiceBusy"
              @pointerdown.prevent="startVoice"
              @pointerup.prevent="finishVoice"
              @pointercancel.prevent="cancelVoice"
              @contextmenu.prevent
            >
              {{ recording ? '松开发送语音' : voiceBusy ? '语音发送中...' : '按住说话' }}
            </button>
            <input
              ref="cameraInput"
              class="file-input"
              type="file"
              accept="image/jpeg,image/png,image/webp"
              capture="environment"
              @change="handleImageInput"
            />
            <input
              ref="galleryInput"
              class="file-input"
              type="file"
              accept="image/jpeg,image/png,image/webp"
              multiple
              @change="handleImageInput"
            />
            <div v-if="selectedImages.length" class="selected-image-list">
              <div v-for="image in selectedImages" :key="image.id" class="selected-image">
                <img :src="image.previewUrl" :alt="image.file.name" />
                <button type="button" :disabled="replying" @click="removeImage(image.id)">移除</button>
              </div>
            </div>
          </div>
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
  IonPage,
  IonSpinner,
  IonTextarea,
  IonTitle,
  IonToast,
  IonToolbar,
  onIonViewDidLeave,
  onIonViewWillEnter
} from '@ionic/vue'
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  helpOrderService,
  type HelpOrderAttachmentDTO,
  type HelpOrderDTO,
  type HelpOrderEventDTO
} from '@/modules/patient/api/mvp'
import { cancelVoiceRecording, startVoiceRecording, stopVoiceRecording } from '@/modules/patient/composables/useVoiceRecorder'
import { uploadHelpOrderVoice } from '@/modules/patient/composables/useHelpOrderVoiceUpload'
import { patientSession } from '@/app/session'
import { APP_NOTIFICATION_EVENT, createAppNotificationClient } from '@/utils/appNotifications'

const route = useRoute()
const order = ref<HelpOrderDTO | null>(null)
const loading = ref(false)
const replying = ref(false)
const replyContent = ref('')
const showToast = ref(false)
const toastMessage = ref('')
const recording = ref(false)
const voiceBusy = ref(false)
const transcribingIds = ref<Set<number>>(new Set())
const cameraInput = ref<HTMLInputElement | null>(null)
const galleryInput = ref<HTMLInputElement | null>(null)
const selectedImages = ref<SelectedImage[]>([])

let pollTimer: number | null = null
let notificationClient: ReturnType<typeof createAppNotificationClient> | null = null

const orderId = computed(() => Number(route.params.id))
const events = computed(() => order.value?.events ?? [])
const isTerminalStatus = computed(() => ['CLOSED', 'REJECTED'].includes(order.value?.status || ''))
const canReply = computed(() => (Boolean(replyContent.value.trim()) || selectedImages.value.length > 0) && !isTerminalStatus.value)

const allowedImageTypes = new Set(['image/jpeg', 'image/png', 'image/webp'])
const maxImageSizeBytes = 10 * 1024 * 1024

interface SelectedImage {
  id: string
  file: File
  previewUrl: string
}

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
    await sendReplyPayload()
  } catch (error: any) {
    showMessage(error?.message || '发送失败')
  } finally {
    replying.value = false
  }
}

const sendReplyPayload = async (extraPayload: Record<string, unknown> = {}) => {
  const attachmentFileAssetIds = await uploadSelectedImages()
  const response = await helpOrderService.replyHelpOrder(orderId.value, {
    content: replyContent.value.trim() || undefined,
    attachmentFileAssetIds,
    ...extraPayload
  })
  order.value = response.data ?? order.value
  replyContent.value = ''
  clearImages()
}

const startVoice = async () => {
  if (replying.value || voiceBusy.value || isTerminalStatus.value) return
  try {
    await startVoiceRecording(() => {
      void finishVoice()
    })
    recording.value = true
  } catch (error: any) {
    showMessage(error?.message === 'VOICE_RECORDER_UNSUPPORTED' ? '当前环境不支持录音' : '无法启动麦克风')
  }
}

const finishVoice = async () => {
  if (!recording.value || voiceBusy.value) return
  recording.value = false
  voiceBusy.value = true
  replying.value = true
  try {
    const recordingResult = await stopVoiceRecording()
    const voiceAttachment = await uploadHelpOrderVoice(recordingResult)
    await sendReplyPayload({ voiceAttachment })
  } catch (error: any) {
    showMessage(error?.message || '语音发送失败')
  } finally {
    voiceBusy.value = false
    replying.value = false
  }
}

const cancelVoice = () => {
  if (!recording.value) return
  recording.value = false
  cancelVoiceRecording()
  showMessage('已取消语音')
}

const transcribeVoice = async (attachmentId: number) => {
  if (transcribingIds.value.has(attachmentId)) return
  transcribingIds.value = new Set([...transcribingIds.value, attachmentId])
  try {
    await helpOrderService.transcribeAttachment(orderId.value, attachmentId)
    await loadDetail(true)
  } catch (error: any) {
    showMessage(error?.message || '转文字失败，可听原音')
  } finally {
    const next = new Set(transcribingIds.value)
    next.delete(attachmentId)
    transcribingIds.value = next
  }
}

const openCameraInput = () => {
  cameraInput.value?.click()
}

const openGalleryInput = () => {
  galleryInput.value?.click()
}

const handleImageInput = (event: Event) => {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  input.value = ''
  for (const file of files) {
    if (selectedImages.value.length >= 3) {
      showMessage('单条消息最多上传 3 张图片')
      break
    }
    if (!allowedImageTypes.has(file.type)) {
      showMessage('仅支持 JPG、PNG、WebP 图片')
      continue
    }
    if (file.size > maxImageSizeBytes) {
      showMessage('单张图片不能超过 10MB')
      continue
    }
    selectedImages.value.push({
      id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
      file,
      previewUrl: URL.createObjectURL(file)
    })
  }
}

const removeImage = (id: string) => {
  const image = selectedImages.value.find((item) => item.id === id)
  if (image) {
    URL.revokeObjectURL(image.previewUrl)
  }
  selectedImages.value = selectedImages.value.filter((item) => item.id !== id)
}

const clearImages = () => {
  selectedImages.value.forEach((image) => URL.revokeObjectURL(image.previewUrl))
  selectedImages.value = []
}

const uploadSelectedImages = async () => {
  const ids: number[] = []
  for (const image of selectedImages.value) {
    const contentType = image.file.type
    const presign = await helpOrderService.presignImage({
      filename: image.file.name || 'help-order-image.jpg',
      contentType,
      sizeBytes: image.file.size,
      businessType: 'HELP_ORDER_IMAGE'
    })
    await helpOrderService.uploadToPresignedUrl(presign.data.uploadUrl, image.file, contentType)
    const registered = await helpOrderService.registerImage({
      objectKey: presign.data.objectKey,
      originalFilename: image.file.name || presign.data.filename,
      contentType,
      sizeBytes: image.file.size,
      businessType: 'HELP_ORDER_IMAGE'
    })
    ids.push(registered.data.id)
  }
  return ids
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

const voiceStatusText = (status?: string | null) => {
  if (status === 'UNCONFIGURED') return '转文字暂不可用，可听原音'
  if (status === 'FAILED') return '转文字失败，可听原音'
  if (status === 'PENDING') return '尚未转文字'
  return '尚未转文字'
}

const canTranscribe = (attachment: HelpOrderAttachmentDTO) => {
  const status = attachment.voiceTranscript?.status
  return attachment.attachmentType === 'AUDIO' && status !== 'SUCCESS' && status !== 'UNCONFIGURED'
}

const transcriptActionText = (status?: string | null) => {
  return status === 'FAILED' ? '重试转文字' : '转文字'
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
  clearImages()
})
</script>

<style scoped>
.page-content {
  --background: var(--patient-bg-page);
}

.page-container {
  padding: var(--patient-space-4);
}

.summary-card,
.timeline-card,
.empty-card {
  border-radius: var(--patient-radius-lg);
  box-shadow: var(--patient-shadow-sm);
}

.summary-top {
  display: flex;
  justify-content: space-between;
  gap: var(--patient-space-3);
  align-items: flex-start;
}

.summary-title {
  font-size: var(--patient-font-title);
  font-weight: var(--patient-font-bold);
  color: var(--patient-text-primary);
  line-height: var(--patient-leading-tight);
}

.summary-meta,
.summary-label,
.empty-text,
.event-meta {
  color: var(--patient-text-secondary);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--patient-space-3);
  margin-top: var(--patient-space-4);
}

.summary-item {
  display: grid;
  gap: var(--patient-space-1);
  padding: var(--patient-space-3);
  border-radius: var(--patient-radius-md);
  background: var(--patient-bg-page);
  color: var(--patient-text-primary);
}

.summary-body {
  margin-top: var(--patient-space-4);
  color: var(--patient-text-primary);
  line-height: var(--patient-leading-relaxed);
  white-space: pre-wrap;
  font-size: var(--patient-font-body);
}

.summary-notice {
  margin-top: var(--patient-space-3);
  padding: var(--patient-space-3) var(--patient-space-4);
  border-radius: var(--patient-radius-md);
  background: var(--patient-primary-light);
  color: var(--patient-primary);
  line-height: var(--patient-leading-normal);
  font-size: var(--patient-font-helper);
}

.summary-notice--warning {
  background: rgba(245, 158, 11, 0.12);
  color: var(--patient-warning);
}

.summary-notice--danger {
  background: rgba(239, 68, 68, 0.10);
  color: var(--patient-danger);
}

.event-list {
  display: grid;
  gap: var(--patient-space-3);
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
  padding: var(--patient-space-4);
  border-radius: var(--patient-radius-lg);
  box-shadow: var(--patient-shadow-sm);
}

.event-bubble--incoming {
  background: var(--patient-bg-card);
  color: var(--patient-text-primary);
}

.event-bubble--outgoing {
  background: linear-gradient(135deg, var(--patient-primary), var(--patient-primary-dark));
  color: var(--patient-text-inverse);
}

.event-bubble--outgoing .event-meta,
.event-bubble--outgoing .event-content {
  color: rgba(255, 255, 255, 0.88);
}

.event-bubble--system {
  background: var(--patient-border);
  color: var(--patient-text-primary);
}

.event-title {
  font-weight: var(--patient-font-semibold);
  font-size: var(--patient-font-body);
}

.event-meta {
  display: flex;
  justify-content: space-between;
  gap: var(--patient-space-3);
  margin-top: var(--patient-space-2);
  font-size: var(--patient-font-caption);
}

.event-content {
  margin-top: var(--patient-space-2);
  line-height: var(--patient-leading-relaxed);
  white-space: pre-wrap;
  font-size: var(--patient-font-body);
}

.event-attachments {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 92px));
  gap: var(--patient-space-2);
  margin-top: var(--patient-space-3);
}

.event-attachment {
  display: block;
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: var(--patient-radius-md);
  background: var(--patient-bg-page);
}

.event-attachment img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.voice-attachment {
  grid-column: 1 / -1;
  display: grid;
  gap: var(--patient-space-2);
  min-width: min(280px, 100%);
  padding: var(--patient-space-3);
  border-radius: var(--patient-radius-md);
  background: rgba(255, 255, 255, 0.12);
}

.event-bubble--incoming .voice-attachment {
  background: var(--patient-bg-page);
}

.voice-player-row {
  display: flex;
  align-items: center;
  gap: var(--patient-space-2);
}

.voice-player-row audio {
  width: min(240px, 100%);
  height: 36px;
}

.voice-player-row span,
.voice-transcript {
  font-size: var(--patient-font-helper);
}

.voice-transcript {
  line-height: var(--patient-leading-normal);
  white-space: pre-wrap;
}

.voice-transcript--muted {
  opacity: 0.8;
}

.voice-transcribe {
  justify-self: start;
  border: 0;
  border-radius: var(--patient-radius-sm);
  padding: 6px 10px;
  background: rgba(15, 23, 42, 0.12);
  color: inherit;
  font-size: var(--patient-font-helper);
}

.voice-transcribe:disabled {
  opacity: 0.58;
}

.composer-toolbar {
  --padding-top: var(--patient-space-2);
  --padding-bottom: var(--patient-space-2);
  --padding-start: var(--patient-space-4);
  --padding-end: var(--patient-space-4);
}

.composer {
  display: flex;
  flex-direction: column;
  gap: var(--patient-space-2);
  padding: var(--patient-space-2) var(--patient-space-3);
}

.composer-main {
  display: flex;
  align-items: flex-end;
  gap: var(--patient-space-3);
  width: 100%;
}

.composer-input {
  flex: 1;
  --background: var(--patient-bg-page);
  --border-radius: var(--patient-radius-lg);
  border: var(--patient-border-default);
  border-radius: var(--patient-radius-lg);
  padding: var(--patient-space-1) var(--patient-space-2);
  font-size: var(--patient-font-body);
}

.composer-button {
  align-self: stretch;
}

.composer-images {
  display: grid;
  gap: var(--patient-space-2);
  width: 100%;
}

.privacy-warning {
  color: var(--patient-warning);
  font-size: var(--patient-font-helper);
  line-height: var(--patient-leading-normal);
}

.image-actions {
  display: flex;
  align-items: center;
  gap: var(--patient-space-2);
  flex-wrap: wrap;
}

.voice-hold-button {
  width: 100%;
  min-height: 42px;
  border: 0;
  border-radius: var(--patient-radius-lg);
  background: var(--patient-primary);
  color: var(--patient-text-inverse);
  font-size: var(--patient-font-body);
  font-weight: var(--patient-font-semibold);
  touch-action: none;
}

.voice-hold-button--recording {
  background: var(--patient-danger);
}

.voice-hold-button:disabled {
  opacity: 0.58;
}

.image-count {
  color: var(--patient-text-secondary);
  font-size: var(--patient-font-helper);
}

.file-input {
  display: none;
}

.selected-image-list {
  display: grid;
  grid-template-columns: repeat(3, 72px);
  gap: var(--patient-space-2);
}

.selected-image {
  position: relative;
  width: 72px;
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: var(--patient-radius-md);
  background: var(--patient-bg-page);
}

.selected-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.selected-image button {
  position: absolute;
  right: 4px;
  bottom: 4px;
  border: 0;
  border-radius: var(--patient-radius-sm);
  padding: 3px 5px;
  background: rgba(15, 23, 42, 0.72);
  color: #fff;
  font-size: 11px;
}

.terminal-banner {
  padding: var(--patient-space-3) 0;
  color: var(--patient-text-secondary);
  line-height: var(--patient-leading-normal);
  font-size: var(--patient-font-helper);
}

</style>
