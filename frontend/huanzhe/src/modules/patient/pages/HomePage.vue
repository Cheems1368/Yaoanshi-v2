<template>
  <ion-page>
    <ion-header class="custom-header">
      <ion-toolbar>
        <ion-title class="app-title">药按时</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="custom-content">
      <ion-refresher slot="fixed" @ionRefresh="handleRefresh($event)">
        <ion-refresher-content></ion-refresher-content>
      </ion-refresher>
      <div class="container">

        <!-- 欢迎区域 -->
        <section class="welcome-section">
          <h2 class="welcome-title">今日用药</h2>
          <p class="welcome-subtitle">{{ activePlan ? activePlan.planName : '暂无生效计划' }}</p>
        </section>

        <!-- 概览卡片（突出数字 + 进度条） -->
        <ion-card class="overview-card">
          <ion-card-content class="overview-body">
            <div class="overview-stats">
              <div class="overview-item">
                <div class="overview-number">{{ todayTasks.length }}</div>
                <div class="overview-label">今日任务</div>
              </div>
              <div class="overview-divider"></div>
              <div class="overview-item">
                <div class="overview-number overview-number--done">{{ completedCount }}</div>
                <div class="overview-label">已完成</div>
              </div>
              <div class="overview-divider"></div>
              <div class="overview-item">
                <div class="overview-number overview-number--pending">{{ pendingCount }}</div>
                <div class="overview-label">待处理</div>
              </div>
            </div>
            <div v-if="todayTasks.length > 0" class="progress-row">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
              </div>
              <span class="progress-label">{{ completedCount }}/{{ todayTasks.length }} 已完成</span>
            </div>
          </ion-card-content>
        </ion-card>

        <ion-card v-if="assessmentStatus?.due" class="assessment-card" data-testid="knowledge-assessment-task">
          <ion-card-content>
            <div>
              <div class="assessment-title">用药知识测评待完成</div>
              <div class="assessment-meta">{{ assessmentStatus.currentPhase === 'WEEK_4' ? '第 4 周复测' : '首次科普测评' }} · 约 3 分钟</div>
            </div>
            <ion-button size="small" @click="router.push('/knowledge-assessment')">去完成</ion-button>
          </ion-card-content>
        </ion-card>

        <!-- 快捷入口（浙里办 3 列宫格） -->
        <ion-card class="quick-actions-card">
          <ion-card-header>
            <ion-card-title class="card-title">快捷入口</ion-card-title>
          </ion-card-header>
          <ion-card-content class="quick-actions-grid">
            <button
              v-for="(item, index) in quickActions"
              :key="item.id"
              class="quick-action-item"
              :data-testid="quickActionTestIds[index]"
              @click="router.push(item.route)"
            >
              <div class="icon-container" :class="`icon-${item.color}`">
                <AppIcon :name="item.icon" :size="36" />
              </div>
              <span class="action-label">{{ item.label }}</span>
            </button>
          </ion-card-content>
        </ion-card>

        <!-- 近期任务 -->
        <ion-card class="recent-card">
          <ion-card-header>
            <ion-card-title class="card-title">近期任务</ion-card-title>
          </ion-card-header>
          <ion-card-content>
            <div v-if="loading" class="loading-state">
              <ion-spinner name="crescent" />
            </div>

            <EmptyState
              v-else-if="!activePlan && todayTasks.length === 0"
              type="no-plan"
              title="暂无生效用药计划"
              description="等待药师建立计划，或发起求助"
            >
              <template #action>
                <ion-button fill="outline" @click="router.push('/tabs/help-order')">
                  去提交求助
                </ion-button>
              </template>
            </EmptyState>

            <EmptyState
              v-else-if="todayTasks.length === 0"
              type="no-records"
              title="今天没有待执行任务"
            />

            <div v-else class="task-list">
              <button
                v-for="task in recentTasks"
                :key="task.id"
                class="task-item"
                @click="router.push('/tabs/medication')"
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
  IonContent,
  IonHeader,
  IonPage,
  IonRefresher,
  IonRefresherContent,
  IonSpinner,
  IonTitle,
  IonToast,
  IonToolbar,
  onIonViewWillEnter
} from '@ionic/vue'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  appAuthService,
  assessmentService,
  medicationService,
  type DoseTaskDTO,
  type KnowledgeAssessmentStatusDTO,
  type MedicationPlanDTO
} from '@/modules/patient/api/mvp'
import { patientSession } from '@/app/session'
import { cacheOfflineAccountType, loadMedicationTasks } from '@/modules/patient/services/offlineMedication'
import { enableAndScheduleMedicationReminders } from '@/modules/patient/services/localMedicationReminders'

const router = useRouter()

const loading = ref(false)
const activePlan = ref<MedicationPlanDTO | null>(null)
const todayTasks = ref<DoseTaskDTO[]>([])
const showToast = ref(false)
const toastMessage = ref('')
const assessmentStatus = ref<KnowledgeAssessmentStatusDTO | null>(null)

const quickActions = [
  { id: 1, label: '处方上传', icon: 'documentTextOutline', color: 'primary', route: '/prescription' },
  { id: 2, label: 'AI 助手', icon: 'sparkles', color: 'secondary', route: '/ai-assistant' },
  { id: 3, label: '自报用药', icon: 'medkit', color: 'success', route: '/self-reported-medications' }
]

const quickActionTestIds = [
  'home-quick-prescription',
  'home-quick-ai-assistant',
  'home-quick-self-reported-medications'
]

const completedCount = computed(() => todayTasks.value.filter((task) => task.status === 'COMPLETED').length)
const pendingCount = computed(() =>
  todayTasks.value.filter((task) => !['COMPLETED', 'MISSED', 'SKIPPED'].includes(task.status)).length
)
const progressPercent = computed(() => {
  if (!todayTasks.value.length) return 0
  return Math.round((completedCount.value / todayTasks.value.length) * 100)
})
const recentTasks = computed(() =>
  [...todayTasks.value]
    .sort((a, b) => String(a.scheduledAt).localeCompare(String(b.scheduledAt)))
    .slice(0, 3)
)

const showMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}

const loadDashboard = async () => {
  loading.value = true
  const expectedAccountSubject = patientSession.getAccountSubject()
  try {
    const [planResponse, tasksResponse, assessmentResponse, accountResponse] = await Promise.all([
      medicationService.getActiveMedicationPlan().catch(() => ({ data: null })),
      loadMedicationTasks(),
      assessmentService.getKnowledgeStatus().catch(() => ({ data: null })),
      appAuthService.getCurrentAccount().catch(() => ({ data: null }))
    ])
    if (!expectedAccountSubject || patientSession.getAccountSubject() !== expectedAccountSubject) {
      activePlan.value = null
      todayTasks.value = []
      assessmentStatus.value = null
      return
    }
    activePlan.value = planResponse.data ?? null
    const today = new Date().toLocaleDateString('sv-SE')
    todayTasks.value = tasksResponse.tasks.filter((task) => task.taskDate === today)
    const accountType = String((accountResponse.data as any)?.accountType || '').toUpperCase()
    if (accountType) {
      patientSession.setAccountType(accountType)
      void cacheOfflineAccountType(accountType, expectedAccountSubject).catch(() => undefined)
    }
    void enableAndScheduleMedicationReminders(tasksResponse.tasks, accountType || patientSession.getAccountType() || undefined)
      .then((mode) => { if (mode === 'inexact') showMessage('未授予精确闹钟权限，用药提醒可能延迟') })
      .catch(() => undefined)
    assessmentStatus.value = assessmentResponse.data ?? null
    if (!tasksResponse.offline) patientSession.setActivePlanStatus(Boolean(planResponse.data))
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
  const map: Record<string, string> = { PENDING: '待执行', COMPLETED: '已完成', MISSED: '漏服', SKIPPED: '跳过' }
  return map[status] || status
}

async function handleRefresh(event: CustomEvent) {
  try {
    await loadDashboard()
  } finally {
    (event.target as HTMLIonRefresherElement).complete()
  }
}

onIonViewWillEnter(loadDashboard)
</script>

<style scoped>
.custom-content {
  --background: var(--patient-bg-page);
}

.container {
  padding: var(--patient-space-4);
  display: flex;
  flex-direction: column;
  gap: var(--patient-space-3);
}

/* ── 欢迎区域 ── */
.welcome-section {
  padding: var(--patient-space-5);
  border-radius: var(--patient-radius-xl);
  color: var(--patient-text-inverse);
  background: linear-gradient(135deg, var(--patient-primary), var(--patient-primary-dark));
}

.welcome-title,
.welcome-subtitle {
  margin: 0;
}

.welcome-title {
  font-size: var(--patient-font-title);
  font-weight: var(--patient-font-bold);
  line-height: var(--patient-leading-tight);
}

.welcome-subtitle {
  margin-top: var(--patient-space-2);
  font-size: var(--patient-font-body);
  opacity: 0.9;
  line-height: var(--patient-leading-normal);
}

/* ── 概览卡片 ── */
.overview-card,
.quick-actions-card,
.assessment-card,
.recent-card {
  margin: 0;
}

.assessment-card ion-card-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--patient-space-3);
}

.assessment-title { font-weight: var(--patient-font-semibold); color: var(--patient-text-primary); }
.assessment-meta { margin-top: var(--patient-space-1); font-size: var(--patient-font-helper); color: var(--patient-text-secondary); }

.overview-body {
  padding: var(--patient-space-4) var(--patient-space-3) var(--patient-space-3);
}

.overview-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.overview-item {
  flex: 1;
  text-align: center;
}

.overview-number {
  font-size: 34px;
  font-weight: var(--patient-font-bold);
  line-height: var(--patient-leading-tight);
  color: var(--patient-primary);
}

.overview-number--done {
  color: var(--patient-success);
}

.overview-number--pending {
  color: var(--patient-warning);
}

.overview-label {
  font-size: var(--patient-font-helper);
  color: var(--patient-text-secondary);
  margin-top: var(--patient-space-1);
}

.overview-divider {
  width: 1px;
  height: 40px;
  background: var(--patient-border);
  flex-shrink: 0;
}

/* ── 进度条 ── */
.progress-row {
  display: flex;
  align-items: center;
  gap: var(--patient-space-2);
  margin-top: var(--patient-space-3);
  padding-top: var(--patient-space-3);
  border-top: var(--patient-border-light);
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: var(--patient-primary-light);
  border-radius: var(--patient-radius-xl);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--patient-primary), var(--patient-primary-dark));
  border-radius: var(--patient-radius-xl);
  transition: width 320ms cubic-bezier(0.2, 0, 0, 1);
}

.progress-label {
  font-size: var(--patient-font-caption);
  font-weight: var(--patient-font-semibold);
  color: var(--patient-text-secondary);
  white-space: nowrap;
}

/* ── 快捷入口（浙里办 3 列宫格） ── */
.card-title {
  font-size: var(--patient-font-subtitle);
  font-weight: var(--patient-font-bold);
  color: var(--patient-text-primary);
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--patient-space-3);
  padding-top: var(--patient-space-2);
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--patient-space-2);
  padding: var(--patient-space-4) var(--patient-space-2);
  border: 0;
  border-radius: var(--patient-radius-lg);
  background: var(--patient-bg-page);
  cursor: pointer;
  min-height: var(--patient-space-12);
  transition: opacity 160ms ease;
}

.quick-action-item:active {
  opacity: 0.72;
}

.icon-container {
  width: 56px;
  height: 56px;
  border-radius: var(--patient-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-primary {
  background: var(--patient-primary-light);
  color: var(--patient-primary);
}

.icon-secondary {
  background: var(--patient-primary-light);
  color: var(--patient-primary);
}

.icon-success {
  background: rgba(16, 185, 129, 0.12);
  color: var(--patient-success);
}

.action-label {
  font-size: var(--patient-font-helper);
  font-weight: var(--patient-font-semibold);
  color: var(--patient-text-primary);
  text-align: center;
  line-height: var(--patient-leading-tight);
}

/* ── 近期任务 ── */
.loading-state {
  padding: var(--patient-space-8) var(--patient-space-4);
  text-align: center;
}

.task-list {
  display: flex;
  flex-direction: column;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--patient-space-3);
  padding: var(--patient-space-4) 0;
  border: 0;
  background: transparent;
  border-bottom: var(--patient-border-light);
  cursor: pointer;
  min-height: 64px;
  text-align: left;
  width: 100%;
}

.task-item:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}

.task-title {
  font-size: var(--patient-font-body);
  font-weight: var(--patient-font-semibold);
  color: var(--patient-text-primary);
  line-height: var(--patient-leading-tight);
}

.task-meta {
  font-size: var(--patient-font-helper);
  color: var(--patient-text-secondary);
  margin-top: var(--patient-space-1);
}
</style>
