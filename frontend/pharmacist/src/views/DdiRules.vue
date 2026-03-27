<template>
  <section class="page-shell pro-page-shell">
    <header class="page-header pro-page-header">
      <div class="pro-page-header__copy">
        <h2>DDI 规则管理</h2>
        <p>维护药物相互作用与禁忌规则，新增或编辑后会立即作用于后续筛查与审核流程。</p>
      </div>
    </header>

    <section class="panel pro-table-panel">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="DDI 规则" name="rules">
          <div class="query-bar pro-toolbar">
            <el-input
              v-model="ruleQuery.keyword"
              clearable
              placeholder="搜索规则编码 / 药品名称 / 提示语"
              class="query-input pro-toolbar__grow"
              @keyup.enter="handleRuleSearch"
            />
            <el-select v-model="ruleQuery.severity" clearable placeholder="全部风险等级" class="query-select">
              <el-option v-for="item in severityOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <div class="toolbar-actions pro-toolbar__actions">
              <el-button type="primary" @click="handleRuleSearch">查询</el-button>
              <el-button @click="loadRules">刷新</el-button>
              <el-button type="success" @click="openRuleDrawer()">新增规则</el-button>
            </div>
          </div>

          <el-alert v-if="ruleError" :closable="false" type="error" :title="ruleError" show-icon />

          <div class="table-scroll pro-table-scroll">
            <el-table v-loading="ruleLoading" :data="rules" border table-layout="auto" empty-text="暂无 DDI 规则">
              <el-table-column prop="ruleCode" label="规则编码" min-width="220" show-overflow-tooltip />
              <el-table-column prop="drugAName" label="药品 A" min-width="180" show-overflow-tooltip />
              <el-table-column prop="drugBName" label="药品 B" min-width="180" show-overflow-tooltip />
              <el-table-column prop="severity" label="风险等级" width="120">
                <template #default="{ row }">
                  <el-tag :type="severityTagType(row.severity)" effect="plain">{{ row.severity }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" effect="plain">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="message" label="提示语" min-width="280" show-overflow-tooltip />
              <el-table-column prop="updatedAt" label="更新时间" min-width="180">
                <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openRuleDrawer(row)">编辑</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="pagination-row pro-pagination-row">
            <el-pagination
              background
              layout="total, prev, pager, next"
              :current-page="ruleQuery.pageNo"
              :page-size="ruleQuery.pageSize"
              :total="ruleTotal"
              @current-change="handleRulePageChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="禁忌规则" name="contraindications">
          <div class="query-bar pro-toolbar">
            <el-input
              v-model="contraindicationQuery.keyword"
              clearable
              placeholder="搜索规则编码 / 药品名称 / 条件值 / 提示语"
              class="query-input pro-toolbar__grow"
              @keyup.enter="handleContraindicationSearch"
            />
            <el-select
              v-model="contraindicationQuery.conditionType"
              clearable
              placeholder="全部条件类型"
              class="query-select"
            >
              <el-option v-for="item in conditionTypeOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <el-select
              v-model="contraindicationQuery.severity"
              clearable
              placeholder="全部风险等级"
              class="query-select"
            >
              <el-option v-for="item in severityOptions" :key="item" :label="item" :value="item" />
            </el-select>
            <div class="toolbar-actions pro-toolbar__actions">
              <el-button type="primary" @click="handleContraindicationSearch">查询</el-button>
              <el-button @click="loadContraindications">刷新</el-button>
              <el-button type="success" @click="openContraindicationDrawer()">新增规则</el-button>
            </div>
          </div>

          <el-alert
            v-if="contraindicationError"
            :closable="false"
            type="error"
            :title="contraindicationError"
            show-icon
          />

          <div class="table-scroll pro-table-scroll">
            <el-table
              v-loading="contraindicationLoading"
              :data="contraindications"
              border
              table-layout="auto"
              empty-text="暂无禁忌规则"
            >
              <el-table-column prop="ruleCode" label="规则编码" min-width="220" show-overflow-tooltip />
              <el-table-column prop="drugName" label="药品名称" min-width="180" show-overflow-tooltip />
              <el-table-column prop="conditionType" label="条件类型" width="150" />
              <el-table-column prop="conditionValue" label="条件值" min-width="220" show-overflow-tooltip />
              <el-table-column prop="severity" label="风险等级" width="120">
                <template #default="{ row }">
                  <el-tag :type="severityTagType(row.severity)" effect="plain">{{ row.severity }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" effect="plain">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="message" label="提示语" min-width="260" show-overflow-tooltip />
              <el-table-column prop="updatedAt" label="更新时间" min-width="180">
                <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="openContraindicationDrawer(row)">编辑</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="pagination-row pro-pagination-row">
            <el-pagination
              background
              layout="total, prev, pager, next"
              :current-page="contraindicationQuery.pageNo"
              :page-size="contraindicationQuery.pageSize"
              :total="contraindicationTotal"
              @current-change="handleContraindicationPageChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-drawer
      v-model="ruleDrawerVisible"
      size="640px"
      :title="ruleDrawerMode === 'create' ? '新增 DDI 规则' : '编辑 DDI 规则'"
      @closed="resetRuleForm"
    >
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="ruleFormRules" label-width="100px">
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input v-model="ruleForm.ruleCode" :disabled="ruleDrawerMode !== 'create'" />
        </el-form-item>
        <el-form-item label="药品 A" prop="drugAName">
          <el-autocomplete
            v-model="ruleForm.drugAName"
            :fetch-suggestions="queryDrugSuggestions"
            placeholder="输入中文名称或拼音首字母（如 aspl）"
            class="full-width"
            clearable
          />
        </el-form-item>
        <el-form-item label="药品 B" prop="drugBName">
          <el-autocomplete
            v-model="ruleForm.drugBName"
            :fetch-suggestions="queryDrugSuggestions"
            placeholder="输入中文名称或拼音首字母（如 hfl）"
            class="full-width"
            clearable
          />
        </el-form-item>
        <el-form-item label="风险等级" prop="severity">
          <el-select v-model="ruleForm.severity" class="full-width">
            <el-option v-for="item in severityOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="ruleForm.status" class="full-width">
            <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="提示语" prop="message">
          <el-input v-model="ruleForm.message" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="建议动作" prop="recommendedAction">
          <el-input v-model="ruleForm.recommendedAction" type="textarea" :rows="2" />
        </el-form-item>
        <el-divider content-position="left">扩展信息（药师专业参考）</el-divider>
        <el-form-item label="机制说明">
          <el-input v-model="ruleForm.mechanismDesc" type="textarea" :rows="3" placeholder="相互作用机制（可选）" />
        </el-form-item>
        <el-form-item label="替代建议">
          <el-input v-model="ruleForm.alternativeRecommendation" type="textarea" :rows="2" placeholder="可替代用药方案（可选）" />
        </el-form-item>
        <el-form-item label="参考来源">
          <el-input v-model="ruleForm.referenceSource" placeholder="如：《临床药理学》第3版第12章（可选）" />
        </el-form-item>
        <el-form-item label="患者摘要">
          <el-input v-model="ruleForm.patientSummary" placeholder="患者侧简短提示，留空则使用提示语（可选）" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="ruleDrawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="ruleSubmitting" @click="submitRuleForm">保存</el-button>
        </div>
      </template>
    </el-drawer>

    <el-drawer
      v-model="contraindicationDrawerVisible"
      size="640px"
      :title="contraindicationDrawerMode === 'create' ? '新增禁忌规则' : '编辑禁忌规则'"
      @closed="resetContraindicationForm"
    >
      <el-form
        ref="contraindicationFormRef"
        :model="contraindicationForm"
        :rules="contraindicationFormRules"
        label-width="100px"
      >
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input
            v-model="contraindicationForm.ruleCode"
            :disabled="contraindicationDrawerMode !== 'create'"
          />
        </el-form-item>
        <el-form-item label="药品名称" prop="drugName">
          <el-autocomplete
            v-model="contraindicationForm.drugName"
            :fetch-suggestions="queryDrugSuggestions"
            placeholder="输入中文名称或拼音首字母（如 aspl）"
            class="full-width"
            clearable
          />
        </el-form-item>
        <el-form-item label="条件类型" prop="conditionType">
          <el-select v-model="contraindicationForm.conditionType" class="full-width">
            <el-option v-for="item in conditionTypeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="条件值" prop="conditionValue">
          <el-input v-model="contraindicationForm.conditionValue" />
        </el-form-item>
        <el-form-item label="风险等级" prop="severity">
          <el-select v-model="contraindicationForm.severity" class="full-width">
            <el-option v-for="item in severityOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="contraindicationForm.status" class="full-width">
            <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="提示语" prop="message">
          <el-input v-model="contraindicationForm.message" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="建议动作" prop="recommendedAction">
          <el-input v-model="contraindicationForm.recommendedAction" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="contraindicationDrawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="contraindicationSubmitting" @click="submitContraindicationForm">
            保存
          </el-button>
        </div>
      </template>
    </el-drawer>
  </section>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createContraindication,
  createDdiRule,
  getContraindications,
  getDdiRules,
  updateContraindication,
  updateDdiRule
} from '@/modules/pharmacist/api/ddi'
import { searchDrugs } from '@/modules/pharmacist/api/drugs'

const severityOptions = ['HIGH', 'MEDIUM', 'LOW']
const statusOptions = ['ACTIVE', 'INACTIVE']
const conditionTypeOptions = ['DISEASE', 'ALLERGY', 'ORGAN_FUNCTION']

const activeTab = ref('rules')

const ruleLoading = ref(false)
const ruleError = ref('')
const rules = ref([])
const ruleTotal = ref(0)
const rulesLoaded = ref(false)

const contraindicationLoading = ref(false)
const contraindicationError = ref('')
const contraindications = ref([])
const contraindicationTotal = ref(0)
const contraindicationsLoaded = ref(false)

const ruleDrawerVisible = ref(false)
const ruleDrawerMode = ref('create')
const ruleSubmitting = ref(false)
const ruleFormRef = ref()

const contraindicationDrawerVisible = ref(false)
const contraindicationDrawerMode = ref('create')
const contraindicationSubmitting = ref(false)
const contraindicationFormRef = ref()

const ruleQuery = reactive({
  keyword: '',
  severity: '',
  pageNo: 1,
  pageSize: 10
})

const contraindicationQuery = reactive({
  keyword: '',
  severity: '',
  conditionType: '',
  pageNo: 1,
  pageSize: 10
})

const createDefaultRuleForm = () => ({
  id: null,
  ruleCode: '',
  drugAName: '',
  drugBName: '',
  severity: 'HIGH',
  message: '',
  recommendedAction: '',
  mechanismDesc: '',
  alternativeRecommendation: '',
  referenceSource: '',
  patientSummary: '',
  status: 'ACTIVE'
})

const createDefaultContraindicationForm = () => ({
  id: null,
  ruleCode: '',
  drugName: '',
  conditionType: 'DISEASE',
  conditionValue: '',
  severity: 'HIGH',
  message: '',
  recommendedAction: '',
  status: 'ACTIVE'
})

const ruleForm = reactive(createDefaultRuleForm())
const contraindicationForm = reactive(createDefaultContraindicationForm())

const ruleFormRules = {
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  drugAName: [{ required: true, message: '请输入药品 A', trigger: 'blur' }],
  drugBName: [{ required: true, message: '请输入药品 B', trigger: 'blur' }],
  severity: [{ required: true, message: '请选择风险等级', trigger: 'change' }],
  message: [{ required: true, message: '请输入提示语', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const contraindicationFormRules = {
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  drugName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
  conditionType: [{ required: true, message: '请选择条件类型', trigger: 'change' }],
  conditionValue: [{ required: true, message: '请输入条件值', trigger: 'blur' }],
  severity: [{ required: true, message: '请选择风险等级', trigger: 'change' }],
  message: [{ required: true, message: '请输入提示语', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const loadRules = async () => {
  ruleLoading.value = true
  ruleError.value = ''
  try {
    const response = await getDdiRules({
      keyword: ruleQuery.keyword || undefined,
      severity: ruleQuery.severity || undefined,
      pageNo: ruleQuery.pageNo,
      pageSize: ruleQuery.pageSize
    })
    rules.value = response.data?.list || []
    ruleTotal.value = response.data?.total || 0
    rulesLoaded.value = true
  } catch (error) {
    rules.value = []
    ruleTotal.value = 0
    ruleError.value = error.message || 'DDI 规则加载失败，请稍后重试'
  } finally {
    ruleLoading.value = false
  }
}

const loadContraindications = async () => {
  contraindicationLoading.value = true
  contraindicationError.value = ''
  try {
    const response = await getContraindications({
      keyword: contraindicationQuery.keyword || undefined,
      severity: contraindicationQuery.severity || undefined,
      conditionType: contraindicationQuery.conditionType || undefined,
      pageNo: contraindicationQuery.pageNo,
      pageSize: contraindicationQuery.pageSize
    })
    contraindications.value = response.data?.list || []
    contraindicationTotal.value = response.data?.total || 0
    contraindicationsLoaded.value = true
  } catch (error) {
    contraindications.value = []
    contraindicationTotal.value = 0
    contraindicationError.value = error.message || '禁忌规则加载失败，请稍后重试'
  } finally {
    contraindicationLoading.value = false
  }
}

const handleRuleSearch = () => {
  ruleQuery.pageNo = 1
  loadRules()
}

const handleContraindicationSearch = () => {
  contraindicationQuery.pageNo = 1
  loadContraindications()
}

const handleRulePageChange = (page) => {
  ruleQuery.pageNo = page
  loadRules()
}

const handleContraindicationPageChange = (page) => {
  contraindicationQuery.pageNo = page
  loadContraindications()
}

const resetRuleForm = () => {
  Object.assign(ruleForm, createDefaultRuleForm())
  ruleDrawerMode.value = 'create'
  ruleFormRef.value?.clearValidate()
}

const resetContraindicationForm = () => {
  Object.assign(contraindicationForm, createDefaultContraindicationForm())
  contraindicationDrawerMode.value = 'create'
  contraindicationFormRef.value?.clearValidate()
}

const openRuleDrawer = (row = null) => {
  resetRuleForm()
  if (row) {
    ruleDrawerMode.value = 'edit'
    Object.assign(ruleForm, {
      id: row.id,
      ruleCode: row.ruleCode || '',
      drugAName: row.drugAName || '',
      drugBName: row.drugBName || '',
      severity: row.severity || 'HIGH',
      message: row.message || '',
      recommendedAction: row.recommendedAction || '',
      mechanismDesc: row.mechanismDesc || '',
      alternativeRecommendation: row.alternativeRecommendation || '',
      referenceSource: row.referenceSource || '',
      patientSummary: row.patientSummary || '',
      status: row.status || 'ACTIVE'
    })
  }
  ruleDrawerVisible.value = true
}

const openContraindicationDrawer = (row = null) => {
  resetContraindicationForm()
  if (row) {
    contraindicationDrawerMode.value = 'edit'
    Object.assign(contraindicationForm, {
      id: row.id,
      ruleCode: row.ruleCode || '',
      drugName: row.drugName || '',
      conditionType: row.conditionType || 'DISEASE',
      conditionValue: row.conditionValue || '',
      severity: row.severity || 'HIGH',
      message: row.message || '',
      recommendedAction: row.recommendedAction || '',
      status: row.status || 'ACTIVE'
    })
  }
  contraindicationDrawerVisible.value = true
}

const submitRuleForm = async () => {
  try {
    await ruleFormRef.value?.validate()
  } catch {
    return
  }
  ruleSubmitting.value = true
  try {
    const payload = {
      ruleCode: ruleForm.ruleCode,
      drugAName: ruleForm.drugAName,
      drugBName: ruleForm.drugBName,
      severity: ruleForm.severity,
      message: ruleForm.message,
      recommendedAction: ruleForm.recommendedAction,
      mechanismDesc: ruleForm.mechanismDesc || undefined,
      alternativeRecommendation: ruleForm.alternativeRecommendation || undefined,
      referenceSource: ruleForm.referenceSource || undefined,
      patientSummary: ruleForm.patientSummary || undefined,
      status: ruleForm.status
    }

    if (ruleDrawerMode.value === 'create') {
      await createDdiRule(payload)
      ElMessage.success('DDI 规则已新增')
    } else {
      await updateDdiRule(ruleForm.id, payload)
      ElMessage.success('DDI 规则已更新')
    }

    ruleDrawerVisible.value = false
    await loadRules()
  } catch (error) {
    ElMessage.error(error.message || 'DDI 规则保存失败，请稍后重试')
  } finally {
    ruleSubmitting.value = false
  }
}

const submitContraindicationForm = async () => {
  try {
    await contraindicationFormRef.value?.validate()
  } catch {
    return
  }
  contraindicationSubmitting.value = true
  try {
    const payload = {
      ruleCode: contraindicationForm.ruleCode,
      drugName: contraindicationForm.drugName,
      conditionType: contraindicationForm.conditionType,
      conditionValue: contraindicationForm.conditionValue,
      severity: contraindicationForm.severity,
      message: contraindicationForm.message,
      recommendedAction: contraindicationForm.recommendedAction,
      status: contraindicationForm.status
    }

    if (contraindicationDrawerMode.value === 'create') {
      await createContraindication(payload)
      ElMessage.success('禁忌规则已新增')
    } else {
      await updateContraindication(contraindicationForm.id, payload)
      ElMessage.success('禁忌规则已更新')
    }

    contraindicationDrawerVisible.value = false
    await loadContraindications()
  } catch (error) {
    ElMessage.error(error.message || '禁忌规则保存失败，请稍后重试')
  } finally {
    contraindicationSubmitting.value = false
  }
}

const queryDrugSuggestions = async (queryString, callback) => {
  if (!queryString || !queryString.trim()) {
    callback([])
    return
  }
  try {
    const res = await searchDrugs(queryString.trim(), 10)
    const items = (res.data || []).map((d) => ({ value: d.nameCn, category: d.category }))
    callback(items)
  } catch {
    callback([])
  }
}

const severityTagType = (severity) => {
  if (severity === 'HIGH') return 'danger'
  if (severity === 'MEDIUM') return 'warning'
  return 'info'
}

const formatDate = (value) => {
  if (!value) return '--'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('zh-CN', { hour12: false })
}

watch(
  activeTab,
  (tab) => {
    if (tab === 'rules' && !rulesLoaded.value) {
      loadRules()
      return
    }
    if (tab === 'contraindications' && !contraindicationsLoaded.value) {
      loadContraindications()
    }
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.query-bar {
  margin-bottom: 16px;
}

.query-select {
  width: 180px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.full-width {
  width: 100%;
}

@media (max-width: 1024px) {
  .query-select {
    width: 100%;
  }
}
</style>
