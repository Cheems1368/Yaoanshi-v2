<template>
  <main class="mobile-workorders">
    <header class="mobile-header">
      <div>
        <p>医生移动工单</p>
        <h1>{{ selectedOrder ? '工单详情' : '升级待办' }}</h1>
      </div>
      <el-button circle :icon="Refresh" :loading="loading" @click="loadOrders" />
    </header>

    <div v-if="notificationHint" class="notification-hint">{{ notificationHint }}</div>

    <section v-if="!selectedOrder" class="order-list">
      <el-skeleton v-if="loading" :rows="6" animated />
      <el-empty v-else-if="orders.length === 0" description="暂无升级工单" />
      <button
        v-for="order in orders"
        v-else
        :key="order.id"
        class="order-card"
        type="button"
        @click="selectOrder(order.id)"
      >
        <div class="order-card__top">
          <strong>{{ order.title }}</strong>
          <el-tag :type="statusTagType(order.status)" size="small">{{ statusText(order.status, 'doctor') }}</el-tag>
        </div>
        <p>{{ order.description || order.title }}</p>
        <span>{{ order.patientName }} · {{ categoryText(order.category) }} · {{ formatDate(order.createdAt) }}</span>
      </button>
    </section>

    <section v-else class="order-detail">
      <el-button text :icon="ArrowLeft" class="back-button" @click="selectedOrder = null">返回列表</el-button>

      <el-skeleton v-if="detailLoading" :rows="8" animated />
      <template v-else-if="detail">
        <article class="detail-summary">
          <div class="detail-summary__top">
            <h2>{{ detail.title }}</h2>
            <el-tag :type="statusTagType(detail.status)">{{ statusText(detail.status, 'doctor') }}</el-tag>
          </div>
          <p class="detail-meta">{{ categoryText(detail.category) }} · {{ formatDate(detail.createdAt) }}</p>
          <p v-if="detail.description" class="detail-description">{{ detail.description }}</p>
        </article>

        <article v-if="detail.patientContext" class="patient-context">
          <h3>患者上下文</h3>
          <dl>
            <div>
              <dt>患者</dt>
              <dd>{{ contextName }}</dd>
            </div>
            <div>
              <dt>主要诊断</dt>
              <dd>{{ detail.patientContext.primaryDiseaseName || '未填写' }}</dd>
            </div>
            <div>
              <dt>药物过敏</dt>
              <dd>{{ detail.patientContext.drugAllergy || '未填写' }}</dd>
            </div>
            <div>
              <dt>食物过敏</dt>
              <dd>{{ detail.patientContext.foodAllergy || '未填写' }}</dd>
            </div>
            <div>
              <dt>当前用药</dt>
              <dd>{{ detail.patientContext.currentMedicationSummary || '未填写' }}</dd>
            </div>
            <div>
              <dt>病史摘要</dt>
              <dd>{{ detail.patientContext.medicalHistorySummary || '未填写' }}</dd>
            </div>
          </dl>
        </article>

        <article class="timeline">
          <h3>会话记录</h3>
          <div v-for="event in detail.events || []" :key="event.id" class="timeline-item">
            <div class="timeline-item__head">
              <strong>{{ eventText(event.eventType) }}</strong>
              <span>{{ formatDate(event.eventAt) }}</span>
            </div>
            <p class="timeline-operator">{{ event.operatorName || event.operatorType }}</p>
            <el-tag v-if="event.decisionResult" type="success" size="small" effect="plain">
              {{ decisionText(event.decisionResult) }}
            </el-tag>
            <p v-if="event.content" class="timeline-content">{{ event.content }}</p>
            <div v-if="event.attachments?.length" class="attachment-grid">
              <a
                v-for="attachment in event.attachments"
                :key="attachment.fileAssetId"
                :href="attachment.downloadUrl"
                target="_blank"
                rel="noreferrer"
              >
                <img :src="attachment.downloadUrl" :alt="attachment.originalFilename" />
              </a>
            </div>
          </div>
        </article>

        <footer v-if="canDecide" class="decision-box">
          <el-segmented v-model="decisionResult" :options="decisionOptions" block />
          <el-input
            v-model="decisionContent"
            type="textarea"
            :rows="4"
            resize="none"
            placeholder="填写医生最终结论，患者和药师都会看到"
          />
          <el-button
            type="primary"
            :loading="deciding"
            :disabled="!decisionContent.trim()"
            @click="submitDecision"
          >
            提交医生决策
          </el-button>
        </footer>
        <div v-else class="terminal-note">
          {{ terminalText }}
        </div>
      </template>
    </section>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { doctorDecisionWorkOrder, getWorkOrderDetail, getWorkOrders } from '@/api/workorders'
import { ADMIN_NOTIFICATION_EVENT } from '@/utils/adminNotifications'
import {
  categoryText,
  decisionText,
  eventText,
  formatDate,
  statusTagType,
  statusText
} from '@/views/workorders/composables/useWorkOrders'

const orders = ref([])
const loading = ref(false)
const selectedOrder = ref(null)
const detail = ref(null)
const detailLoading = ref(false)
const decisionResult = ref('ADOPTED')
const decisionContent = ref('')
const deciding = ref(false)
const notificationHint = ref('')

const decisionOptions = [
  { label: '采纳建议', value: 'ADOPTED' },
  { label: '不采纳建议', value: 'NOT_ADOPTED' }
]

const canDecide = computed(() => ['WAITING_DOCTOR', 'DOCTOR_IN_PROGRESS'].includes(detail.value?.status))
const terminalText = computed(() => {
  if (detail.value?.status === 'CLOSED') return '工单已关闭，当前仅支持查看。'
  if (detail.value?.status === 'REJECTED') return '工单已驳回，当前仅支持查看。'
  return '当前状态不能提交医生决策。'
})
const contextName = computed(() => {
  const context = detail.value?.patientContext || {}
  const parts = [context.name, context.gender, context.age ? `${context.age}岁` : null].filter(Boolean)
  return parts.join(' / ') || '未填写'
})

async function loadOrders() {
  loading.value = true
  try {
    const response = await getWorkOrders({ status: 'WAITING_DOCTOR,DOCTOR_IN_PROGRESS', pageNo: 1, pageSize: 30 })
    orders.value = response.data?.list || []
  } finally {
    loading.value = false
  }
}

async function selectOrder(id) {
  selectedOrder.value = id
  await loadDetail(id)
}

async function loadDetail(id = selectedOrder.value) {
  if (!id) return
  detailLoading.value = true
  try {
    const response = await getWorkOrderDetail(id)
    detail.value = response.data
    decisionContent.value = ''
    decisionResult.value = 'ADOPTED'
  } finally {
    detailLoading.value = false
  }
}

async function submitDecision() {
  if (!selectedOrder.value || !decisionContent.value.trim()) return
  deciding.value = true
  try {
    await doctorDecisionWorkOrder(selectedOrder.value, {
      decisionResult: decisionResult.value,
      content: decisionContent.value.trim()
    })
    ElMessage.success('医生决策已提交')
    await Promise.all([loadOrders(), loadDetail()])
  } finally {
    deciding.value = false
  }
}

function handleNotification(event) {
  if (!event?.detail?.businessType?.startsWith('HELP_ORDER_')) return
  notificationHint.value = '有新的工单动态，列表已刷新'
  window.setTimeout(() => {
    notificationHint.value = ''
  }, 3000)
  void loadOrders()
  if (selectedOrder.value && Number(event.detail.businessId) === Number(selectedOrder.value)) {
    void loadDetail()
  }
}

onMounted(() => {
  void loadOrders()
  window.addEventListener(ADMIN_NOTIFICATION_EVENT, handleNotification)
})

onBeforeUnmount(() => {
  window.removeEventListener(ADMIN_NOTIFICATION_EVENT, handleNotification)
})
</script>

<style lang="scss" scoped>
.mobile-workorders {
  min-height: 100vh;
  background: var(--pro-bg-secondary);
  color: var(--pro-text-primary);
  padding: 16px;
}

.mobile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.mobile-header p,
.mobile-header h1 {
  margin: 0;
}

.mobile-header p {
  color: var(--pro-text-secondary);
  font-size: 13px;
}

.mobile-header h1 {
  margin-top: 4px;
  font-size: 22px;
}

.notification-hint {
  margin-bottom: 12px;
  padding: 10px 12px;
  border-radius: var(--pro-radius-md);
  background: var(--pro-brand-secondary);
  color: var(--pro-brand-primary);
  font-size: 14px;
}

.order-list,
.order-detail,
.timeline,
.patient-context {
  display: grid;
  gap: 12px;
}

.order-card,
.detail-summary,
.patient-context,
.timeline,
.decision-box,
.terminal-note {
  border: 1px solid var(--pro-border-color);
  border-radius: var(--pro-radius-md);
  background: var(--pro-bg-primary);
}

.order-card {
  width: 100%;
  padding: 14px;
  text-align: left;
}

.order-card__top,
.detail-summary__top,
.timeline-item__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.order-card strong,
.detail-summary h2 {
  margin: 0;
  color: var(--pro-text-primary);
}

.order-card p,
.detail-description,
.timeline-content {
  margin: 8px 0 0;
  color: var(--pro-text-primary);
  line-height: 1.55;
  white-space: pre-wrap;
}

.order-card span,
.detail-meta,
.timeline-item__head span,
.timeline-operator {
  color: var(--pro-text-secondary);
  font-size: 13px;
}

.back-button {
  justify-self: start;
}

.detail-summary,
.patient-context,
.timeline,
.decision-box,
.terminal-note {
  padding: 14px;
}

.patient-context h3,
.timeline h3 {
  margin: 0;
  font-size: 16px;
}

.patient-context dl {
  display: grid;
  gap: 10px;
  margin: 0;
}

.patient-context dt {
  margin-bottom: 2px;
  color: var(--pro-text-secondary);
  font-size: 12px;
}

.patient-context dd {
  margin: 0;
  color: var(--pro-text-primary);
  line-height: 1.45;
}

.timeline-item {
  display: grid;
  gap: 6px;
  padding: 12px 0;
  border-top: 1px solid var(--pro-border-color);
}

.timeline-item:first-of-type {
  border-top: none;
}

.timeline-content {
  padding: 10px 12px;
  border-radius: var(--pro-radius-sm);
  background: var(--pro-bg-secondary);
}

.attachment-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.attachment-grid a {
  display: block;
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: var(--pro-radius-sm);
  border: 1px solid var(--pro-border-color);
}

.attachment-grid img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.decision-box {
  position: sticky;
  bottom: 0;
  display: grid;
  gap: 12px;
  box-shadow: var(--pro-shadow-sm);
}

.terminal-note {
  color: var(--pro-text-secondary);
  text-align: center;
}
</style>
