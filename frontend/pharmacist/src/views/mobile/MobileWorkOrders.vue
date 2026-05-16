<template>
  <main class="mobile-workorders">
    <header class="mobile-header">
      <div>
        <p>移动工单</p>
        <h1>待处理工单</h1>
      </div>
      <el-button circle :icon="Refresh" :loading="loading" @click="loadOrders" />
    </header>

    <div v-if="notificationHint" class="notification-hint">{{ notificationHint }}</div>

    <section v-if="!selectedOrder" class="order-list">
      <el-skeleton v-if="loading" :rows="6" animated />
      <el-empty v-else-if="orders.length === 0" description="暂无待处理工单" />
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
          <el-tag :type="statusTagType(order.status)" size="small">{{ statusText(order.status) }}</el-tag>
        </div>
        <p>{{ order.description || order.title }}</p>
        <span>{{ categoryText(order.category) }} · {{ formatDate(order.createdAt) }}</span>
      </button>
    </section>

    <section v-else class="order-detail">
      <el-button text :icon="ArrowLeft" class="back-button" @click="selectedOrder = null">返回列表</el-button>

      <el-skeleton v-if="detailLoading" :rows="8" animated />
      <template v-else-if="detail">
        <article class="detail-summary">
          <div class="detail-summary__top">
            <h2>{{ detail.title }}</h2>
            <el-tag :type="statusTagType(detail.status)">{{ statusText(detail.status) }}</el-tag>
          </div>
          <p class="detail-meta">{{ categoryText(detail.category) }} · {{ formatDate(detail.createdAt) }}</p>
          <p v-if="detail.description" class="detail-description">{{ detail.description }}</p>
        </article>

        <article class="timeline">
          <h3>会话记录</h3>
          <div v-for="event in detail.events || []" :key="event.id" class="timeline-item">
            <div class="timeline-item__head">
              <strong>{{ eventText(event.eventType) }}</strong>
              <span>{{ formatDate(event.eventAt) }}</span>
            </div>
            <p class="timeline-operator">{{ event.operatorName || event.operatorType }}</p>
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

        <footer v-if="!isTerminalStatus" class="reply-box">
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="3"
            resize="none"
            placeholder="输入给患者的回复"
          />
          <el-button type="primary" :loading="replying" :disabled="!replyContent.trim()" @click="sendReply">
            回复患者
          </el-button>
        </footer>
        <div v-else class="terminal-note">工单已结束，当前仅支持查看。</div>
      </template>
    </section>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ArrowLeft, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getWorkOrderDetail, getWorkOrders, replyWorkOrder } from '@/modules/pharmacist/api/workorders'
import { ADMIN_NOTIFICATION_EVENT } from '@/utils/adminNotifications'
import { categoryText, eventText, formatDate, statusTagType, statusText } from '@/views/workorders/composables/useWorkOrders'

const orders = ref([])
const loading = ref(false)
const selectedOrder = ref(null)
const detail = ref(null)
const detailLoading = ref(false)
const replyContent = ref('')
const replying = ref(false)
const notificationHint = ref('')

const isTerminalStatus = computed(() => ['CLOSED', 'REJECTED'].includes(detail.value?.status))

async function loadOrders() {
  loading.value = true
  try {
    const response = await getWorkOrders({ status: 'SUBMITTED,PHARMACIST_IN_PROGRESS', pageNo: 1, pageSize: 30 })
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
  } finally {
    detailLoading.value = false
  }
}

async function sendReply() {
  if (!selectedOrder.value || !replyContent.value.trim()) return
  replying.value = true
  try {
    await replyWorkOrder(selectedOrder.value, { content: replyContent.value.trim() })
    replyContent.value = ''
    ElMessage.success('回复已发送')
    await Promise.all([loadOrders(), loadDetail()])
  } finally {
    replying.value = false
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
.timeline {
  display: grid;
  gap: 12px;
}

.order-card {
  width: 100%;
  border: 1px solid var(--pro-border-color);
  border-radius: var(--pro-radius-md);
  background: var(--pro-bg-primary);
  padding: 14px;
  text-align: left;
  box-shadow: var(--pro-shadow-sm);
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
  overflow-wrap: anywhere;
}

.order-card p,
.order-card span,
.detail-meta,
.timeline-operator,
.timeline-item__head span {
  color: var(--pro-text-secondary);
  font-size: 13px;
}

.detail-summary,
.timeline,
.reply-box,
.terminal-note {
  border: 1px solid var(--pro-border-color);
  border-radius: var(--pro-radius-md);
  background: var(--pro-bg-primary);
  padding: 14px;
}

.back-button {
  justify-self: start;
}

.detail-summary h2,
.timeline h3 {
  margin: 0;
}

.detail-description,
.timeline-content {
  white-space: pre-wrap;
  line-height: 1.6;
}

.timeline-item {
  display: grid;
  gap: 6px;
  padding-top: 12px;
  border-top: 1px solid var(--pro-border-color);
}

.attachment-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.attachment-grid a {
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: var(--pro-radius-sm);
  background: var(--pro-bg-secondary);
}

.attachment-grid img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.reply-box {
  position: sticky;
  bottom: 12px;
  display: grid;
  gap: 10px;
  box-shadow: var(--pro-shadow-md);
}
</style>
