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
        <el-tag v-if="event.decisionResult" class="decision-tag" size="small" type="success" effect="plain">
          {{ decisionText(event.decisionResult) }}
        </el-tag>
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
            </div>
          </template>
        </div>
      </el-timeline-item>
    </el-timeline>
    <p v-else class="timeline-empty">暂无处理记录</p>
  </section>
</template>

<script setup>
import { decisionText, eventText, formatDate } from '../composables/useWorkOrders'

defineProps({
  events: { type: Array, default: () => [] }
})

function eventColor(eventType) {
  const map = {
    SUBMITTED: 'var(--pro-brand-primary)',
    PATIENT_REPLY: 'var(--pro-brand-primary)',
    ASSIGNED: 'var(--pro-brand-primary)',
    PHARMACIST_REPLY: 'var(--pro-success)',
    DOCTOR_REPLY: 'var(--pro-success)',
    DOCTOR_DECISION: 'var(--pro-success)',
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

.decision-tag {
  margin-left: 8px;
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
