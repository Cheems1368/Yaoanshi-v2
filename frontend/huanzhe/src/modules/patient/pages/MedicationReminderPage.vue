<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/tabs/home" />
        </ion-buttons>
        <ion-title>用药提醒</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="page-content">
      <ion-refresher slot="fixed" @ionRefresh="handleRefresh($event)">
        <ion-refresher-content></ion-refresher-content>
      </ion-refresher>
      <div class="container">
        <ion-card class="overview-card">
          <ion-card-content class="overview-grid">
            <div class="overview-item">
              <div class="overview-number">{{ tasks.length }}</div>
              <div class="overview-label">今日任务</div>
            </div>
            <div class="overview-divider"></div>
            <div class="overview-item">
              <div class="overview-number overview-number--done">{{ completedCount }}</div>
              <div class="overview-label">已服药</div>
            </div>
            <div class="overview-divider"></div>
            <div class="overview-item">
              <div class="overview-number overview-number--pending">{{ pendingCount }}</div>
              <div class="overview-label">待处理</div>
            </div>
          </ion-card-content>
        </ion-card>

        <ion-card v-if="activePlan" class="plan-card">
          <ion-card-header>
            <ion-card-title>{{ activePlan.planName }}</ion-card-title>
            <ion-card-subtitle>版本 {{ activePlan.versionNo }} · {{ activePlan.status }}</ion-card-subtitle>
          </ion-card-header>
        </ion-card>

        <div v-if="loading" class="loading-state">
          <ion-spinner name="crescent" />
        </div>

        <EmptyState
          v-else-if="!activePlan"
          type="no-plan"
          title="暂无生效用药计划"
          description="等待药师建立计划，或发起求助"
        />

        <EmptyState
          v-else-if="tasks.length === 0"
          type="no-records"
          title="今天没有待执行任务"
        />

        <ion-card v-for="task in tasks" :key="task.id" class="task-card">
          <ion-card-content>
            <div class="task-header">
              <div>
                <div class="task-title">{{ task.drugName }}</div>
                <div class="task-meta">{{ task.dosage }} · {{ formatTime(task.scheduledAt) }}</div>
                <div v-if="task.instructions || task.mealRelation" class="task-extra">
                  {{ task.instructions || task.mealRelation }}
                </div>
                <div
                  v-if="reminderNotice(task)"
                  class="task-reminder"
                  :class="`task-reminder--${task.reminderStage || 'none'}`"
                >
                  {{ reminderNotice(task) }}
                </div>
              </div>
              <ion-badge :color="statusColor(task.status)">{{ statusText(task.status) }}</ion-badge>
            </div>

            <div class="task-actions">
              <ion-button
                size="small"
                color="success"
                :disabled="submittingTaskId === task.id"
                @click="saveResult(task, 'TAKEN')"
              >
                已服药
              </ion-button>
              <ion-button
                size="small"
                fill="outline"
                color="warning"
                :disabled="submittingTaskId === task.id"
                @click="saveResult(task, 'MISSED')"
              >
                漏服
              </ion-button>
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
  IonBackButton,
  IonBadge,
  IonButtons,
  IonButton,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardSubtitle,
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
import { appAuthService, medicationService, type DoseTaskDTO, type MedicationPlanDTO } from '@/modules/patient/api/mvp'
import { patientSession } from '@/app/session'

const loading = ref(false)
const activePlan = ref<MedicationPlanDTO | null>(null)
const tasks = ref<DoseTaskDTO[]>([])
const currentAccountType = ref('PATIENT')
const submittingTaskId = ref<number | null>(null)
const showToast = ref(false)
const toastMessage = ref('')

const completedCount = computed(() => tasks.value.filter((task) => task.status === 'COMPLETED').length)
const pendingCount = computed(() => tasks.value.filter((task) => !['COMPLETED', 'MISSED', 'SKIPPED'].includes(task.status)).length)

const showMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}

const loadTasks = async () => {
  loading.value = true
  try {
    const [planResponse, taskResponse, accountResponse] = await Promise.all([
      medicationService.getActiveMedicationPlan(),
      medicationService.getTodayDoseTasks(),
      appAuthService.getCurrentAccount()
    ])
    activePlan.value = planResponse.data ?? null
    tasks.value = taskResponse.data ?? []
    currentAccountType.value = String((accountResponse.data as any)?.accountType || 'PATIENT').toUpperCase()
    patientSession.setActivePlanStatus(Boolean(planResponse.data))
  } catch (error: any) {
    activePlan.value = null
    tasks.value = []
    patientSession.setActivePlanStatus(false)
    showMessage(error?.message || '提醒数据加载失败')
  } finally {
    loading.value = false
  }
}

const saveResult = async (task: DoseTaskDTO, result: 'TAKEN' | 'MISSED') => {
  submittingTaskId.value = task.id
  try {
    const payload = {
      result,
      actualTakenAt: new Date().toISOString(),
      note: result === 'TAKEN' ? '患者端提交' : '',
      skipReason: result === 'MISSED' ? 'PATIENT_REPORTED' : ''
    }

    if (task.lastEffectiveCheckinId) {
      await medicationService.correctCheckin(task.lastEffectiveCheckinId, payload)
    } else {
      await medicationService.submitCheckin({ doseTaskId: task.id, ...payload })
    }

    showMessage(result === 'TAKEN' ? '已记录为已服药' : '已记录为漏服')
    await loadTasks()
  } catch (error: any) {
    showMessage(error?.message || '提交失败')
  } finally {
    submittingTaskId.value = null
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
  const map: Record<string, string> = { PENDING: '待执行', COMPLETED: '已服药', MISSED: '漏服', SKIPPED: '跳过' }
  return map[status] || status
}

const statusColor = (status: string) => {
  if (status === 'COMPLETED') return 'success'
  if (status === 'MISSED') return 'danger'
  if (status === 'SKIPPED') return 'warning'
  return 'medium'
}

const formatOverdue = (minutes?: number | null) => {
  const value = Math.max(minutes ?? 0, 0)
  if (value < 60) return `${value} 分钟`
  const hours = Math.floor(value / 60)
  const rest = value % 60
  return rest > 0 ? `${hours} 小时 ${rest} 分钟` : `${hours} 小时`
}

const reminderNotice = (task: DoseTaskDTO) => {
  if (task.reminderStage === 'FAMILY_REMINDER_DUE') {
    return currentAccountType.value === 'FAMILY'
      ? `已超过 ${formatOverdue(task.overdueMinutes)}，需要你协助记录`
      : `已超过 ${formatOverdue(task.overdueMinutes)}，已提醒家属协助`
  }
  if (task.reminderStage === 'OVERDUE') {
    return `已超过 ${formatOverdue(task.overdueMinutes)}，请尽快记录`
  }
  if (task.reminderStage === 'DUE') {
    return '已到服药时间，请及时记录'
  }
  if (task.reminderStage === 'UPCOMING') {
    return '尚未到服药时间'
  }
  return ''
}

async function handleRefresh(event: CustomEvent) {
  try {
    await loadTasks()
  } finally {
    (event.target as HTMLIonRefresherElement).complete()
  }
}

onIonViewWillEnter(loadTasks)
</script>

<style scoped>
.page-content {
  --background: var(--patient-bg-page);
}

.container {
  padding: var(--patient-space-4);
  display: flex;
  flex-direction: column;
  gap: var(--patient-space-3);
}

/* ── 概览卡片 ── */
.overview-card {
  margin: 0;
}

.overview-grid {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: var(--patient-space-4) var(--patient-space-3) var(--patient-space-3);
}

.overview-item {
  flex: 1;
  text-align: center;
}

.overview-number {
  font-size: var(--patient-font-page-title);
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

/* ── 计划卡片 ── */
.plan-card {
  margin: 0;
}

/* ── 加载状态 ── */
.loading-state {
  padding: var(--patient-space-8) var(--patient-space-4);
  text-align: center;
}

/* ── 任务卡片 ── */
.task-card {
  margin: 0;
}

.task-header {
  display: flex;
  justify-content: space-between;
  gap: var(--patient-space-3);
  align-items: flex-start;
}

.task-title {
  font-size: var(--patient-font-subtitle);
  font-weight: var(--patient-font-bold);
  color: var(--patient-text-primary);
  line-height: var(--patient-leading-tight);
}

.task-meta {
  font-size: var(--patient-font-helper);
  color: var(--patient-text-secondary);
  margin-top: var(--patient-space-1);
  line-height: var(--patient-leading-normal);
}

.task-extra {
  font-size: var(--patient-font-caption);
  color: var(--patient-text-muted);
  margin-top: var(--patient-space-1);
}

.task-reminder {
  margin-top: var(--patient-space-2);
  padding: var(--patient-space-2) var(--patient-space-3);
  border-radius: var(--patient-radius-md);
  font-size: var(--patient-font-helper);
  line-height: var(--patient-leading-normal);
  color: var(--patient-text-primary);
  background: var(--patient-bg-page);
}

.task-reminder--DUE,
.task-reminder--UPCOMING {
  color: var(--patient-text-secondary);
}

.task-reminder--OVERDUE,
.task-reminder--FAMILY_REMINDER_DUE {
  color: var(--patient-warning);
  background: color-mix(in srgb, var(--patient-warning) 12%, var(--patient-bg-card));
}

.task-actions {
  display: flex;
  gap: var(--patient-space-3);
  margin-top: var(--patient-space-4);
}
</style>
