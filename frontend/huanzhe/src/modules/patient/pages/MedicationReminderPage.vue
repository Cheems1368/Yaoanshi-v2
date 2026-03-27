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
            <div>
              <div class="overview-number">{{ tasks.length }}</div>
              <div class="overview-label">今日任务</div>
            </div>
            <div>
              <div class="overview-number">{{ completedCount }}</div>
              <div class="overview-label">已服药</div>
            </div>
            <div>
              <div class="overview-number">{{ pendingCount }}</div>
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

        <div v-if="loading" class="empty-state">
          <ion-spinner name="crescent" />
        </div>

        <ion-card v-else-if="!activePlan" class="empty-card">
          <ion-card-content class="empty-state">
            <ion-icon :icon="documentTextOutline" class="empty-icon" />
            <p class="empty-text">暂无生效用药计划，等待药师建计划</p>
          </ion-card-content>
        </ion-card>

        <ion-card v-else-if="tasks.length === 0" class="empty-card">
          <ion-card-content class="empty-state">
            <ion-icon :icon="checkmarkCircleOutline" class="empty-icon" />
            <p class="empty-text">今天没有待执行任务</p>
          </ion-card-content>
        </ion-card>

        <ion-card v-for="task in tasks" :key="task.id" class="task-card">
          <ion-card-content>
            <div class="task-header">
              <div>
                <div class="task-title">{{ task.drugName }}</div>
                <div class="task-meta">{{ task.dosage }} · {{ formatTime(task.scheduledAt) }}</div>
                <div v-if="task.instructions || task.mealRelation" class="task-extra">
                  {{ task.instructions || task.mealRelation }}
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
  IonIcon,
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
import { checkmarkCircleOutline, documentTextOutline } from 'ionicons/icons'
import { medicationService, type DoseTaskDTO, type MedicationPlanDTO } from '@/modules/patient/api/mvp'
import { patientSession } from '@/app/session'

const loading = ref(false)
const activePlan = ref<MedicationPlanDTO | null>(null)
const tasks = ref<DoseTaskDTO[]>([])
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
    const [planResponse, taskResponse] = await Promise.all([
      medicationService.getActiveMedicationPlan(),
      medicationService.getTodayDoseTasks()
    ])
    activePlan.value = planResponse.data ?? null
    tasks.value = taskResponse.data ?? []
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
      await medicationService.submitCheckin({
        doseTaskId: task.id,
        ...payload
      })
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
  const map: Record<string, string> = {
    PENDING: '待执行',
    COMPLETED: '已服药',
    MISSED: '漏服',
    SKIPPED: '跳过'
  }
  return map[status] || status
}

const statusColor = (status: string) => {
  if (status === 'COMPLETED') return 'success'
  if (status === 'MISSED') return 'danger'
  if (status === 'SKIPPED') return 'warning'
  return 'medium'
}

async function handleRefresh(event: CustomEvent) {
  try {
    await loadTasks()
  } finally {
    ;(event.target as HTMLIonRefresherElement).complete()
  }
}

onIonViewWillEnter(loadTasks)
</script>

<style scoped>
.page-content {
  --background: var(--patient-bg-page);
}

.container {
  padding: 16px;
}

.overview-card,
.plan-card,
.task-card,
.empty-card {
  border-radius: 18px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}

.overview-grid {
  display: flex;
  justify-content: space-between;
  text-align: center;
}

.overview-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--patient-primary);
}

.overview-label,
.task-meta,
.task-extra,
.empty-text {
  color: var(--patient-text-secondary);
}

.task-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.task-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--patient-text-primary);
}

.task-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
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
