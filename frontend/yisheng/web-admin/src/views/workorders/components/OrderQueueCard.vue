<template>
  <article
    :class="['queue-card', { 'queue-card--active': active }]"
    @click="$emit('select', order.id)"
  >
    <div class="queue-card__header">
      <strong class="queue-card__name">{{ order.patientName }}</strong>
      <el-tag :type="statusTagType(order.status)" size="small" effect="plain">
        {{ statusText(order.status, role) }}
      </el-tag>
    </div>
    <p class="queue-card__title">{{ order.title }}</p>
    <div class="queue-card__footer">
      <span class="queue-card__time">{{ relativeTime(order.createdAt) }}</span>
      <div class="queue-card__badges">
        <el-tag v-if="order.priority === 'URGENT'" type="danger" size="small" effect="plain">
          紧急
        </el-tag>
        <span v-if="hasNewReply" class="queue-card__dot" />
      </div>
    </div>
  </article>
</template>

<script setup>
import { statusTagType, statusText, relativeTime } from '../composables/useWorkOrders'

defineProps({
  order: { type: Object, required: true },
  active: { type: Boolean, default: false },
  hasNewReply: { type: Boolean, default: false },
  role: { type: String, default: 'pharmacist' }
})

defineEmits(['select'])
</script>

<style lang="scss" scoped>
.queue-card {
  display: grid;
  gap: 6px;
  padding: 12px 16px;
  border-left: 3px solid transparent;
  cursor: pointer;
  transition: background 160ms ease;

  &:hover {
    background: var(--pro-bg-secondary);
  }

  &--active {
    border-left-color: var(--pro-brand-primary);
    background: var(--pro-brand-secondary);
  }

  & + & {
    border-top: 1px solid var(--pro-border-color);
  }
}

.queue-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.queue-card__name {
  font-size: 14px;
  color: var(--pro-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.queue-card__title {
  margin: 0;
  font-size: 13px;
  color: var(--pro-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.queue-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.queue-card__time {
  font-size: 12px;
  color: var(--pro-text-secondary);
}

.queue-card__badges {
  display: flex;
  align-items: center;
  gap: 6px;
}

.queue-card__dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--pro-brand-primary);
}
</style>
