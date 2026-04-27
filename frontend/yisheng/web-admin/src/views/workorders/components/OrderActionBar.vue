<template>
  <section class="action-bar">
    <!-- Closed / Rejected banner -->
    <div v-if="isTerminal" class="action-bar__terminal">
      <el-tag :type="detail.status === 'CLOSED' ? 'success' : 'danger'" size="large" effect="plain">
        {{ detail.status === 'CLOSED' ? '工单已关闭' : '工单已驳回' }}
      </el-tag>
    </div>

    <!-- Action buttons -->
    <template v-else>
      <div v-if="showDoctorWorkflowHint" class="action-bar__hint">
        请先回复接手工单，之后才可关闭。
      </div>
      <div class="action-bar__buttons">
        <el-button
          v-if="showClaim"
          type="success"
          @click="handleClaim"
        >
          认领
        </el-button>
        <el-button
          v-if="showReply"
          :type="activeAction === 'reply' ? 'primary' : 'default'"
          @click="toggleAction('reply')"
        >
          回复
        </el-button>
        <el-button
          v-if="showEscalate"
          :type="activeAction === 'escalate' ? 'primary' : 'default'"
          plain
          @click="toggleAction('escalate')"
        >
          升级医生
        </el-button>
        <el-button
          v-if="showReject"
          :type="activeAction === 'reject' ? 'danger' : 'default'"
          plain
          @click="toggleAction('reject')"
        >
          驳回
        </el-button>
        <el-button
          v-if="showClose"
          :type="activeAction === 'close' ? 'success' : 'default'"
          plain
          @click="toggleAction('close')"
        >
          关闭
        </el-button>
      </div>

      <!-- Inline input area -->
      <transition name="expand">
        <div v-if="activeAction" class="action-bar__input">
          <div class="action-bar__input-header">
            <span>{{ actionConfig[activeAction].title }}</span>
            <el-button text size="small" @click="cancelAction">取消</el-button>
          </div>
          <el-input
            ref="inputRef"
            v-model="inputText"
            type="textarea"
            :rows="activeAction === 'reply' ? 3 : 2"
            :placeholder="actionConfig[activeAction].placeholder"
          />
          <div class="action-bar__input-footer">
            <el-button
              :type="actionConfig[activeAction].buttonType"
              :disabled="actionConfig[activeAction].required && !inputText.trim()"
              :loading="submitting"
              @click="handleSubmit"
            >
              {{ actionConfig[activeAction].submitText }}
            </el-button>
          </div>
        </div>
      </transition>
    </template>
  </section>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'

const props = defineProps({
  detail: { type: Object, required: true },
  role: { type: String, default: 'pharmacist' },
  draft: { type: Object, default: null },
  onClaim: { type: Function, default: null },
  onReply: { type: Function, default: null },
  onEscalate: { type: Function, default: null },
  onReject: { type: Function, default: null },
  onClose: { type: Function, default: null },
  onSaveDraft: { type: Function, default: null }
})

const activeAction = ref(null)
const inputText = ref('')
const submitting = ref(false)
const inputRef = ref(null)

const actionConfig = {
  reply: {
    title: '回复工单',
    placeholder: '请输入处理回复',
    submitText: '提交回复',
    buttonType: 'primary',
    required: true
  },
  escalate: {
    title: '升级给医生',
    placeholder: '升级说明（可选）',
    submitText: '确认升级',
    buttonType: 'warning',
    required: false
  },
  reject: {
    title: '驳回工单',
    placeholder: '请输入驳回原因',
    submitText: '确认驳回',
    buttonType: 'danger',
    required: true
  },
  close: {
    title: '关闭工单',
    placeholder: '关闭说明（可选）',
    submitText: '确认关闭',
    buttonType: 'success',
    required: false
  }
}

const status = computed(() => props.detail?.status)
const isTerminal = computed(() => status.value === 'CLOSED' || status.value === 'REJECTED')
const isPharmacist = computed(() => props.role === 'pharmacist')

const showClaim = computed(() =>
  isPharmacist.value && status.value === 'SUBMITTED'
)
const showReply = computed(() => {
  if (isPharmacist.value) return ['SUBMITTED', 'PHARMACIST_IN_PROGRESS'].includes(status.value)
  return ['WAITING_DOCTOR', 'DOCTOR_IN_PROGRESS'].includes(status.value)
})
const showEscalate = computed(() =>
  isPharmacist.value && ['SUBMITTED', 'PHARMACIST_IN_PROGRESS'].includes(status.value)
)
const showReject = computed(() => !isTerminal.value)
// Doctors can close only after they take ownership via reply.
const showClose = computed(() => {
  if (isPharmacist.value) return !isTerminal.value
  return status.value === 'DOCTOR_IN_PROGRESS'
})
const showDoctorWorkflowHint = computed(() =>
  !isPharmacist.value && status.value === 'WAITING_DOCTOR'
)

// Restore draft when detail changes
watch(() => props.draft, (draft) => {
  if (draft) {
    activeAction.value = draft.action
    inputText.value = draft.text
  }
}, { immediate: true })

function toggleAction(action) {
  if (activeAction.value === action) {
    cancelAction()
    return
  }
  // Save current draft before switching
  if (activeAction.value && inputText.value.trim()) {
    props.onSaveDraft?.({ action: activeAction.value, text: inputText.value })
  }
  activeAction.value = action
  inputText.value = ''
  nextTick(() => inputRef.value?.focus())
}

function cancelAction() {
  activeAction.value = null
  inputText.value = ''
}

async function handleSubmit() {
  const action = activeAction.value
  if (!action) {
    return
  }

  const handlers = {
    reply: props.onReply,
    escalate: props.onEscalate,
    reject: props.onReject,
    close: props.onClose
  }
  const handler = handlers[action]
  if (!handler) {
    return
  }

  submitting.value = true
  try {
    await handler(props.detail.id, inputText.value)
    activeAction.value = null
    inputText.value = ''
  } catch {
    // Parent handlers already surface request errors.
  } finally {
    submitting.value = false
  }
}

function handleClaim() {
  return props.onClaim?.(props.detail.id)
}
</script>

<style lang="scss" scoped>
.action-bar {
  display: grid;
  gap: 16px;
  padding: 20px;
  border: 1px solid var(--pro-border-color);
  border-radius: var(--pro-radius-lg);
  background: var(--pro-bg-primary);
}

.action-bar__terminal {
  display: flex;
  justify-content: center;
  padding: 8px 0;
}

.action-bar__buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.action-bar__hint {
  padding: 12px 14px;
  border-radius: var(--pro-radius-md);
  background: var(--pro-bg-secondary);
  color: var(--pro-text-secondary);
  font-size: 13px;
  line-height: 1.5;
}

.action-bar__input {
  display: grid;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--pro-border-color);
}

.action-bar__input-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  span {
    font-size: 14px;
    font-weight: 600;
    color: var(--pro-text-primary);
  }
}

.action-bar__input-footer {
  display: flex;
  justify-content: flex-end;
}

.expand-enter-active,
.expand-leave-active {
  transition: all 240ms cubic-bezier(0.2, 0, 0, 1);
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}

.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  max-height: 300px;
}

@media (max-width: 768px) {
  .action-bar__buttons {
    flex-direction: column;

    .el-button {
      width: 100%;
    }
  }
}
</style>
