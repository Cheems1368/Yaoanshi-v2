<template>
  <ion-page>
    <ion-header class="custom-header">
      <ion-toolbar>
        <ion-title class="app-title">药按时</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="custom-content">
      <div class="container">
        <section class="welcome-section">
          <h2 class="welcome-title">今日用药</h2>
          <p class="welcome-subtitle">{{ activePlan ? activePlan.planName : '暂无生效计划' }}</p>
        </section>

        <ion-card class="overview-card">
          <ion-card-content class="overview-content">
            <div class="overview-item">
              <div class="overview-number">{{ todayTasks.length }}</div>
              <div class="overview-label">今日任务</div>
            </div>
            <div class="overview-item">
              <div class="overview-number">{{ completedCount }}</div>
              <div class="overview-label">已完成</div>
            </div>
            <div class="overview-item">
              <div class="overview-number">{{ pendingCount }}</div>
              <div class="overview-label">待处理</div>
            </div>
          </ion-card-content>
        </ion-card>

        <ion-card class="quick-actions-card">
          <ion-card-header>
            <ion-card-title class="card-title">快速入口</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <ion-grid>
              <ion-row>
                <ion-col size="3" v-for="(item, index) in quickActions" :key="item.id">
                  <button
                    class="quick-action-item"
                    :data-testid="quickActionTestIds[index]"
                    @click="router.push(item.route)"
                  >
                    <div class="icon-container" :class="`icon-${item.color}`">
                      <ion-icon :icon="item.icon" />
                    </div>
                    <span class="action-label">{{ item.label }}</span>
                  </button>
                </ion-col>
              </ion-row>
            </ion-grid>
          </ion-card-content>
        </ion-card>

        <ion-card class="recent-card">
          <ion-card-header>
            <ion-card-title class="card-title">近期任务</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <div v-if="loading" class="empty-state">
              <ion-spinner name="crescent" />
            </div>

            <div v-else-if="!activePlan" class="empty-state">
              <ion-icon :icon="documentTextOutline" class="empty-icon" />
              <p class="empty-text">暂无生效用药计划，等待药师建计划</p>
              <ion-button fill="outline" @click="router.push('/tabs/help-order')">去提交求助</ion-button>
            </div>

            <div v-else-if="todayTasks.length === 0" class="empty-state">
              <ion-icon :icon="checkmarkCircleOutline" class="empty-icon" />
              <p class="empty-text">今天没有待执行任务</p>
            </div>

            <div v-else class="task-list">
              <button
                v-for="task in recentTasks"
                :key="task.id"
                class="task-item"
                @click="router.push('/medication-reminder')"
              >
                <div>
                  <div class="task-title">{{ task.drugName }}</div>
                  <div class="task-meta">{{ formatTime(task.scheduledAt) }} · {{ task.dosage }}</div>
                </div>
                <ion-badge :color="task.status === 'COMPLETED' ? 'success' : task.status === 'MISSED' ? 'danger' : 'medium'">
                  {{ statusText(task.status) }}
                </ion-badge>
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
  IonCardTitle,
  IonCol,
  IonContent,
  IonGrid,
  IonHeader,
  IonIcon,
  IonPage,
  IonRow,
  IonSpinner,
  IonTitle,
  IonToast,
  IonToolbar,
  onIonViewWillEnter
} from '@ionic/vue'
import { chatbubbles, clipboard, documentTextOutline, medkit, notifications, shieldCheckmark, sparkles, checkmarkCircleOutline } from 'ionicons/icons'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { medicationService, type DoseTaskDTO, type MedicationPlanDTO } from '@/modules/patient/api/mvp'
import { patientSession } from '@/app/session'

const router = useRouter()

const loading = ref(false)
const activePlan = ref<MedicationPlanDTO | null>(null)
const todayTasks = ref<DoseTaskDTO[]>([])
const showToast = ref(false)
const toastMessage = ref('')

const quickActions = [
  { id: 1, label: '用药提醒', icon: notifications, color: 'primary', route: '/medication-reminder' },
  { id: 2, label: '服药记录', icon: clipboard, color: 'success', route: '/medication-record' },
  { id: 3, label: '用药求助', icon: chatbubbles, color: 'warning', route: '/tabs/help-order' },
  { id: 4, label: '合理用药', icon: shieldCheckmark, color: 'tertiary', route: '/rational-medication' },
  { id: 5, label: 'AI助手', icon: sparkles, color: 'secondary', route: '/ai-assistant' },
  { id: 6, label: '记录新药品', icon: medkit, color: 'rose', route: '/self-reported-medications' }
]

const quickActionTestIds = [
  'home-quick-medication-reminder',
  'home-quick-medication-record',
  'home-quick-help-order',
  'home-quick-rational-medication',
  'home-quick-ai-assistant',
  'home-quick-self-reported-medications'
]

const completedCount = computed(() => todayTasks.value.filter((task) => task.status === 'COMPLETED').length)
const pendingCount = computed(() => todayTasks.value.filter((task) => !['COMPLETED', 'MISSED', 'SKIPPED'].includes(task.status)).length)
const recentTasks = computed(() => [...todayTasks.value].sort((a, b) => String(a.scheduledAt).localeCompare(String(b.scheduledAt))).slice(0, 3))

const showMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}

const loadDashboard = async () => {
  loading.value = true
  try {
    const [planResponse, tasksResponse] = await Promise.all([
      medicationService.getActiveMedicationPlan(),
      medicationService.getTodayDoseTasks()
    ])
    activePlan.value = planResponse.data ?? null
    todayTasks.value = tasksResponse.data ?? []
    patientSession.setActivePlanStatus(Boolean(planResponse.data))
  } catch (error: any) {
    activePlan.value = null
    todayTasks.value = []
    patientSession.setActivePlanStatus(false)
    showMessage(error?.message || '首页数据加载失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (value?: string | null) => {
  if (!value) return '--:--'
  const date = new Date(value)
  return Number.isNaN(date.getTime())
    ? value
    : date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false })
}

const statusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待执行',
    COMPLETED: '已完成',
    MISSED: '漏服',
    SKIPPED: '跳过'
  }
  return map[status] || status
}

onIonViewWillEnter(loadDashboard)
</script>

<style scoped>
.custom-content {
  --background: #f6f8fb;
}

.container {
  padding: 16px;
}

.welcome-section {
  padding: 20px;
  margin-bottom: 16px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(135deg, #1d4ed8, #0f766e);
}

.welcome-title,
.welcome-subtitle {
  margin: 0;
}

.welcome-title {
  font-size: 24px;
  font-weight: 700;
}

.welcome-subtitle {
  margin-top: 8px;
  opacity: 0.9;
}

.overview-card,
.quick-actions-card,
.recent-card {
  margin-bottom: 16px;
  border-radius: 18px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.overview-content {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.overview-item {
  flex: 1;
  text-align: center;
}

.overview-number {
  font-size: 28px;
  font-weight: 700;
  color: #1d4ed8;
}

.overview-label,
.action-label,
.task-meta,
.empty-text {
  color: #64748b;
}

.card-title {
  font-weight: 700;
}

.quick-action-item {
  width: 100%;
  border: 0;
  background: transparent;
  padding: 8px 4px;
}

.icon-container {
  width: 48px;
  height: 48px;
  margin: 0 auto 8px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-primary { background: rgba(29, 78, 216, 0.12); color: #1d4ed8; }
.icon-success { background: rgba(22, 163, 74, 0.12); color: #16a34a; }
.icon-warning { background: rgba(245, 158, 11, 0.16); color: #d97706; }
.icon-tertiary { background: rgba(14, 165, 233, 0.12); color: #0284c7; }
.icon-secondary { background: rgba(99, 102, 241, 0.12); color: #4f46e5; }
.icon-rose { background: rgba(225, 29, 72, 0.10); color: #e11d48; }

.task-list {
  display: grid;
  gap: 12px;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  border: 0;
  background: transparent;
  border-bottom: 1px solid #e2e8f0;
}

.task-item:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}

.task-title {
  font-weight: 600;
  color: #0f172a;
}

.empty-state {
  padding: 28px 12px;
  text-align: center;
}

.empty-icon {
  font-size: 36px;
  color: #94a3b8;
}
</style>
