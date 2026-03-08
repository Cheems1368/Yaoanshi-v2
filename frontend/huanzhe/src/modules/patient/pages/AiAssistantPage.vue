<template>
  <ion-page data-testid="ai-assistant-page">
    <ion-header class="page-header ion-no-border">
      <ion-toolbar>
        <ion-buttons slot="start" data-testid="ai-back-button">
          <ion-button @click="handleBack">返回</ion-button>
        </ion-buttons>
        <ion-title>用药科普助手</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content ref="contentRef" class="page-content">
      <AiHomeView v-if="viewMode === 'home'" @start-chat="startChat" />
      <AiChatView
        v-else
        :messages="messages"
        :suggestions="suggestions"
        @select-suggestion="sendSuggestion"
      />
    </ion-content>

    <ion-footer class="input-footer ion-no-border">
      <ion-toolbar>
        <div class="input-row" data-testid="ai-input-row">
          <ion-input
            v-model="inputText"
            class="message-input"
            data-testid="ai-input"
            placeholder="输入用药疑问，如漏服怎么办"
            @keyup.enter="submitInput"
          />
          <ion-button :disabled="isSending || !inputText.trim()" @click="submitInput">发送</ion-button>
        </div>
        <div v-if="lastFailedPrompt && !isSending" class="retry-row" data-testid="ai-retry-row">
          <ion-button size="small" fill="clear" @click="retryLastFailed">重试上一条问题</ion-button>
        </div>
      </ion-toolbar>
    </ion-footer>

    <ion-toast
      :is-open="showToast"
      :message="toastMessage"
      :duration="2600"
      @didDismiss="showToast = false"
    />
  </ion-page>
</template>

<script setup lang="ts">
import { nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  IonButton,
  IonButtons,
  IonContent,
  IonFooter,
  IonHeader,
  IonInput,
  IonPage,
  IonTitle,
  IonToast,
  IonToolbar
} from '@ionic/vue'
import AiHomeView from '@/modules/patient/components/ai/AiHomeView.vue'
import AiChatView, { type AiViewMessage } from '@/modules/patient/components/ai/AiChatView.vue'
import { aiAssistantService, type AiChatMessage } from '@/modules/patient/api/aiAssistant'

const router = useRouter()
const viewMode = ref<'home' | 'chat'>('home')
const contentRef = ref<any>(null)
const inputText = ref('')
const isSending = ref(false)
const showToast = ref(false)
const toastMessage = ref('')
const suggestions = ref<string[]>([])
const lastFailedPrompt = ref('')
const initialGreeting = '你好，我是用药科普助手，可帮助说明服药时间、漏服处理、常见不良反应和何时就医。'
const messages = ref<AiViewMessage[]>([
  {
    role: 'assistant',
    content: initialGreeting
  }
])

const showError = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}

const backToHome = () => {
  viewMode.value = 'home'
  messages.value = [
    {
      role: 'assistant',
      content: initialGreeting
    }
  ]
  suggestions.value = []
  lastFailedPrompt.value = ''
}

const handleBack = () => {
  if (viewMode.value === 'chat') {
    backToHome()
    return
  }

  router.push('/tabs/home')
}

const scrollToBottom = async () => {
  await nextTick()
  if (contentRef.value?.scrollToBottom) {
    await contentRef.value.scrollToBottom(220)
  }
}

const buildSuggestions = (reply: string) => {
  if (reply.includes('降压') || reply.includes('血压')) {
    suggestions.value = ['漏服降压药怎么处理？', '需要早晚都量血压吗？', '头晕时是否停药？']
    return
  }
  if (reply.includes('出血') || reply.includes('阿司匹林')) {
    suggestions.value = ['胃不舒服还能继续吃吗？', '要不要和护胃药同服？', '什么情况必须急诊？']
    return
  }
  suggestions.value = ['饭前饭后吃药怎么判断？', '老年人多药联用注意什么？', '什么时候需要线下复诊？']
}

const submitMessage = async (text: string) => {
  const content = text.trim()
  if (!content || isSending.value) {
    return
  }

  isSending.value = true
  suggestions.value = []
  lastFailedPrompt.value = ''
  viewMode.value = 'chat'
  inputText.value = ''
  messages.value.push({ role: 'user', content })
  messages.value.push({ role: 'assistant', content: '', isLoading: true })
  await scrollToBottom()

  try {
    const history: AiChatMessage[] = messages.value
      .filter((message) => !message.isLoading)
      .map((message) => ({
        role: message.role,
        content: message.content
      }))

    const response = await aiAssistantService.chat({
      messages: history
    })

    messages.value[messages.value.length - 1] = {
      role: 'assistant',
      content: response.data.reply
    }
    lastFailedPrompt.value = ''
    buildSuggestions(response.data.reply)
  } catch (error: any) {
    messages.value[messages.value.length - 1] = {
      role: 'assistant',
      content: '当前服务繁忙，请稍后重试。'
    }
    lastFailedPrompt.value = content
    showError(error?.message || 'AI 服务暂不可用')
  } finally {
    isSending.value = false
    await scrollToBottom()
  }
}

const startChat = (text: string) => {
  submitMessage(text)
}

const sendSuggestion = (text: string) => {
  submitMessage(text)
}

const submitInput = () => {
  submitMessage(inputText.value)
}

const retryLastFailed = () => {
  if (!lastFailedPrompt.value || isSending.value) {
    return
  }
  submitMessage(lastFailedPrompt.value)
}
</script>

<style scoped>
.page-header {
  --background: #2979ff;
  --color: #ffffff;
}

.page-content {
  --background: linear-gradient(180deg, #eff6ff 0%, #ffffff 42%, #f8fafc 100%);
}

.input-footer {
  --background: #ffffff;
  box-shadow: 0 -2px 12px rgba(15, 23, 42, 0.08);
}

.input-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 6px 10px calc(8px + env(safe-area-inset-bottom));
}

.retry-row {
  display: flex;
  justify-content: flex-end;
  padding: 0 10px calc(4px + env(safe-area-inset-bottom));
}

.message-input {
  --background: #f8fafc;
  --border-radius: 22px;
  --padding-start: 14px;
  --padding-end: 14px;
  border: 1px solid #dbeafe;
  border-radius: 22px;
}
</style>
