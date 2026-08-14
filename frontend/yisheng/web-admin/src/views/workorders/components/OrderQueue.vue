<template>
  <div class="order-queue">
    <div class="order-queue__filters">
      <el-input
        v-model="keyword"
        placeholder="搜索患者 / 工单号"
        clearable
        :prefix-icon="Search"
        class="order-queue__search"
      />
      <div class="order-queue__tabs">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          :class="['order-queue__tab', { 'order-queue__tab--active': activeTab === tab.value }]"
          @click="handleTabChange(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div v-loading="loading" class="order-queue__list">
      <template v-if="filteredOrders.length">
        <OrderQueueCard
          v-for="item in filteredOrders"
          :key="item.id"
          :order="item"
          :active="item.id === selectedId"
          :has-new-reply="unreadIds.has(item.id)"
          :role="role"
          @select="$emit('select', $event)"
        />
      </template>
      <div v-else class="order-queue__empty">暂无工单</div>
    </div>

    <div class="order-queue__pager">
      <el-pagination
        small
        background
        layout="prev, pager, next"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        @current-change="$emit('page-change', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import OrderQueueCard from './OrderQueueCard.vue'

const props = defineProps({
  orders: { type: Array, required: true },
  selectedId: { type: [Number, String], default: null },
  unreadIds: { type: Set, default: () => new Set() },
  loading: { type: Boolean, default: false },
  total: { type: Number, default: 0 },
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 20 },
  role: { type: String, default: 'pharmacist' }
})

const emit = defineEmits(['select', 'page-change', 'status-change'])

const keyword = ref('')
const activeTab = ref('')

const tabs = computed(() => {
  if (props.role === 'doctor') {
    return [
      { label: '全部', value: '' },
      { label: '待处理', value: 'WAITING_DOCTOR' },
      { label: '处理中', value: 'DOCTOR_IN_PROGRESS' },
      { label: '已完成', value: 'CLOSED,REJECTED' }
    ]
  }
  return [
    { label: '全部', value: '' },
    { label: '待处理', value: 'SUBMITTED' },
    { label: '处理中', value: 'PHARMACIST_IN_PROGRESS' },
    { label: '已完成', value: 'CLOSED,REJECTED' }
  ]
})

const filteredOrders = computed(() => {
  if (!keyword.value.trim()) return props.orders
  const kw = keyword.value.trim().toLowerCase()
  return props.orders.filter(
    (order) =>
      (order.patientName && order.patientName.toLowerCase().includes(kw)) ||
      (order.orderNo && order.orderNo.toLowerCase().includes(kw))
  )
})

function handleTabChange(value) {
  activeTab.value = value
  emit('status-change', value)
}
</script>

<style lang="scss" scoped>
.order-queue {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.order-queue__filters {
  flex: 0 0 auto;
  padding: 16px 16px 12px;
  display: grid;
  gap: 12px;
  border-bottom: 1px solid var(--pro-border-color);
}

.order-queue__search {
  width: 100%;
}

.order-queue__tabs {
  display: flex;
  gap: 4px;
}

.order-queue__tab {
  flex: 1;
  padding: 6px 0;
  border: none;
  border-radius: var(--pro-radius-sm);
  background: transparent;
  color: var(--pro-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all 160ms ease;

  &:hover {
    background: var(--pro-bg-secondary);
  }

  &--active {
    background: var(--pro-brand-secondary);
    color: var(--pro-brand-primary);
    font-weight: 600;
  }
}

.order-queue__list {
  flex: 1 1 auto;
  overflow-y: auto;
  min-height: 0;
}

.order-queue__empty {
  padding: 48px 16px;
  text-align: center;
  color: var(--pro-text-secondary);
  font-size: 14px;
}

.order-queue__pager {
  flex: 0 0 auto;
  padding: 12px 16px;
  border-top: 1px solid var(--pro-border-color);
  display: flex;
  justify-content: center;
}
</style>
