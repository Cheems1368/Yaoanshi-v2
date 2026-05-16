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

.timeline-empty {
  margin: 0;
  color: var(--pro-text-secondary);
  font-size: 14px;
}
</style>
