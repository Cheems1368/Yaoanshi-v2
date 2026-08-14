<template>
  <div class="order-detail">
    <div v-if="!orderId" class="order-detail__empty">
      <el-icon :size="48" color="var(--pro-text-secondary)"><Document /></el-icon>
      <p>请从左侧选择工单</p>
    </div>

    <el-skeleton v-else-if="loading" :rows="10" animated class="order-detail__skeleton" />

    <template v-else-if="detail">
      <el-button
        v-if="showBack"
        text
        class="order-detail__back"
        @click="$emit('back')"
      >
        &larr; 返回列表
      </el-button>

      <OrderSummary :detail="detail" :role="role" />

      <SelfReportedMedicationsPanel
        v-if="detail.category === 'NEW_MEDICATION_REVIEW'"
        :order-id="detail.id"
        :patient-id="detail.patientId"
        @refresh="$emit('refresh', detail.id)"
      />

      <OrderTimeline
        :order-id="detail.id"
        :events="detail.events || []"
        :can-trigger-transcribe="role === 'pharmacist'"
        @refresh="$emit('refresh', detail.id)"
      />

      <OrderActionBar
        :detail="detail"
        :role="role"
        :draft="draft"
        @claim="$emit('claim', detail.id)"
        @reply="$emit('reply', detail.id, $event)"
        @escalate="$emit('escalate', detail.id, $event)"
        @reject="$emit('reject', detail.id, $event)"
        @close="$emit('close', detail.id, $event)"
        @save-draft="$emit('save-draft', detail.id, $event)"
      />
    </template>
  </div>
</template>

<script setup>
import { Document } from '@element-plus/icons-vue'
import OrderSummary from './OrderSummary.vue'
import OrderTimeline from './OrderTimeline.vue'
import OrderActionBar from './OrderActionBar.vue'
import SelfReportedMedicationsPanel from './SelfReportedMedicationsPanel.vue'

defineProps({
  orderId: { type: [Number, String], default: null },
  detail: { type: Object, default: null },
  loading: { type: Boolean, default: false },
  role: { type: String, default: 'pharmacist' },
  draft: { type: Object, default: null },
  showBack: { type: Boolean, default: false }
})

defineEmits(['back', 'claim', 'reply', 'escalate', 'reject', 'close', 'save-draft', 'refresh'])
</script>

<style lang="scss" scoped>
.order-detail {
  display: grid;
  gap: 24px;
  align-content: start;
}

.order-detail__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 80px 24px;

  p {
    margin: 0;
    color: var(--pro-text-secondary);
    font-size: 14px;
  }
}

.order-detail__skeleton {
  padding: 0;
}

.order-detail__back {
  justify-self: start;
  font-size: 14px;
}
</style>
