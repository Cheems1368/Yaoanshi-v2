<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>用药</ion-title>
      </ion-toolbar>
      <ion-toolbar>
        <ion-segment v-model="activeSegment" class="med-segment">
          <ion-segment-button value="reminder">
            <ion-label>今日提醒</ion-label>
          </ion-segment-button>
          <ion-segment-button value="record">
            <ion-label>服药记录</ion-label>
          </ion-segment-button>
        </ion-segment>
      </ion-toolbar>
    </ion-header>

    <ion-content class="page-content">
      <ion-refresher slot="fixed" @ionRefresh="handleRefresh($event)">
        <ion-refresher-content></ion-refresher-content>
      </ion-refresher>

      <!-- 今日提醒 -->
      <div v-show="activeSegment === 'reminder'" class="container">
        <div v-if="offlineMode" class="offline-banner" role="status">
          当前离线，展示 48 小时内的加密缓存；新打卡将在联网后自动同步，已提交记录需联网更正。
        </div>
        <ion-card class="overview-card">
          <ion-card-content class="overview-grid">
            <div class="overview-item">
              <div class="overview-number">{{ tasks.length }}</div>
              <div class="overview-label">今日任务</div>
            </div>
            <div class="overview-item">
              <div class="overview-number">{{ reminderCompletedCount }}</div>
              <div class="overview-label">已服药</div>
            </div>
            <div class="overview-item">
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

        <div v-if="reminderLoading" class="loading-state">
          <ion-spinner name="crescent" />
        </div>

        <EmptyState
          v-else-if="!activePlan && tasks.length === 0"
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
              <div class="task-info">
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
                expand="block"
                color="success"
                :disabled="submittingTaskId === task.id"
                @click="saveResult(task, 'TAKEN')"
              >已服药</ion-button>
              <ion-button
                expand="block"
                fill="outline"
                color="warning"
                :disabled="submittingTaskId === task.id"
                @click="saveResult(task, 'MISSED')"
              >漏服</ion-button>
            </div>
          </ion-card-content>
        </ion-card>
      </div>

      <!-- 服药记录 -->
      <div v-show="activeSegment === 'record'" class="container">
        <ion-card class="overview-card">
          <ion-card-content class="overview-grid">
            <div class="overview-item">
              <div class="overview-number">{{ records.length }}</div>
              <div class="overview-label">近30天记录</div>
            </div>
            <div class="overview-item">
              <div class="overview-number">{{ effectiveCount }}</div>
              <div class="overview-label">有效记录</div>
            </div>
            <div class="overview-item">
              <div class="overview-number">{{ recordCompletedCount }}</div>
              <div class="overview-label">已服药</div>
            </div>
          </ion-card-content>
        </ion-card>

        <div v-if="recordLoading" class="loading-state">
          <ion-spinner name="crescent" />
        </div>

        <EmptyState
          v-else-if="records.length === 0"
          type="no-records"
          title="最近 30 天暂无服药记录"
        />

        <ion-card v-for="record in records" :key="record.id" class="record-card">
          <ion-card-content>
            <div class="record-header">
              <div class="record-info">
                <div class="record-title">任务 #{{ record.doseTaskId }}</div>
                <div class="record-meta">{{ formatDateTime(record.actualTakenAt || record.createdAt) }}</div>
                <div class="record-meta">提交人：{{ operatorText(record.operatorType) }}</div>
              </div>
              <ion-badge :color="resultColor(record.result)">{{ resultText(record.result) }}</ion-badge>
            </div>
            <div v-if="record.note" class="record-note">{{ record.note }}</div>
            <div v-if="record.skipReason" class="record-note">原因：{{ record.skipReason }}</div>
            <div v-if="!record.effective" class="record-note muted">已被后续更正覆盖</div>
            <div v-if="record.effective" class="record-actions">
              <ion-button size="small" color="success" fill="outline" @click="correctRecord(record, 'TAKEN')">
                更正为已服药
              </ion-button>
              <ion-button size="small" color="warning" fill="outline" @click="correctRecord(record, 'MISSED')">
                更正为漏服
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
  IonBadge,
  IonButton,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardSubtitle,
  IonCardTitle,
  IonContent,
  IonHeader,
  IonLabel,
  IonPage,
  IonRefresher,
  IonRefresherContent,
  IonSegment,
  IonSegmentButton,
  IonSpinner,
  IonTitle,
  IonToast,
  IonToolbar,
  onIonViewWillEnter
} from '@ionic/vue'
import { computed, ref } from 'vue'
import {
  appAuthService,
  medicationService,
  type CheckinRecordDTO,
  type DoseTaskDTO,
  type MedicationPlanDTO
} from '@/modules/patient/api/mvp'
import { patientSession } from '@/app/session'
import {
  loadMedicationTasks,
  submitOrQueueCheckin
} from '@/modules/patient/services/offlineMedication'
import {
  cancelMedicationReminder,
  enableAndScheduleMedicationReminders
} from '@/modules/patient/services/localMedicationReminders'

const activeSegment = ref<'reminder' | 'record'>('reminder')

// ── Reminder state ──────────────────────────────────────────────────────────
const reminderLoading = ref(false)
const activePlan = ref<MedicationPlanDTO | null>(null)
const tasks = ref<DoseTaskDTO[]>([])
const currentAccountType = ref('PATIENT')
const submittingTaskId = ref<number | null>(null)
const offlineMode = ref(false)

const reminderCompletedCount = computed(() => tasks.value.filter((t) => t.status === 'COMPLETED').length)
const pendingCount = computed(() =>
  tasks.value.filter((t) => !['COMPLETED', 'MISSED', 'SKIPPED'].includes(t.status)).length
)

const loadTasks = async () => {
  reminderLoading.value = true
  const expectedAccountSubject = patientSession.getAccountSubject()
  try {
    const [planRes, taskRes, accountRes] = await Promise.all([
      medicationService.getActiveMedicationPlan().catch(() => ({ data: null })),
      loadMedicationTasks(),
      appAuthService.getCurrentAccount().catch(() => ({ data: { accountType: 'PATIENT' } }))
    ])
    if (!expectedAccountSubject || patientSession.getAccountSubject() !== expectedAccountSubject) {
      activePlan.value = null
      tasks.value = []
      offlineMode.value = false
      return
    }
    activePlan.value = planRes.data ?? null
    const today = new Date().toLocaleDateString('sv-SE')
    tasks.value = taskRes.tasks.filter((task) => task.taskDate === today)
    offlineMode.value = taskRes.offline
    void enableAndScheduleMedicationReminders(taskRes.tasks)
      .then((mode) => { if (mode === 'inexact') showMessage('未授予精确闹钟权限，用药提醒可能延迟') })
      .catch(() => undefined)
    currentAccountType.value = String((accountRes.data as any)?.accountType || 'PATIENT').toUpperCase()
    if (!taskRes.offline) patientSession.setActivePlanStatus(Boolean(planRes.data))
  } catch {
    activePlan.value = null
    tasks.value = []
    offlineMode.value = false
    patientSession.setActivePlanStatus(false)
    showMessage('提醒数据加载失败')
  } finally {
    reminderLoading.value = false
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
      if (offlineMode.value) {
        showMessage('当前离线，已提交记录需联网后更正')
        return
      }
      await medicationService.correctCheckin(task.lastEffectiveCheckinId, payload)
    } else {
      const outcome = await submitOrQueueCheckin(task, result)
      if (outcome === 'queued') {
        task.status = result === 'TAKEN' ? 'COMPLETED' : 'MISSED'
        await cancelMedicationReminder(task.id).catch(() => undefined)
        showMessage('当前离线，记录已加密保存，将在联网后自动同步')
        return
      }
    }
    showMessage(result === 'TAKEN' ? '已记录为已服药' : '已记录为漏服')
    await loadTasks()
  } catch {
    showMessage('提交失败')
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

const statusText = (status: string) =>
  ({ PENDING: '待执行', COMPLETED: '已服药', MISSED: '漏服', SKIPPED: '跳过' })[status] ?? status

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

// ── Record state ────────────────────────────────────────────────────────────
const recordLoading = ref(false)
const records = ref<CheckinRecordDTO[]>([])

const effectiveCount = computed(() => records.value.filter((r) => r.effective).length)
const recordCompletedCount = computed(
  () => records.value.filter((r) => r.effective && r.result === 'TAKEN').length
)

const loadRecords = async () => {
  recordLoading.value = true
  try {
    const res = await medicationService.getCheckins({ pageNo: 1, pageSize: 30 })
    records.value = res.data?.list ?? []
  } catch {
    records.value = []
    showMessage('服药记录加载失败')
  } finally {
    recordLoading.value = false
  }
}

const correctRecord = async (record: CheckinRecordDTO, result: 'TAKEN' | 'MISSED') => {
  try {
    await medicationService.correctCheckin(record.id, {
      result,
      actualTakenAt: new Date().toISOString(),
      note: '记录页更正',
      skipReason: result === 'MISSED' ? 'PATIENT_REPORTED' : ''
    })
    showMessage(result === 'TAKEN' ? '已更正为已服药' : '已更正为漏服')
    await loadRecords()
  } catch {
    showMessage('更正失败')
  }
}

const formatDateTime = (value?: string | null) => {
  if (!value) return '--'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

const resultText = (result: string) =>
  ({ TAKEN: '已服药', MISSED: '漏服', SKIPPED: '跳过' })[result] ?? result

const resultColor = (result: string) => {
  if (result === 'TAKEN') return 'success'
  if (result === 'MISSED') return 'danger'
  return 'warning'
}

const operatorText = (operatorType: string) => (operatorType === 'FAMILY' ? '亲情号' : '本人')

// ── Shared ──────────────────────────────────────────────────────────────────
const showToast = ref(false)
const toastMessage = ref('')

const showMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}

const loadAll = () => Promise.all([loadTasks(), loadRecords()])

async function handleRefresh(event: CustomEvent) {
  try {
    await loadAll()
  } finally {
    (event.target as HTMLIonRefresherElement).complete()
  }
}

onIonViewWillEnter(async () => {
  await loadAll()
})
</script>

<style scoped>
.page-content {
  --background: var(--patient-bg-page);
}

.med-segment {
  padding: 0 var(--patient-space-4);
  --background: transparent;
}

.offline-banner {
  padding: var(--patient-space-3);
  border-radius: var(--patient-radius-md);
  color: var(--patient-warning-dark, #8a4b08);
  background: rgba(245, 158, 11, 0.14);
  font-size: var(--patient-font-helper);
  line-height: var(--patient-leading-normal);
}

ion-segment-button {
  font-size: var(--patient-font-body);
  font-weight: var(--patient-font-semibold);
  min-height: 48px;
}

.container {
  padding: var(--patient-space-4);
  display: flex;
  flex-direction: column;
  gap: var(--patient-space-3);
}

/* ── Overview card ── */
.overview-card {
  border-radius: var(--patient-radius-lg);
  box-shadow: var(--patient-shadow-sm);
}

.overview-grid {
  display: flex;
  justify-content: space-around;
  text-align: center;
  padding: var(--patient-space-3) 0;
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--patient-space-1);
}

.overview-number {
  font-size: var(--patient-font-page-title);
  font-weight: var(--patient-font-bold);
  line-height: var(--patient-leading-tight);
  color: var(--patient-primary);
}

.overview-label {
  font-size: var(--patient-font-helper);
  color: var(--patient-text-secondary);
}

/* ── Plan / task cards ── */
.plan-card,
.task-card,
.record-card,
.empty-card {
  border-radius: var(--patient-radius-lg);
  box-shadow: var(--patient-shadow-sm);
  margin: 0;
}

/* ── Task ── */
.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--patient-space-3);
  margin-bottom: var(--patient-space-3);
}

.task-info {
  flex: 1;
  min-width: 0;
}

.task-title {
  font-size: var(--patient-font-subtitle);
  font-weight: var(--patient-font-semibold);
  color: var(--patient-text-primary);
  line-height: var(--patient-leading-tight);
}

.task-meta {
  font-size: var(--patient-font-helper);
  color: var(--patient-text-secondary);
  margin-top: var(--patient-space-1);
}

.task-extra {
  font-size: var(--patient-font-helper);
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
  gap: var(--patient-space-2);
}

.task-actions ion-button {
  flex: 1;
  --border-radius: var(--patient-radius-md);
  height: 48px;
  font-size: var(--patient-font-body);
}

/* ── Record ── */
.record-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--patient-space-3);
  margin-bottom: var(--patient-space-2);
}

.record-info {
  flex: 1;
  min-width: 0;
}

.record-title {
  font-size: var(--patient-font-subtitle);
  font-weight: var(--patient-font-semibold);
  color: var(--patient-text-primary);
}

.record-meta {
  font-size: var(--patient-font-helper);
  color: var(--patient-text-secondary);
  margin-top: var(--patient-space-1);
}

.record-note {
  font-size: var(--patient-font-helper);
  color: var(--patient-text-secondary);
  margin-top: var(--patient-space-2);
}

.record-note.muted {
  color: var(--patient-text-muted);
}

.record-actions {
  display: flex;
  gap: var(--patient-space-2);
  margin-top: var(--patient-space-3);
}

/* ── Loading state ── */
.loading-state {
  padding: var(--patient-space-8) var(--patient-space-4);
  text-align: center;
}
</style>
