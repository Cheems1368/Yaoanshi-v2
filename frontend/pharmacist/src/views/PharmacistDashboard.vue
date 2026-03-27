<template>
  <section class="dashboard-page">
    <header class="page-header">
      <div>
        <h2>工作台总览</h2>
        <p>聚焦待处理工单、待审核处方和需跟进患者，支持通知到达后自动刷新。</p>
      </div>
      <el-button type="primary" :loading="loading" @click="loadDashboard">刷新概览</el-button>
    </header>

    <el-alert v-if="errorMessage" type="error" :closable="false" :title="errorMessage" show-icon />

    <div class="summary-grid">
      <article v-for="item in summaryCards" :key="item.label" class="summary-card">
        <span class="label">{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <span class="hint">{{ item.hint }}</span>
      </article>
    </div>

    <div class="content-grid">
      <section class="panel">
        <div class="panel-header">
          <h3>待处理工单</h3>
          <span>按提交时间展示最近 3 条</span>
        </div>
        <div v-if="recentOrders.length" class="panel-list">
          <article v-for="order in recentOrders" :key="order.id" class="list-item">
            <div>
              <strong>{{ order.title }}</strong>
              <p>{{ order.patientName }} / {{ statusText(order.status) }}</p>
            </div>
            <el-tag :type="statusTagType(order.status)" effect="plain">{{ statusText(order.status) }}</el-tag>
          </article>
        </div>
        <el-empty v-else description="当前没有待处理工单" />
      </section>

      <section class="panel">
        <div class="panel-header">
          <h3>待审核处方</h3>
          <span>展示最近 3 条上传记录</span>
        </div>
        <div v-if="recentPrescriptions.length" class="panel-list">
          <article v-for="item in recentPrescriptions" :key="item.id" class="list-item">
            <div>
              <strong>#{{ item.id }} / {{ item.patientName }}</strong>
              <p>{{ item.hospitalPatientId || '未绑定院内 ID' }}</p>
            </div>
            <el-tag :type="item.status === 'UPLOADED' ? 'warning' : 'success'" effect="plain">
              {{ prescriptionStatusText(item.status) }}
            </el-tag>
          </article>
        </div>
        <el-empty v-else description="当前没有待审核处方" />
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

let dashboardDebounceTimer = null
const debouncedLoadDashboard = () => {
  clearTimeout(dashboardDebounceTimer)
  dashboardDebounceTimer = setTimeout(loadDashboard, 300)
}
import { getPatients } from '@/modules/pharmacist/api/patients'
import { getPrescriptions } from '@/modules/pharmacist/api/prescriptions'
import { getWorkOrders } from '@/modules/pharmacist/api/workorders'
import { ADMIN_NOTIFICATION_EVENT } from '@/utils/adminNotifications'

const loading = ref(false)
const errorMessage = ref('')
const workOrderTotal = ref(0)
const prescriptionTotal = ref(0)
const patientTotal = ref(0)
const recentOrders = ref([])
const recentPrescriptions = ref([])

const summaryCards = computed(() => [
  { label: '待处理工单', value: workOrderTotal.value, hint: '覆盖药师处理与待升级工单' },
  { label: '待审核处方', value: prescriptionTotal.value, hint: '优先关注待复核处方' },
  { label: '在管患者', value: patientTotal.value, hint: '用于随访和计划维护的患者范围' }
])

const loadDashboard = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const [ordersResponse, prescriptionsResponse, patientsResponse] = await Promise.all([
      getWorkOrders({ pageNo: 1, pageSize: 3 }),
      getPrescriptions({ status: 'UPLOADED', pageNo: 1, pageSize: 3 }),
      getPatients({ pageNo: 1, pageSize: 1 })
    ])
    workOrderTotal.value = ordersResponse.data?.total || 0
    prescriptionTotal.value = prescriptionsResponse.data?.total || 0
    patientTotal.value = patientsResponse.data?.total || 0
    recentOrders.value = ordersResponse.data?.list || []
    recentPrescriptions.value = prescriptionsResponse.data?.list || []
  } catch (error) {
    errorMessage.value = error.message || '工作台摘要加载失败，请稍后重试'
    workOrderTotal.value = 0
    prescriptionTotal.value = 0
    patientTotal.value = 0
    recentOrders.value = []
    recentPrescriptions.value = []
  } finally {
    loading.value = false
  }
}

const handleAdminNotification = (event) => {
  if (!event?.detail?.businessType?.startsWith('HELP_ORDER_')) {
    return
  }
  debouncedLoadDashboard()
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
  return map[status] || status || '--'
}

const prescriptionStatusText = (status) => {
  if (status === 'UPLOADED') return '待复核'
  if (status === 'PHARMACIST_CONFIRMED') return '已复核'
  if (status === 'REJECTED') return '已驳回'
  return status || '--'
}

const statusTagType = (status) => {
  if (status === 'CLOSED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'WAITING_DOCTOR' || status === 'DOCTOR_IN_PROGRESS') return 'warning'
  return 'primary'
}

onMounted(() => {
  loadDashboard()
  window.addEventListener(ADMIN_NOTIFICATION_EVENT, handleAdminNotification)
})

onBeforeUnmount(() => {
  window.removeEventListener(ADMIN_NOTIFICATION_EVENT, handleAdminNotification)
})
</script>

<style lang="scss" scoped>
.dashboard-page {
  display: grid;
  gap: 24px;
}

.page-header,
.panel-header,
.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-header h2,
.panel-header h3,
.list-item strong {
  margin: 0;
  color: var(--pro-text-primary);
}

.page-header p,
.panel-header span,
.list-item p,
.label,
.hint {
  margin: 0;
  color: var(--pro-text-secondary);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.summary-card,
.panel {
  padding: 20px;
  border: 1px solid var(--pro-border-color);
  border-radius: var(--pro-radius-lg);
  background: var(--pro-bg-primary);
  box-shadow: var(--pro-shadow-sm);
}

.summary-card {
  display: grid;
  gap: 10px;
}

.summary-card strong {
  font-size: 32px;
  color: var(--pro-text-primary);
  font-family: var(--pro-title-font-family);
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
}

.panel {
  display: grid;
  gap: 16px;
}

.panel-list {
  display: grid;
  gap: 12px;
}

.list-item {
  padding: 14px 16px;
  border-radius: var(--pro-radius-md);
  background: var(--pro-bg-secondary);
}

@media (max-width: 1024px) {
  .summary-grid,
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
