<template>
  <section class="order-summary">
    <div class="summary-grid">
      <article class="summary-card">
        <strong>{{ detail.patientName }}</strong>
        <span>{{ detail.orderNo }}</span>
        <span>{{ categoryText(detail.category) }}</span>
      </article>
      <article class="summary-card">
        <div class="summary-card__status">
          <el-tag :type="statusTagType(detail.status)" effect="plain">
            {{ statusText(detail.status, role) }}
          </el-tag>
          <el-tag v-if="detail.priority === 'URGENT'" type="danger" size="small" effect="plain">
            紧急
          </el-tag>
        </div>
        <span>当前处理：{{ detail.currentHandlerName || '待分配' }}</span>
        <span>医生：{{ detail.assignedDoctorName || '未指定' }}</span>
      </article>
    </div>

    <p class="summary-desc">{{ detail.description }}</p>
    <p v-if="detail.serviceNotice" class="summary-note">服务说明：{{ detail.serviceNotice }}</p>
    <p v-if="detail.rejectReason" class="summary-note summary-note--danger">驳回原因：{{ detail.rejectReason }}</p>
  </section>
</template>

<script setup>
import { statusTagType, statusText, categoryText } from '../composables/useWorkOrders'

defineProps({
  detail: { type: Object, required: true },
  role: { type: String, default: 'pharmacist' }
})
</script>

<style lang="scss" scoped>
.order-summary {
  display: grid;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.summary-card {
  display: grid;
  gap: 6px;
  padding: 16px;
  border: 1px solid var(--pro-border-color);
  border-radius: var(--pro-radius-lg);
  background: var(--pro-bg-primary);
}

.summary-card strong {
  font-size: 16px;
  color: var(--pro-text-primary);
}

.summary-card span {
  font-size: 13px;
  color: var(--pro-text-secondary);
}

.summary-card__status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-desc {
  margin: 0;
  color: var(--pro-text-primary);
  white-space: pre-wrap;
  line-height: 1.6;
}

.summary-note {
  margin: 0;
  padding: 12px 16px;
  border-radius: var(--pro-radius-sm);
  background: var(--pro-bg-secondary);
  color: var(--pro-text-secondary);
  font-size: 13px;
}

.summary-note--danger {
  background: #fef0f0;
  color: var(--pro-danger);
}

@media (max-width: 768px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
