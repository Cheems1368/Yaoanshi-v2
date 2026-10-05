<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h2>患者详情</h2>
        <p>补全患者医学资料，保存后自动刷新最新 ABCD2 快照，并在同页维护用药计划。</p>
      </div>
      <div class="header-actions">
        <el-button class="patient-back-button" @click="goBack">返回列表</el-button>
        <el-button v-if="!canReassign" type="primary" class="patient-save-button" :loading="saving" @click="handleSave">保存资料</el-button>
      </div>
    </header>

    <el-alert
      v-if="errorMessage"
      type="error"
      :closable="false"
      :title="errorMessage"
      show-icon
    />

    <el-skeleton v-if="loading" :rows="8" animated />

    <template v-else-if="detail">
      <section class="summary-grid">
        <article class="summary-card">
          <span class="card-label">患者信息</span>
          <strong>{{ detail.name }}</strong>
          <p>手机号: {{ detail.mobileMasked }}</p>
          <p>院内患者 ID: {{ detail.hospitalPatientId }}</p>
          <p>绑定状态: {{ detail.bindingStatus === 'BOUND' ? '已绑定' : '待药师确认' }}</p>
          <el-button v-if="detail.bindingStatus === 'PENDING'" type="primary" :loading="confirmingBinding" @click="confirmBinding">核验并确认绑定</el-button>
        </article>

        <article class="summary-card">
          <span class="card-label">责任药师</span>
          <strong>{{ detail.responsiblePharmacistName }}</strong>
          <p>{{ detail.responsiblePharmacistId ? `药师档案 #${detail.responsiblePharmacistId}` : '首次保存后自动认领' }}</p>
          <p>待补全: {{ detail.pendingFields?.length ? detail.pendingFields.join('、') : '无' }}</p>
          <el-button v-if="canReassign" plain :loading="loadingOptions" @click="openReassign">重新分配</el-button>
        </article>

        <article class="summary-card">
          <span class="card-label">最新 ABCD2</span>
          <strong>{{ latestAbcd2Title }}</strong>
          <p>{{ latestAbcd2Meta }}</p>
          <p>{{ latestAbcd2Time }}</p>
        </article>
      </section>

      <section class="content-grid">
        <article class="panel">
          <div class="panel-header">
            <h3>知识测评</h3>
            <el-button :loading="reminding" :disabled="!detail.currentKnowledgePhase || detail[detail.currentKnowledgePhase === 'DAY_1' ? 'day1AssessmentStatus' : 'week4AssessmentStatus'] === 'COMPLETED'" @click="sendAssessmentReminder">提醒当前阶段</el-button>
          </div>
          <div class="placeholder-box">
            <p>首日：{{ assessmentSummary(detail.day1KnowledgeAssessment, detail.day1AssessmentStatus) }}</p>
            <p>第 4 周：{{ assessmentSummary(detail.week4KnowledgeAssessment, detail.week4AssessmentStatus) }}</p>
          </div>
        </article>
        <article class="panel">
          <div class="panel-header"><h3>工单满意度</h3></div>
          <div class="placeholder-box">
            <strong>{{ detail.satisfactionCount ? `${detail.satisfactionAverage?.toFixed(1)} / 5` : '暂无评分' }}</strong>
            <p>已评价工单：{{ detail.satisfactionCount || 0 }} 单</p>
          </div>
        </article>
      </section>

      <section class="content-grid">
        <article class="panel">
          <div class="panel-header">
            <h3>医学资料</h3>
            <span>补齐本轮 MVP 所需字段</span>
          </div>

          <el-form label-position="top" :model="form" :disabled="canReassign" class="detail-form">
            <div class="form-grid">
              <el-form-item label="患者姓名">
                <el-input v-model="form.name" placeholder="请输入患者姓名" />
              </el-form-item>

              <el-form-item label="性别">
                <el-select v-model="form.gender" placeholder="请选择性别">
                  <el-option label="男" value="MALE" />
                  <el-option label="女" value="FEMALE" />
                  <el-option label="其他" value="OTHER" />
                  <el-option label="未知" value="UNKNOWN" />
                </el-select>
              </el-form-item>

              <el-form-item label="出生日期">
                <el-date-picker
                  v-model="form.birthDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="请选择出生日期"
                />
              </el-form-item>

              <el-form-item label="年龄">
                <el-input-number v-model="form.age" :min="0" :max="120" controls-position="right" />
              </el-form-item>

              <el-form-item label="主要疾病编码">
                <el-input v-model="form.primaryDiseaseCode" placeholder="请输入主要疾病编码" />
              </el-form-item>

              <el-form-item label="主要疾病名称" class="patient-primary-disease">
                <el-input v-model="form.primaryDiseaseName" placeholder="请输入主要疾病名称" />
              </el-form-item>
            </div>

            <el-form-item label="药物过敏史">
              <el-input v-model="form.drugAllergy" type="textarea" :rows="3" placeholder="请输入药物过敏史" />
            </el-form-item>

            <el-form-item label="食物过敏史">
              <el-input v-model="form.foodAllergy" type="textarea" :rows="3" placeholder="请输入食物过敏史" />
            </el-form-item>

            <el-form-item label="病史补充">
              <el-input
                v-model="form.medicalHistorySummary"
                type="textarea"
                :rows="4"
                placeholder="可记录吸烟史、既往病史、随访备注等"
              />
            </el-form-item>
          </el-form>
        </article>

        <article class="panel">
          <div class="panel-header">
            <h3>ABCD2 关键信息</h3>
            <span>关键字段变化时自动生成最新快照</span>
          </div>

          <el-form label-position="top" :model="form.abcd2Input" :disabled="canReassign" class="detail-form">
            <div class="form-grid">
              <el-form-item label="是否高血压或初测血压升高">
                <el-switch v-model="form.abcd2Input.bloodPressureHigh" class="patient-bp-switch" />
              </el-form-item>

              <el-form-item label="临床表现">
                <el-select v-model="form.abcd2Input.clinicalFeature" placeholder="请选择临床表现">
                  <el-option label="单侧无力" value="UNILATERAL_WEAKNESS" />
                  <el-option label="语言障碍" value="SPEECH_IMPAIRMENT" />
                  <el-option label="其他/未记录" value="OTHER" />
                </el-select>
              </el-form-item>

              <el-form-item label="症状持续时间（分钟）">
                <el-input-number
                  v-model="form.abcd2Input.symptomDurationMinutes"
                  :min="0"
                  :max="1440"
                  controls-position="right"
                />
              </el-form-item>

              <el-form-item label="是否糖尿病">
                <el-switch v-model="form.abcd2Input.diabetes" />
              </el-form-item>
            </div>
          </el-form>

          <div class="snapshot-box" v-if="detail.latestAbcd2Assessment">
            <h4>最近一次快照</h4>
            <p>风险等级: {{ detail.latestAbcd2Assessment.riskLevel }}</p>
            <p>分数: {{ detail.latestAbcd2Assessment.score }}</p>
            <p>时间: {{ detail.latestAbcd2Assessment.calculatedAt }}</p>
          </div>
          <el-empty v-else description="暂无 ABCD2 快照" />
        </article>
      </section>

      <section class="content-grid">
        <article class="panel">
          <div class="panel-header">
            <h3>亲情号绑定</h3>
            <span>本轮只提供只读展示</span>
          </div>

          <div v-if="detail.familyBindings?.length" class="binding-list">
            <article v-for="binding in detail.familyBindings" :key="binding.id" class="binding-item">
              <strong>{{ binding.relationshipType || '未标注关系' }}</strong>
              <span>家庭账号: {{ binding.familyAccountId }}</span>
              <span>授权时间: {{ binding.authorizedAt || '未知' }}</span>
            </article>
          </div>
          <el-empty v-else description="暂无亲情号绑定" />
        </article>

        <article class="panel">
          <div class="panel-header">
            <h3>自报按时服药率</h3>
            <span>统计截止：{{ displayDate(detail.adherenceSummary?.asOf) }}</span>
          </div>
          <div class="placeholder-box">
            <p>近 7 天：{{ displayAdherence(detail.adherenceSummary?.sevenDayAdherence) }}（{{ detail.adherenceSummary?.sevenDayOnTimeTaken ?? 0 }} / {{ detail.adherenceSummary?.sevenDayDueTasks ?? 0 }}）</p>
            <p>近 30 天：{{ displayAdherence(detail.adherenceSummary?.thirtyDayAdherence) }}（{{ detail.adherenceSummary?.thirtyDayOnTimeTaken ?? 0 }} / {{ detail.adherenceSummary?.thirtyDayDueTasks ?? 0 }}）</p>
            <p>提前超过 60 分钟：近 7 天 {{ detail.adherenceSummary?.sevenDayEarlyOver60Minutes ?? 0 }} 次；近 30 天 {{ detail.adherenceSummary?.thirtyDayEarlyOver60Minutes ?? 0 }} 次</p>
            <p>尚处于按时窗口：近 7 天 {{ detail.adherenceSummary?.sevenDayPendingOnTimeWindow ?? 0 }} 次；近 30 天 {{ detail.adherenceSummary?.thirtyDayPendingOnTimeWindow ?? 0 }} 次</p>
            <el-button :loading="exportingAdherence" @click="exportAdherence(7)">导出近 7 天 CSV</el-button>
            <el-button :loading="exportingAdherence" @click="exportAdherence(30)">导出近 30 天 CSV</el-button>
          </div>
        </article>
      </section>

      <section ref="planSectionRef">
        <MedicationPlanPanel
          :patient-id="detail.id"
          :latest-plan-id="detail.latestMedicationPlanId"
          :read-only="canReassign"
          @plan-updated="handlePlanUpdated"
        />
      </section>
    </template>

    <el-dialog v-model="reassignVisible" title="重新分配责任药师" width="480px">
      <el-form label-position="top">
        <el-form-item label="目标药师">
          <el-select v-model="reassignForm.pharmacistId" placeholder="请选择在职药师" style="width: 100%">
            <el-option v-for="item in pharmacistOptions" :key="item.id" :label="item.realName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分配原因">
          <el-input v-model="reassignForm.reason" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reassignVisible = false">取消</el-button>
        <el-button type="primary" :loading="reassigning" @click="submitReassign">确认分配</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { confirmPatientBinding, downloadPatientAdherence, getPatientDetail, getPharmacistOptions, reassignPatient, remindKnowledgeAssessment, updatePatient } from '@/modules/pharmacist/api/patients'
import { useUserStore } from '@/store/user'
import MedicationPlanPanel from '@/views/patients/MedicationPlanPanel.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const confirmingBinding = ref(false)
const errorMessage = ref('')
const detail = ref(null)
const planSectionRef = ref(null)
const reminding = ref(false)
const exportingAdherence = ref(false)
const loadingOptions = ref(false)
const reassigning = ref(false)
const reassignVisible = ref(false)
const pharmacistOptions = ref([])
const reassignForm = reactive({ pharmacistId: null, reason: '' })

const form = reactive({
  name: '',
  gender: '',
  birthDate: '',
  age: null,
  primaryDiseaseCode: '',
  primaryDiseaseName: '',
  drugAllergy: '',
  foodAllergy: '',
  medicalHistorySummary: '',
  abcd2Input: {
    bloodPressureHigh: false,
    clinicalFeature: 'OTHER',
    symptomDurationMinutes: 0,
    diabetes: false
  }
})

const patientId = computed(() => route.query.id)
const confirmBinding = async () => {
  const id = patientId.value
  try {
    await ElMessageBox.confirm(`请核验 ${detail.value.name} 的手机号与院内患者 ID ${detail.value.hospitalPatientId}。确认后您将成为责任药师。`, '确认患者身份', { type: 'warning' })
    if (patientId.value !== id) return
    confirmingBinding.value = true
    const response = await confirmPatientBinding(id)
    if (patientId.value === id) detail.value = response.data
    ElMessage.success('患者绑定已确认')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error?.message || '绑定确认失败')
  } finally {
    confirmingBinding.value = false
  }
}
const canReassign = computed(() => userStore.userInfo.roles.some((role) =>
  ['admin', 'super_admin', 'tenant_admin'].includes(String(role).toLowerCase())
))
const assessmentSummary = (assessment, status) => assessment
  ? `已完成 · ${assessment.score} / ${assessment.maxScore} 分 · ${assessment.submittedAt || '已提交'}`
  : status === 'NOT_DUE' ? '未到期' : '未提交'

const exportAdherence = async (days) => {
  const id = patientId.value
  exportingAdherence.value = true
  try {
    const blob = await downloadPatientAdherence(id, days)
    if (patientId.value !== id) return
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `patient-adherence-${id}-${days}d.csv`
    link.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    errorMessage.value = error.message || '自报按时服药率导出失败'
  } finally {
    exportingAdherence.value = false
  }
}

const sendAssessmentReminder = async () => {
  const id = patientId.value
  reminding.value = true
  try {
    await remindKnowledgeAssessment(id)
    if (patientId.value === id) ElMessage.success('测评提醒已发送')
  } catch (error) {
    if (patientId.value === id) errorMessage.value = error.message || '测评提醒发送失败'
  } finally {
    reminding.value = false
  }
}

const openReassign = async () => {
  if (!canReassign.value) return
  loadingOptions.value = true
  try {
    const response = await getPharmacistOptions()
    pharmacistOptions.value = response.data || []
    reassignForm.pharmacistId = detail.value?.responsiblePharmacistId || null
    reassignForm.reason = ''
    reassignVisible.value = true
  } catch (error) {
    errorMessage.value = error.message || '药师列表加载失败'
  } finally {
    loadingOptions.value = false
  }
}

const submitReassign = async () => {
  if (!reassignForm.pharmacistId || !reassignForm.reason.trim()) {
    ElMessage.warning('请选择药师并填写分配原因')
    return
  }
  const id = patientId.value
  reassigning.value = true
  try {
    const response = await reassignPatient(id, {
      pharmacistId: reassignForm.pharmacistId,
      reason: reassignForm.reason.trim()
    })
    if (patientId.value !== id) return
    detail.value = response.data
    hydrateForm(response.data)
    reassignVisible.value = false
    ElMessage.success('责任药师已更新')
  } catch (error) {
    if (patientId.value === id) errorMessage.value = error.message || '重新分配失败'
  } finally {
    reassigning.value = false
  }
}

const latestAbcd2Title = computed(() => {
  const latest = detail.value?.latestAbcd2Assessment
  return latest ? `${latest.riskLevel} / ${latest.score} 分` : '暂无快照'
})

const latestAbcd2Meta = computed(() => {
  const latest = detail.value?.latestAbcd2Assessment
  return latest ? '最近一次自动评估结果' : '保存 ABCD2 关键信息后自动生成'
})

const latestAbcd2Time = computed(() => detail.value?.latestAbcd2Assessment?.calculatedAt || '')

const hydrateForm = (payload) => {
  form.name = payload.name || ''
  form.gender = payload.gender || ''
  form.birthDate = payload.birthDate || ''
  form.age = payload.age ?? null
  form.primaryDiseaseCode = payload.primaryDiseaseCode || ''
  form.primaryDiseaseName = payload.primaryDiseaseName || ''
  form.drugAllergy = payload.drugAllergy || ''
  form.foodAllergy = payload.foodAllergy || ''
  form.medicalHistorySummary = payload.medicalHistory?.summary || ''
  form.abcd2Input = {
    bloodPressureHigh: Boolean(payload.abcd2Input?.bloodPressureHigh),
    clinicalFeature: payload.abcd2Input?.clinicalFeature || 'OTHER',
    symptomDurationMinutes: payload.abcd2Input?.symptomDurationMinutes ?? 0,
    diabetes: Boolean(payload.abcd2Input?.diabetes)
  }
}

const loadDetail = async () => {
  if (!patientId.value) {
    errorMessage.value = '缺少患者 ID，无法加载详情'
    detail.value = null
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    const response = await getPatientDetail(patientId.value)
    detail.value = response.data
    hydrateForm(response.data)
  } catch (error) {
    detail.value = null
    errorMessage.value = error.message || '患者详情加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!patientId.value) {
    return
  }

  saving.value = true
  try {
    const response = await updatePatient(patientId.value, {
      name: form.name,
      gender: form.gender,
      birthDate: form.birthDate || null,
      age: form.age,
      primaryDiseaseCode: form.primaryDiseaseCode,
      primaryDiseaseName: form.primaryDiseaseName,
      drugAllergy: form.drugAllergy,
      foodAllergy: form.foodAllergy,
      medicalHistory: {
        summary: form.medicalHistorySummary
      },
      abcd2Input: {
        bloodPressureHigh: form.abcd2Input.bloodPressureHigh,
        clinicalFeature: form.abcd2Input.clinicalFeature,
        symptomDurationMinutes: form.abcd2Input.symptomDurationMinutes,
        diabetes: form.abcd2Input.diabetes
      }
    })
    detail.value = response.data
    hydrateForm(response.data)
    ElMessage.success('患者资料已保存')
  } catch (error) {
    errorMessage.value = error.message || '患者资料保存失败，请稍后重试'
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  router.push('/patient/list')
}

const displayAdherence = (value) => (value == null ? '暂无数据' : `${value.toFixed(2)}%`)
const displayDate = (value) => value
  ? new Date(value).toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai', hour12: false })
  : '--'

const handlePlanUpdated = (plan) => {
  if (!detail.value || !plan) {
    return
  }
  detail.value.latestMedicationPlanId = plan.id
  detail.value.latestMedicationPlanName = plan.planName
  detail.value.latestMedicationPlanStatus = plan.status
  detail.value.latestMedicationPlanVersionNo = plan.versionNo
  detail.value.latestMedicationPlanConfirmedAt = plan.confirmedAt
}

const maybeScrollToPlan = async () => {
  if (route.query.focus !== 'plan') {
    return
  }
  await nextTick()
  planSectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

watch(
  patientId,
  async () => {
    await loadDetail()
    await maybeScrollToPlan()
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.page-shell {
  display: grid;
  gap: 24px;
}

.page-header,
.header-actions,
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-header h2,
.panel-header h3,
.summary-card strong,
.binding-item strong,
.placeholder-box strong {
  margin: 0;
  color: var(--pro-text-primary);
}

.page-header p,
.panel-header span,
.summary-card p,
.binding-item span,
.placeholder-box p,
.card-label {
  margin: 0;
  color: var(--pro-text-secondary);
}

.summary-grid,
.content-grid,
.form-grid {
  display: grid;
  gap: 20px;
}

.summary-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.content-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
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

.card-label {
  font-size: 12px;
  text-transform: uppercase;
}

.detail-form {
  margin-top: 16px;
}

.form-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.binding-list {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.binding-item,
.snapshot-box,
.placeholder-box {
  display: grid;
  gap: 8px;
  padding: 16px;
  border-radius: var(--pro-radius-md);
  background: var(--pro-bg-secondary);
}

@media (max-width: 1100px) {
  .summary-grid,
  .content-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-header,
  .header-actions,
  .panel-header {
    display: grid;
  }
}
</style>
