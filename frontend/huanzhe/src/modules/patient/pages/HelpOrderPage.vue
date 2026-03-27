<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>用药求助</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="page-content">
      <ion-refresher slot="fixed" @ionRefresh="handleRefresh($event)">
        <ion-refresher-content></ion-refresher-content>
      </ion-refresher>
      <div class="page-container">
        <ion-card class="form-card">
          <ion-card-header>
            <ion-card-title>提交求助工单</ion-card-title>
            <ion-card-subtitle>提交后会进入药师处理队列，必要时再升级到医生</ion-card-subtitle>
          </ion-card-header>
          <ion-card-content>
            <ion-item>
              <ion-label position="stacked">问题类型</ion-label>
              <ion-select v-model="form.category" interface="popover">
                <ion-select-option value="MEDICATION_CONSULTATION">用药咨询</ion-select-option>
                <ion-select-option value="MISSED_OR_WRONG_DOSE">漏服 / 错服</ion-select-option>
                <ion-select-option value="ADVERSE_REACTION">不良反应</ion-select-option>
                <ion-select-option value="PLAN_CHANGE">计划调整</ion-select-option>
                <ion-select-option value="OTHER">其他</ion-select-option>
              </ion-select>
            </ion-item>

            <ion-item>
              <ion-label position="stacked">标题</ion-label>
              <ion-input v-model="form.title" placeholder="例如：今天上午漏服降压药怎么办" />
            </ion-item>

            <ion-item>
              <ion-label position="stacked">详细说明</ion-label>
              <ion-textarea
                v-model="form.description"
                auto-grow
                placeholder="可选填写：当前症状、药名、时间点以及希望获得的帮助"
              />
            </ion-item>

            <ion-item lines="none">
              <ion-checkbox v-model="form.requiresOfflineEmergency" />
              <ion-label class="checkbox-label">存在紧急风险，需要线下急诊提醒</ion-label>
            </ion-item>

            <ion-button expand="block" :disabled="submitting || !canSubmit" @click="submitOrder">
              {{ submitting ? '提交中...' : '提交工单' }}
            </ion-button>
          </ion-card-content>
        </ion-card>

        <ion-card class="list-card">
          <ion-card-header>
            <ion-card-title>我的工单</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <div v-if="loading" class="empty-state">
              <ion-spinner name="crescent" />
            </div>

            <div v-else-if="orders.length === 0" class="empty-state">
              <ion-icon :icon="chatboxEllipsesOutline" class="empty-icon" />
              <p class="empty-text">还没有提交过工单</p>
            </div>

            <div v-else class="order-list">
              <button
                v-for="order in orders"
                :key="order.id"
                class="order-item"
                type="button"
                @click="openDetail(order.id)"
              >
                <div class="order-top">
                  <div>
                    <div class="order-title">{{ order.title }}</div>
                    <div class="order-meta">{{ categoryText(order.category) }} · {{ formatDateTime(order.createdAt) }}</div>
                  </div>
                  <ion-badge :color="statusColor(order.status)">{{ statusText(order.status) }}</ion-badge>
                </div>
                <p class="order-desc">{{ order.description || order.title }}</p>
                <div class="order-meta">
                  当前处理：{{ order.currentHandlerName || handlerText(order.currentHandlerType) || '待分配' }}
                </div>
                <div v-if="order.rejectReason" class="order-meta">驳回原因：{{ order.rejectReason }}</div>
                <div v-if="order.serviceNotice" class="order-meta">服务说明：{{ order.serviceNotice }}</div>
              </button>
            </div>
          </ion-card-content>
        </ion-card>
      </div>

      <ion-toast :is-open="showToast" :message="toastMessage" :duration="2400" @did-dismiss="showToast = false" />
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import {
  IonBadge,
  IonButton,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardSubtitle,
  IonCardTitle,
  IonCheckbox,
  IonContent,
  IonHeader,
  IonIcon,
  IonInput,
  IonItem,
  IonLabel,
  IonPage,
  IonRefresher,
  IonRefresherContent,
  IonSelect,
  IonSelectOption,
  IonSpinner,
  IonTextarea,
  IonTitle,
  IonToast,
  IonToolbar,
  onIonViewWillEnter
} from '@ionic/vue'
import { computed, ref } from 'vue'
import { chatboxEllipsesOutline } from 'ionicons/icons'
import { useRoute, useRouter } from 'vue-router'
import { helpOrderService, type HelpOrderDTO } from '@/modules/patient/api/mvp'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const orders = ref<HelpOrderDTO[]>([])
const showToast = ref(false)
const toastMessage = ref('')

const form = ref({
  category: 'MEDICATION_CONSULTATION',
  title: '',
  description: '',
  requiresOfflineEmergency: false
})

const canSubmit = computed(() => Boolean(form.value.title.trim()))

const showMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}

const readQueryValue = (value: unknown) => {
  if (Array.isArray(value)) {
    return typeof value[0] === 'string' ? value[0] : ''
  }
  return typeof value === 'string' ? value : ''
}

const applyPrefillFromQuery = async () => {
  const category = readQueryValue(route.query.category)
  const title = readQueryValue(route.query.title)
  const description = readQueryValue(route.query.description)
  const requiresOfflineEmergency = readQueryValue(route.query.requiresOfflineEmergency)

  if (!category && !title && !description && !requiresOfflineEmergency) {
    return
  }

  form.value = {
    category: category || 'MEDICATION_CONSULTATION',
    title,
    description,
    requiresOfflineEmergency: requiresOfflineEmergency === 'true'
  }

  await router.replace({ path: route.path, query: {} })
}

const loadOrders = async () => {
  loading.value = true
  try {
    const response = await helpOrderService.getHelpOrders({ pageNo: 1, pageSize: 20 })
    orders.value = response.data?.list ?? []
  } catch (error: any) {
    orders.value = []
    showMessage(error?.message || '工单列表加载失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.value = {
    category: 'MEDICATION_CONSULTATION',
    title: '',
    description: '',
    requiresOfflineEmergency: false
  }
}

const openDetail = (id: number) => {
  void router.push(`/help-order/${id}`)
}

const submitOrder = async () => {
  if (!canSubmit.value) {
    showMessage('请填写标题')
    return
  }

  submitting.value = true
  try {
    const response = await helpOrderService.createHelpOrder({
      category: form.value.category,
      title: form.value.title.trim(),
      description: form.value.description.trim() || undefined,
      priority: form.value.requiresOfflineEmergency ? 'URGENT' : 'NORMAL',
      requiresOfflineEmergency: form.value.requiresOfflineEmergency
    })
    const createdId = response.data?.id
    resetForm()
    if (createdId) {
      void router.push(`/help-order/${createdId}`)
      return
    }
    showMessage('工单已提交')
    await loadOrders()
  } catch (error: any) {
    showMessage(error?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const statusText = (status: string) => {
  const map: Record<string, string> = {
    SUBMITTED: '待药师处理',
    PHARMACIST_IN_PROGRESS: '药师处理中',
    WAITING_DOCTOR: '待医生处理',
    DOCTOR_IN_PROGRESS: '医生处理中',
    CLOSED: '已关闭',
    REJECTED: '已驳回'
  }
  return map[status] || status
}

const statusColor = (status: string) => {
  if (status === 'CLOSED') return 'success'
  if (status === 'REJECTED') return 'danger'
  if (status === 'WAITING_DOCTOR' || status === 'DOCTOR_IN_PROGRESS') return 'warning'
  return 'primary'
}

const categoryText = (category: string) => {
  const map: Record<string, string> = {
    MEDICATION_CONSULTATION: '用药咨询',
    MISSED_OR_WRONG_DOSE: '漏服 / 错服',
    ADVERSE_REACTION: '不良反应',
    PLAN_CHANGE: '计划调整',
    OTHER: '其他'
  }
  return map[category] || category
}

const handlerText = (handlerType?: string | null) => {
  if (handlerType === 'PHARMACIST') return '药师'
  if (handlerType === 'DOCTOR') return '医生'
  return ''
}

const formatDateTime = (value?: string | null) => {
  if (!value) return '--'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

async function handleRefresh(event: CustomEvent) {
  try {
    await loadOrders()
  } finally {
    ;(event.target as HTMLIonRefresherElement).complete()
  }
}

onIonViewWillEnter(async () => {
  await applyPrefillFromQuery()
  await loadOrders()
})
</script>

<style scoped>
.page-content {
  --background: var(--patient-bg-page);
}

.page-container {
  padding: 16px;
}

.form-card,
.list-card {
  border-radius: 18px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.checkbox-label {
  margin-left: 12px;
}

.order-list {
  display: grid;
  gap: 14px;
}

.order-item {
  width: 100%;
  padding: 14px;
  border: 0;
  border-radius: 14px;
  background: var(--patient-bg-page);
  text-align: left;
}

.order-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.order-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--patient-text-primary);
}

.order-meta,
.order-desc,
.empty-text {
  color: var(--patient-text-secondary);
}

.order-desc {
  margin: 10px 0;
  white-space: pre-wrap;
}

.empty-state {
  text-align: center;
  padding: 32px 12px;
}

.empty-icon {
  font-size: 36px;
  color: var(--patient-text-muted);
}
</style>
