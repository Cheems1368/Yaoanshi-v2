<template>
  <section class="page-shell pro-page-shell">
    <header class="page-header pro-page-header">
      <div class="pro-page-header__copy">
        <h2>患者管理</h2>
        <p>查看已绑定或待完善资料的患者，并进入详情页补全医学信息与计划维护。</p>
      </div>
      <div class="search-bar pro-toolbar">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="搜索姓名 / 手机号 / 院内患者 ID"
          class="search-input pro-toolbar__grow"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" class="patient-search-button" @click="handleSearch">查询</el-button>
      </div>
    </header>

    <el-alert
      v-if="errorMessage"
      type="error"
      :closable="false"
      :title="errorMessage"
      show-icon
      class="state-banner"
    />

    <section class="table-panel pro-table-panel">
      <div class="table-scroll pro-table-scroll">
        <el-table
          v-loading="loading"
          :data="patients"
          border
          table-layout="auto"
          empty-text="暂无符合条件的患者"
        >
          <el-table-column prop="name" label="患者姓名" min-width="140" show-overflow-tooltip />
          <el-table-column prop="mobileMasked" label="手机号" min-width="140" show-overflow-tooltip />
          <el-table-column prop="hospitalPatientId" label="院内患者 ID" min-width="180" show-overflow-tooltip />
          <el-table-column prop="bindingStatus" label="绑定状态" min-width="110">
            <template #default="{ row }">
              <el-tag :type="row.bindingStatus === 'BOUND' ? 'success' : 'info'" effect="plain">
                {{ row.bindingStatus === 'BOUND' ? '已绑定' : row.bindingStatus }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="primaryDiseaseName" label="主要疾病" min-width="180" show-overflow-tooltip />
          <el-table-column
            prop="responsiblePharmacistName"
            label="责任药师"
            min-width="140"
            show-overflow-tooltip
          />
          <el-table-column prop="familyBindingCount" label="亲情号数" width="100" />
          <el-table-column prop="pendingFieldsSummary" label="待补全信息" min-width="240" show-overflow-tooltip />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" class="patient-detail-button" @click="openDetail(row.id)">
                进入详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-row pro-pagination-row">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </section>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getPatients } from '@/modules/pharmacist/api/patients'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const total = ref(0)
const patients = ref([])

const query = reactive({
  keyword: '',
  pageNo: 1,
  pageSize: 10
})

const loadPatients = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const response = await getPatients({
      keyword: query.keyword || undefined,
      pageNo: query.pageNo,
      pageSize: query.pageSize
    })
    const page = response.data || {}
    patients.value = page.list || []
    total.value = page.total || 0
  } catch (error) {
    patients.value = []
    total.value = 0
    errorMessage.value = error.message || '患者列表加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNo = 1
  loadPatients()
}

const handlePageChange = (pageNo) => {
  query.pageNo = pageNo
  loadPatients()
}

const openDetail = (id) => {
  router.push({ path: '/patient/info', query: { id: String(id) } })
}

onMounted(loadPatients)
</script>

<style lang="scss" scoped>
.search-bar {
  flex: 0 1 520px;
  justify-content: flex-end;
  flex-shrink: 0;
}

.search-input {
  max-width: 420px;
}

.state-banner,
.table-panel {
  border-radius: var(--pro-radius-lg);
}

@media (max-width: 1024px) {
  .search-bar,
  .search-input {
    max-width: none;
  }
}
</style>
