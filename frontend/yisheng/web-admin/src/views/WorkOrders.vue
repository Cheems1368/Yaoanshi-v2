<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h2>升级工单</h2>
        <p>仅展示已升级到医生队列的工单，医生可回复、关闭或驳回。</p>
      </div>
      <div class="query-bar">
        <el-select v-model="query.status" clearable placeholder="全部状态" class="query-select">
          <el-option label="待医生处理" value="WAITING_DOCTOR" />
          <el-option label="医生处理中" value="DOCTOR_IN_PROGRESS" />
          <el-option label="已关闭" value="CLOSED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-button type="primary" @click="loadOrders">刷新</el-button>
      </div>
    </header>

    <section class="panel">
      <el-table v-loading="loading" :data="orders" border empty-text="暂无升级工单">
        <el-table-column prop="orderNo" label="工单号" width="180" />
        <el-table-column prop="patientName" label="患者" min-width="120" />
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="status" label="状态" width="150">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="plain">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="升级时间" min-width="180">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </section>

    <el-drawer v-model="detailVisible" size="760px" :title="detail?.title || '工单详情'">
      <el-skeleton v-if="detailLoading" :rows="8" animated />

      <template v-else-if="detail">
        <section class="detail-section">
          <div class="summary-grid">
            <article class="summary-card">
              <strong>{{ detail.patientName }}</strong>
              <span>{{ detail.orderNo }}</span>
              <span>药师：{{ detail.currentHandlerName || '待接手' }}</span>
            </article>
            <article class="summary-card">
              <strong>{{ statusText(detail.status) }}</strong>
              <span>指派医生：{{ detail.assignedDoctorName || '当前账号接手' }}</span>
              <span v-if="detail.firstRespondedAt">首次响应：{{ formatDate(detail.firstRespondedAt) }}</span>
            </article>
          </div>
          <p class="detail-desc">{{ detail.description }}</p>
        </section>

        <section class="detail-section">
          <div class="action-row">
            <el-button type="primary" @click="handleReply(detail.id)">回复</el-button>
            <el-button type="success" plain @click="handleClose(detail.id)">关闭</el-button>
            <el-button type="danger" plain @click="handleReject(detail.id)">驳回</el-button>
          </div>
        </section>

        <section class="detail-section">
          <h3>处理时间线</h3>
          <el-timeline>
            <el-timeline-item
              v-for="event in detail.events || []"
              :key="event.id"
              :timestamp="formatDate(event.eventAt)"
              placement="top"
            >
              <strong>{{ eventText(event.eventType) }}</strong>
              <p>{{ event.operatorName || event.operatorType }}</p>
              <p v-if="event.content">{{ event.content }}</p>
            </el-timeline-item>
          </el-timeline>
        </section>
      </template>
    </el-drawer>
  </section>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  closeWorkOrder,
  getWorkOrderDetail,
  getWorkOrders,
  rejectWorkOrder,
  replyWorkOrder
} from '@/api/workorders'
import { ADMIN_NOTIFICATION_EVENT } from '@/utils/adminNotifications'

const loading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const total = ref(0)
const orders = ref([])
const detail = ref(null)

const query = reactive({
  status: '',
  pageNo: 1,
  pageSize: 10
})

const loadOrders = async () => {
  loading.value = true
  try {
    const response = await getWorkOrders({ ...query })
    orders.value = response.data?.list || []
    total.value = response.data?.total || 0
  } catch (error) {
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const openDetail = async (id) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const response = await getWorkOrderDetail(id)
    detail.value = response.data
  } finally {
    detailLoading.value = false
  }
}

const refreshAfterAction = async (id) => {
  await loadOrders()
  if (detailVisible.value && id) {
    await openDetail(id)
  }
}

const handleReply = async (id) => {
  const { value } = await ElMessageBox.prompt('请输入医生处理意见', '回复工单', {
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '回复内容不能为空'
  })
  await replyWorkOrder(id, { content: value })
  ElMessage.success('回复已提交')
  await refreshAfterAction(id)
}

const handleReject = async (id) => {
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回工单', {
    confirmButtonText: '驳回',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '驳回原因不能为空'
  })
  await rejectWorkOrder(id, { reason: value })
  ElMessage.success('工单已驳回')
  await refreshAfterAction(id)
}

const handleClose = async (id) => {
  const { value } = await ElMessageBox.prompt('请输入关闭说明（可选）', '关闭工单', {
    confirmButtonText: '关闭',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：已给出临床建议'
  })
  await closeWorkOrder(id, { note: value || '' })
  ElMessage.success('工单已关闭')
  await refreshAfterAction(id)
}

const handlePageChange = (page) => {
  query.pageNo = page
  loadOrders()
}

const statusText = (status) => {
  const map = {
    WAITING_DOCTOR: '待医生处理',
    DOCTOR_IN_PROGRESS: '医生处理中',
    CLOSED: '已关闭',
    REJECTED: '已驳回'
  }
  return map[status] || status
}

const statusTagType = (status) => {
  if (status === 'CLOSED') return 'success'
  if (status === 'REJECTED') return 'danger'
  return 'warning'
}

const eventText = (eventType) => {
  const map = {
    ESCALATED_TO_DOCTOR: '药师升级',
    DOCTOR_REPLY: '医生回复',
    CLOSED: '工单关闭',
    REJECTED: '工单驳回'
  }
  return map[eventType] || eventType
}

const formatDate = (value) => {
  if (!value) return '--'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

const handleAdminNotification = async (event) => {
  if (!event?.detail?.businessType?.startsWith('HELP_ORDER_')) {
    return
  }
  await loadOrders()
  if (detailVisible.value && detail.value?.id) {
    await openDetail(detail.value.id)
  }
}

onMounted(() => {
  loadOrders()
  window.addEventListener(ADMIN_NOTIFICATION_EVENT, handleAdminNotification)
})

onBeforeUnmount(() => {
  window.removeEventListener(ADMIN_NOTIFICATION_EVENT, handleAdminNotification)
})
</script>

<style lang="scss" scoped>
.page-shell,
.panel {
  display: grid;
  gap: 24px;
}

.page-header,
.query-bar,
.action-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.page-header h2,
.detail-section h3 {
  margin: 0;
  color: var(--pro-text-primary);
}

.page-header p,
.detail-desc {
  margin: 8px 0 0;
  color: var(--pro-text-secondary);
}

.query-select {
  width: 180px;
}

.pagination-row {
  display: flex;
  justify-content: flex-end;
}

.detail-section {
  display: grid;
  gap: 16px;
  margin-bottom: 20px;
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
}

.detail-desc {
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .page-header,
  .query-bar,
  .action-row {
    flex-direction: column;
    align-items: stretch;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
