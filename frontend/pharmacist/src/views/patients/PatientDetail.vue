<template>
  <section class="page-shell">
    <header class="page-header">
      <div>
        <h2>患者详情</h2>
        <p>补全患者医学资料，保存后自动刷新最新 ABCD2 快照，并在同页维护用药计划。</p>
      </div>
      <div class="header-actions">
        <el-button class="patient-back-button" @click="goBack">返回列表</el-button>
        <el-button type="primary" class="patient-save-button" :loading="saving" @click="handleSave">保存资料</el-button>
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
          <p>绑定状态: {{ detail.bindingStatus === 'BOUND' ? '已绑定' : detail.bindingStatus }}</p>
        </article>

        <article class="summary-card">
          <span class="card-label">责任药师</span>
          <strong>{{ detail.responsiblePharmacistName }}</strong>
          <p>{{ detail.responsiblePharmacistId ? `药师档案 #${detail.responsiblePharmacistId}` : '首次保存后自动认领' }}</p>
          <p>待补全: {{ detail.pendingFields?.length ? detail.pendingFields.join('、') : '无' }}</p>
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
            <h3>医学资料</h3>
            <span>补齐本轮 MVP 所需字段</span>
          </div>

          <el-form label-position="top" :model="form" class="detail-form">
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

              <el-form-item label="脱敏身份证号">
                <el-input v-model="form.idCardMasked" placeholder="例如 310***********1234" />
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

          <el-form label-position="top" :model="form.abcd2Input" class="detail-form">
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
            <h3>依从性摘要</h3>
            <span>展示最近返回的依从性概览</span>
          </div>
          <div class="placeholder-box">
            <strong>{{ detail.adherenceSummary?.status || 'NOT_AVAILABLE' }}</strong>
            <p>7 天依从性: {{ displayAdherence(detail.adherenceSummary?.sevenDayAdherence) }}</p>
            <p>30 天依从性: {{ displayAdherence(detail.adherenceSummary?.thirtyDayAdherence) }}</p>
          </div>
        </article>
      </section>

      <section ref="planSectionRef">
        <MedicationPlanPanel
          :patient-id="detail.id"
          :latest-plan-id="detail.latestMedicationPlanId"
          @plan-updated="handlePlanUpdated"
        />
      </section>
    </template>
  </section>
</template>

<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPatientDetail, updatePatient } from '@/modules/pharmacist/api/patients'
import MedicationPlanPanel from '@/views/patients/MedicationPlanPanel.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const detail = ref(null)
const planSectionRef = ref(null)

const form = reactive({
  name: '',
  gender: '',
  birthDate: '',
  age: null,
  idCardMasked: '',
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
  form.idCardMasked = payload.idCardMasked || ''
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
      idCardMasked: form.idCardMasked,
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

const displayAdherence = (value) => (value == null ? '暂无数据' : `${value}%`)

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
