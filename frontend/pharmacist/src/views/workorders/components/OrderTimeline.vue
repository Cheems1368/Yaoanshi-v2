<template>
  <section class="order-timeline">
    <h3>处理时间线</h3>
    <el-timeline v-if="events.length">
      <el-timeline-item
        v-for="event in events"
        :key="event.id"
        :timestamp="formatDate(event.eventAt)"
        :color="eventColor(event.eventType)"
        placement="top"
      >
        <strong>{{ eventText(event.eventType) }}</strong>
        <p class="timeline-operator">{{ event.operatorName || event.operatorType }}</p>
        <p v-if="event.content" class="timeline-content">{{ event.content }}</p>
        <div v-if="event.attachments?.length" class="timeline-attachments">
          <template v-for="attachment in event.attachments" :key="attachment.fileAssetId">
            <a
              v-if="attachment.attachmentType !== 'AUDIO'"
              class="timeline-image"
              :href="attachment.downloadUrl"
              target="_blank"
              rel="noreferrer"
            >
              <img :src="attachment.downloadUrl" :alt="attachment.originalFilename" />
            </a>
            <div v-else class="timeline-audio">
              <div class="timeline-audio__player">
                <audio :src="attachment.downloadUrl" controls preload="metadata" />
                <span>{{ attachment.durationSeconds || 1 }}″</span>
              </div>
              <p v-if="attachment.voiceTranscript?.text" class="timeline-transcript">
                转写：{{ attachment.voiceTranscript.text }}
              </p>
              <p v-else class="timeline-transcript timeline-transcript--muted">
                {{ voiceStatusText(attachment.voiceTranscript?.status) }}
              </p>
              <el-button
                v-if="attachment.id && canTranscribe(attachment)"
                size="small"
                plain
                :loading="transcribingId === attachment.id"
                @click="handleTranscribe(attachment.id)"
              >
                {{ transcriptActionText(attachment.voiceTranscript?.status) }}
              </el-button>
            </div>
          </template>
        </div>
      </el-timeline-item>
    </el-timeline>
    <p v-else class="timeline-empty">暂无处理记录</p>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { eventText, formatDate } from '../composables/useWorkOrders'
import { transcribeWorkOrderAttachment } from '@/modules/pharmacist/api/workorders'

const props = defineProps({
  orderId: { type: [Number, String], default: null },
  events: { type: Array, default: () => [] },
  canTriggerTranscribe: { type: Boolean, default: true }
})
const emit = defineEmits(['refresh'])
const transcribingId = ref(null)

function eventColor(eventType) {
  const map = {
    SUBMITTED: 'var(--pro-brand-primary)',
    PATIENT_REPLY: 'var(--pro-brand-primary)',
    ASSIGNED: 'var(--pro-brand-primary)',
    PHARMACIST_REPLY: 'var(--pro-success)',
    DOCTOR_REPLY: 'var(--pro-success)',
    ESCALATED_TO_DOCTOR: 'var(--pro-warning)',
    REJECTED: 'var(--pro-danger)',
    CLOSED: '#909399'
  }
  return map[eventType] || 'var(--pro-brand-primary)'
}

function voiceStatusText(status) {
  if (status === 'UNCONFIGURED') return '转文字暂不可用，可听原音'
  if (status === 'FAILED') return '转文字失败，可听原音'
  return '尚未转文字'
}

function canTranscribe(attachment) {
  const status = attachment.voiceTranscript?.status
  return props.canTriggerTranscribe && attachment.attachmentType === 'AUDIO' && status !== 'SUCCESS' && status !== 'UNCONFIGURED'
}

function transcriptActionText(status) {
  return status === 'FAILED' ? '重试转文字' : '转文字'
}

async function handleTranscribe(attachmentId) {
  if (!props.orderId || transcribingId.value) return
  transcribingId.value = attachmentId
  try {
    await transcribeWorkOrderAttachment(props.orderId, attachmentId)
    ElMessage.success('转写已完成')
    emit('refresh')
  } catch (error) {
    ElMessage.error(error?.message || '转文字失败，可听原音')
  } finally {
    transcribingId.value = null
  }
}
</script>

<style lang="scss" scoped>
.order-timeline {
  display: grid;
  gap: 16px;
}

.order-timeline h3 {
  margin: 0;
  font-size: 16px;
  color: var(--pro-text-primary);
}

.timeline-operator {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--pro-text-secondary);
}

.timeline-content {
  margin: 4px 0 0;
  padding: 8px 12px;
  border-radius: var(--pro-radius-sm);
  background: var(--pro-bg-secondary);
  color: var(--pro-text-primary);
  white-space: pre-wrap;
  font-size: 13px;
  line-height: 1.5;
}

.timeline-attachments {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 96px));
  gap: 8px;
  margin-top: 8px;
}

.timeline-image {
  display: block;
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: var(--pro-radius-sm);
  border: 1px solid var(--pro-border-color);
  background: var(--pro-bg-secondary);
}

.timeline-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.timeline-audio {
  grid-column: 1 / -1;
  display: grid;
  gap: 8px;
  padding: 10px 12px;
  border-radius: var(--pro-radius-sm);
  background: var(--pro-bg-secondary);
}

.timeline-audio__player {
  display: flex;
  align-items: center;
  gap: 8px;
}

.timeline-audio__player audio {
  width: min(280px, 100%);
  height: 36px;
}

.timeline-transcript {
  margin: 0;
  color: var(--pro-text-primary);
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.timeline-transcript--muted {
  color: var(--pro-text-secondary);
}

.timeline-empty {
  margin: 0;
  color: var(--pro-text-secondary);
  font-size: 14px;
}
</style>
