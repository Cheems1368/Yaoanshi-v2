<template>
  <section class="workstation">
    <header class="workstation__header">
      <div>
        <h2>升级工单</h2>
        <p>仅展示已经升级到医生队列的工单，医生可回复、关闭或驳回。</p>
      </div>
      <div class="workstation__toolbar">
        <el-button type="primary" @click="loadOrders">刷新</el-button>
      </div>
    </header>

    <div :class="['workstation__body', { 'workstation__body--detail-open': mobileShowDetail }]">
      <div v-show="!mobileShowDetail || !isMobile" class="workstation__queue">
        <OrderQueue
          :orders="orders"
          :selected-id="selectedId"
          :unread-ids="unreadIds"
          :loading="loading"
          :total="total"
          :current-page="query.pageNo"
          :page-size="query.pageSize"
          role="doctor"
          @select="handleSelect"
          @page-change="handlePageChange"
          @status-change="handleStatusChange"
        />
      </div>

      <div v-show="mobileShowDetail || !isMobile" class="workstation__detail">
        <OrderDetail
          :order-id="selectedId"
          :detail="detail"
          :loading="detailLoading"
          :draft="currentDraft"
          :show-back="isMobile"
          role="doctor"
          :on-reply="handleReply"
          :on-reject="handleReject"
          :on-close="handleClose"
          :on-save-draft="handleSaveDraft"
          @back="mobileShowDetail = false"
        />
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import {
  closeWorkOrder,
  getWorkOrderDetail,
  getWorkOrders,
  rejectWorkOrder,
  replyWorkOrder
} from '@/api/workorders'
import { useWorkOrders } from './composables/useWorkOrders'
import OrderQueue from './components/OrderQueue.vue'
import OrderDetail from './components/OrderDetail.vue'

const api = {
  getWorkOrders,
  getWorkOrderDetail,
  replyWorkOrder,
  closeWorkOrder,
  rejectWorkOrder
}

const {
  query, orders, total, loading,
  selectedId, detail, detailLoading,
  unreadIds,
  loadOrders, selectOrder,
  reply, reject, close,
  saveDraft, getDraft
} = useWorkOrders('doctor', api)

const mobileShowDetail = ref(false)

const isMobile = computed(() => {
  if (typeof window === 'undefined') return false
  return window.innerWidth < 1024
})

const currentDraft = computed(() => {
  if (!selectedId.value) return null
  return getDraft(selectedId.value)
})

function handleSelect(id) {
  selectOrder(id)
  if (isMobile.value) {
    mobileShowDetail.value = true
  }
}

function handlePageChange(page) {
  query.pageNo = page
  loadOrders()
}

function handleStatusChange(statusValue) {
  query.status = statusValue
  query.pageNo = 1
  loadOrders()
}

function handleReply(id, content) {
  return reply(id, content)
}

function handleReject(id, reason) {
  return reject(id, reason)
}

function handleClose(id, note) {
  return close(id, note)
}

function handleSaveDraft(id, draft) {
  saveDraft(id, draft.action, draft.text)
}
</script>

<style lang="scss" scoped>
.workstation {
  display: grid;
  grid-template-rows: auto 1fr;
  gap: 0;
  height: calc(100vh - 72px - 48px);
  min-height: 0;
}

.workstation__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding: 0 0 16px;

  h2 {
    margin: 0;
    color: var(--pro-text-primary);
  }

  p {
    margin: 8px 0 0;
    color: var(--pro-text-secondary);
  }
}

.workstation__toolbar {
  display: flex;
  gap: 12px;
}

.workstation__body {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 0;
  min-height: 0;
  overflow: hidden;
  border: 1px solid var(--pro-border-color);
  border-radius: var(--pro-radius-lg);
  background: var(--pro-bg-primary);
  box-shadow: var(--pro-shadow-sm);
}

.workstation__queue {
  min-height: 0;
  border-right: 1px solid var(--pro-border-color);
}

.workstation__detail {
  min-height: 0;
  overflow-y: auto;
  padding: 24px;
}

@media (max-width: 1023px) {
  .workstation {
    height: calc(100vh - 72px - 24px);
  }

  .workstation__body {
    grid-template-columns: 1fr;
  }

  .workstation__queue {
    border-right: none;
  }
}
</style>
