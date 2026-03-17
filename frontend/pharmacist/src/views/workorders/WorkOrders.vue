<template>
  <section class="workstation">
    <!-- Page header -->
    <header class="workstation__header pro-page-header">
      <div class="pro-page-header__copy">
        <h2>工单处理</h2>
        <p>承接患者求助、认领处理、必要时升级到医生，并完成关闭或驳回。</p>
      </div>
      <div class="pro-toolbar">
        <el-button type="primary" @click="loadOrders">刷新</el-button>
      </div>
    </header>

    <!-- Dual-column body -->
    <div :class="['workstation__body', { 'workstation__body--detail-open': mobileShowDetail }]">
      <!-- Left: queue -->
      <div v-show="!mobileShowDetail || !isMobile" class="workstation__queue">
        <OrderQueue
          :orders="orders"
          :selected-id="selectedId"
          :unread-ids="unreadIds"
          :loading="loading"
          :total="total"
          :current-page="query.pageNo"
          :page-size="query.pageSize"
          role="pharmacist"
          @select="handleSelect"
          @page-change="handlePageChange"
          @status-change="handleStatusChange"
        />
      </div>

      <!-- Right: detail -->
      <div v-show="mobileShowDetail || !isMobile" class="workstation__detail">
        <OrderDetail
          :order-id="selectedId"
          :detail="detail"
          :loading="detailLoading"
          :draft="currentDraft"
          :show-back="isMobile"
          role="pharmacist"
          @back="mobileShowDetail = false"
          @claim="claim"
          @reply="handleReply"
          @escalate="handleEscalate"
          @reject="handleReject"
          @close="handleClose"
          @save-draft="handleSaveDraft"
        />
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import {
  claimWorkOrder,
  closeWorkOrder,
  escalateWorkOrder,
  getWorkOrderDetail,
  getWorkOrders,
  rejectWorkOrder,
  replyWorkOrder
} from '@/modules/pharmacist/api/workorders'
import { useWorkOrders } from './composables/useWorkOrders'
import OrderQueue from './components/OrderQueue.vue'
import OrderDetail from './components/OrderDetail.vue'

const api = {
  getWorkOrders,
  getWorkOrderDetail,
  claimWorkOrder,
  replyWorkOrder,
  escalateWorkOrder,
  closeWorkOrder,
  rejectWorkOrder
}

const {
  query, orders, total, loading,
  selectedId, detail, detailLoading,
  unreadIds,
  loadOrders, selectOrder,
  claim, reply, escalate, reject, close,
  saveDraft, getDraft, clearDraft
} = useWorkOrders('pharmacist', api)

// Mobile single-column mode
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
  reply(id, content)
}

function handleEscalate(id, note) {
  escalate(id, note)
}

function handleReject(id, reason) {
  reject(id, reason)
}

function handleClose(id, note) {
  close(id, note)
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
  padding: 0 0 16px;
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
  border-right: 1px solid var(--pro-border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.workstation__detail {
  background: var(--pro-bg-secondary);
  overflow-y: auto;
  padding: 24px;
}

@media (max-width: 1279px) and (min-width: 1024px) {
  .workstation__body {
    grid-template-columns: 300px 1fr;
  }
}

@media (max-width: 1023px) {
  .workstation {
    height: auto;
    min-height: calc(100vh - 72px - 48px);
  }

  .workstation__body {
    grid-template-columns: 1fr;
  }

  .workstation__queue {
    border-right: none;
    min-height: 400px;
  }

  .workstation__detail {
    min-height: 400px;
  }
}
</style>
