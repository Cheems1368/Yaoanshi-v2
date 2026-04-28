<template>
  <section class="srm-panel">
    <h4 class="srm-panel__title">自报药品列表</h4>

    <div v-if="loading" class="srm-panel__loading">
      <el-skeleton :rows="3" animated />
    </div>

    <el-empty v-else-if="medications.length === 0" description="暂无自报药品" :image-size="60" />

    <div v-else class="srm-list">
      <div v-for="med in medications" :key="med.id" class="srm-item">
        <div class="srm-item__info">
          <span class="srm-item__name">{{ med.drugName }}</span>
          <span v-if="med.dosage" class="srm-item__meta">{{ med.dosage }}</span>
          <span v-if="med.notes" class="srm-item__meta">备注：{{ med.notes }}</span>
          <el-tag :type="statusTagType(med.status)" size="small" effect="plain" class="srm-item__tag">
            {{ statusText(med.status) }}
          </el-tag>
        </div>
        <div v-if="med.status === 'PENDING'" class="srm-item__actions">
          <el-button type="success" size="small" :loading="actionLoading === `ack-${med.id}`" @click="acknowledge(med)">
            已知晓
          </el-button>
          <el-button type="danger" size="small" plain :loading="actionLoading === `rej-${med.id}`" @click="openRejectDialog(med)">
            驳回
          </el-button>
          <el-button size="small" plain @click="goToTransfer(med)">
            转入正式计划
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="rejectDialogVisible" title="驳回原因" width="380px" :append-to-body="true">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="3"
        placeholder="请输入驳回原因（可选）"
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="actionLoading === `rej-${rejectingId}`" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  acknowledgeSelfReportedMedication,
  getSelfReportedMedicationsForWorkOrder,
  rejectSelfReportedMedication
} from '@/modules/pharmacist/api/workorders'

const props = defineProps({
  orderId: { type: [Number, String], required: true },
  patientId: { type: [Number, String], default: null }
})

const emit = defineEmits(['refresh'])

const router = useRouter()
const loading = ref(false)
const medications = ref([])
const actionLoading = ref(null)

const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const rejectingMed = ref(null)
const rejectingId = ref(null)

const loadMedications = async () => {
  if (!props.orderId) return
  loading.value = true
  try {
    const res = await getSelfReportedMedicationsForWorkOrder(props.orderId)
    medications.value = res.data?.data ?? res.data ?? []
  } catch {
    medications.value = []
  } finally {
    loading.value = false
  }
}

const acknowledge = async (med) => {
  actionLoading.value = `ack-${med.id}`
  try {
    await acknowledgeSelfReportedMedication(props.orderId, med.id)
    ElMessage.success('已确认知晓')
    await loadMedications()
    emit('refresh')
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    actionLoading.value = null
  }
}

const openRejectDialog = (med) => {
  rejectingMed.value = med
  rejectingId.value = med.id
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectingMed.value) return
  actionLoading.value = `rej-${rejectingMed.value.id}`
  try {
    await rejectSelfReportedMedication(props.orderId, rejectingMed.value.id, rejectReason.value.trim())
    rejectDialogVisible.value = false
    ElMessage.success('已驳回')
    await loadMedications()
    emit('refresh')
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    actionLoading.value = null
  }
}

const goToTransfer = (med) => {
  if (!props.patientId) return
  router.push({
    path: `/patients/${props.patientId}/medication-plan`,
    query: {
      prefillDrugName: med.drugName,
      prefillDosage: med.dosage || '',
      selfReportedMedicationId: med.id,
      workOrderId: props.orderId
    }
  })
}

const statusText = (status) => {
  const map = {
    PENDING: '待审核',
    ACTIVE: '已确认',
    STOPPED: '已停用',
    TRANSFERRED: '已转正式计划'
  }
  return map[status] || status
}

const statusTagType = (status) => {
  const map = {
    PENDING: 'warning',
    ACTIVE: 'success',
    STOPPED: 'info',
    TRANSFERRED: 'primary'
  }
  return map[status] || 'info'
}

watch(() => props.orderId, loadMedications)
onMounted(loadMedications)
</script>

<style lang="scss" scoped>
.srm-panel {
  border: 1px solid var(--pro-border-color);
  border-radius: var(--pro-radius-lg);
  padding: 16px;
  background: var(--pro-bg-primary);
}

.srm-panel__title {
  margin: 0 0 16px;
  font-size: 14px;
  font-weight: 600;
  color: var(--pro-text-primary);
}

.srm-panel__loading {
  padding: 8px 0;
}

.srm-list {
  display: grid;
  gap: 12px;
}

.srm-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 12px;
  border: 1px solid var(--pro-border-color);
  border-radius: var(--pro-radius-sm);
  background: var(--pro-bg-secondary);
}

.srm-item__info {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.srm-item__name {
  font-weight: 600;
  color: var(--pro-text-primary);
  font-size: 14px;
}

.srm-item__meta {
  font-size: 12px;
  color: var(--pro-text-secondary);
}

.srm-item__tag {
  width: fit-content;
}

.srm-item__actions {
  display: flex;
  gap: 6px;
  align-items: center;
  flex-shrink: 0;
}
</style>
