<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h2>工单处理</h2>
        <p>承接患者求助、认领处理、必要时升级到医生，并完成关闭或驳回。</p>
      </div>
      <div class="query-bar">
        <el-select v-model="query.status" clearable placeholder="全部状态" class="query-select">
          <el-option label="待药师处理" value="SUBMITTED" />
          <el-option label="药师处理中" value="PHARMACIST_IN_PROGRESS" />
          <el-option label="待医生处理" value="WAITING_DOCTOR" />
          <el-option label="医生处理中" value="DOCTOR_IN_PROGRESS" />
          <el-option label="已关闭" value="CLOSED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-button type="primary" @click="loadOrders">刷新</el-button>
      </div>
    </header>

    <section class="panel">
      <el-table v-loading="loading" :data="orders" border empty-text="暂无工单">
        <el-table-column prop="orderNo" label="工单号" width="180" />
        <el-table-column prop="patientName" label="患者" min-width="120" />
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="category" label="类型" width="140">
          <template #default="{ row }">{{ categoryText(row.category) }}</template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="110">
          <template #default="{ row }">
            <el-tag :type="row.priority === 'URGENT' ? 'danger' : 'info'" effect="plain">
              {{ row.priority === 'URGENT' ? '紧急' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="150">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="plain">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" min-width="180">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
            <el-button v-if="row.status === 'SUBMITTED'" link type="success" @click="handleClaim(row.id)">认领</el-button>
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
              <span>{{ categoryText(detail.category) }}</span>
            </article>
            <article class="summary-card">
              <strong>{{ statusText(detail.status) }}</strong>
              <span>当前处理：{{ detail.currentHandlerName || '待分配' }}</span>
              <span>医生：{{ detail.assignedDoctorName || '未升级' }}</span>
            </article>
          </div>
          <p class="detail-desc">{{ detail.description }}</p>
          <p v-if="detail.serviceNotice" class="detail-note">服务说明：{{ detail.serviceNotice }}</p>
          <p v-if="detail.rejectReason" class="detail-note">驳回原因：{{ detail.rejectReason }}</p>
        </section>

        <section class="detail-section">
          <div class="action-row">
            <el-button v-if="detail.status === 'SUBMITTED'" type="success" @click="handleClaim(detail.id)">认领</el-button>
            <el-button type="primary" @click="handleReply(detail.id)">回复</el-button>
            <el-button type="warning" plain @click="handleEscalate(detail.id)">升级医生</el-button>
            <el-button type="danger" plain @click="handleReject(detail.id)">驳回</el-button>
            <el-button type="success" plain @click="handleClose(detail.id)">关闭</el-button>
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
  claimWorkOrder,
  closeWorkOrder,
  escalateWorkOrder,
  getWorkOrderDetail,
  getWorkOrders,
  rejectWorkOrder,
  replyWorkOrder
} from '@/modules/pharmacist/api/workorders'
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

const handleClaim = async (id) => {
  await claimWorkOrder(id)
  ElMessage.success('工单已认领')
  await refreshAfterAction(id)
}

const handleReply = async (id) => {
  const { value } = await ElMessageBox.prompt('请输入处理回复', '回复工单', {
    confirmButtonText: '提交',
    cancelButtonText: '取消',
    inputPattern: /\S+/,
    inputErrorMessage: '回复内容不能为空'
  })
  await replyWorkOrder(id, { content: value })
  ElMessage.success('回复已提交')
  await refreshAfterAction(id)
}

const handleEscalate = async (id) => {
  const { value } = await ElMessageBox.prompt('请输入升级说明（可选）', '升级给医生', {
    confirmButtonText: '升级',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：存在不良反应，需要医生进一步判断'
  })
  await escalateWorkOrder(id, { note: value || '' })
  ElMessage.success('已升级到医生队列')
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
    inputPlaceholder: '例如：已完成指导，提醒患者填写满意度'
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
    SUBMITTED: '待药师处理',
    PHARMACIST_IN_PROGRESS: '药师处理中',
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
  if (status === 'WAITING_DOCTOR' || status === 'DOCTOR_IN_PROGRESS') return 'warning'
  return 'primary'
}

const categoryText = (category) => {
  const map = {
    MEDICATION_CONSULTATION: '用药咨询',
    MISSED_OR_WRONG_DOSE: '漏服 / 错服',
    ADVERSE_REACTION: '不良反应',
    PLAN_CHANGE: '计划调整',
    OTHER: '其他'
  }
  return map[category] || category
}

const eventText = (eventType) => {
  const map = {
    SUBMITTED: '患者提交',
    ASSIGNED: '药师认领',
    PHARMACIST_REPLY: '药师回复',
    ESCALATED_TO_DOCTOR: '升级医生',
    DOCTOR_REPLY: '医生回复',
    REJECTED: '工单驳回',
    CLOSED: '工单关闭'
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
.detail-desc,
.detail-note {
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
