<template>
  <ion-page data-testid="ai-assistant-page">
    <ion-header class="page-header ion-no-border">
      <ion-toolbar>
        <ion-buttons slot="start" data-testid="ai-back-button">
          <ion-button @click="handleBack">
            <ion-icon slot="icon-only" :icon="chevronBackOutline" />
          </ion-button>
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
        <div class="disclaimer-row">
          仅供用药科普参考，不能替代医生面诊。如症状加重请及时就医。
        </div>
        <div class="input-row" data-testid="ai-input-row">
          <div class="input-wrap">
            <ion-input
              v-model="inputText"
              class="message-input"
              data-testid="ai-input"
              placeholder="输入用药疑问，如漏服怎么办"
              @keyup.enter="submitInput"
            />
          </div>
          <ion-button
            class="send-button"
            :disabled="isSending || !inputText.trim()"
            @click="submitInput"
          >
            发送
          </ion-button>
        </div>
        <div v-if="lastFailedPrompt && !isSending" class="retry-row" data-testid="ai-retry-row">
          <ion-button size="small" fill="clear" @click="retryLastFailed">重试上一条问题</ion-button>
        </div>
      </ion-toolbar>
    </ion-footer>

    <ion-alert
      :is-open="showConsentDialog"
      header="数据共享声明"
      :message="consentMessage"
      :buttons="consentButtons"
      @didDismiss="showConsentDialog = false"
    />

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
  IonAlert,
  IonButton,
  IonButtons,
  IonContent,
  IonFooter,
  IonHeader,
  IonIcon,
  IonInput,
  IonPage,
  IonTitle,
  IonToast,
  IonToolbar,
  onIonViewWillEnter
} from '@ionic/vue'
import { chevronBackOutline } from 'ionicons/icons'
import AiHomeView from '@/modules/patient/components/ai/AiHomeView.vue'
import AiChatView, { type AiViewMessage } from '@/modules/patient/components/ai/AiChatView.vue'
import { aiAssistantService, type AiChatMessage } from '@/modules/patient/api/aiAssistant'
import { aiConsentService } from '@/modules/patient/api/mvp'

const router = useRouter()
const viewMode = ref<'home' | 'chat'>('home')
const showConsentDialog = ref(false)
const consentMessage =
  'AI 助手功能需要将您的问题发送至第三方 AI 服务（硅基流动）进行处理。您的问题内容将按照隐私协议处理，不会与您的身份信息绑定存储。'
const consentButtons = [
  {
    text: '不同意',
    role: 'cancel',
    handler: () => {
      router.push('/tabs/home')
    }
  },
  {
    text: '同意并继续',
    handler: async () => {
      try {
        await aiConsentService.grantConsent()
      } catch {
        // 忽略错误，下次进入时会再次检查
      }
    }
  }
]

onIonViewWillEnter(async () => {
  try {
    const res = await aiConsentService.getConsentStatus()
    if (!res.data) {
      showConsentDialog.value = true
    }
  } catch {
    // 网络错误时不阻断使用
  }
})
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
    suggestions.value = response.data.suggestions || []
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
  --background: #ffffff;
  --color: #1E293B;
}

.page-header ion-toolbar {
  --background: #ffffff;
  --color: #1E293B;
}

.page-header ion-button {
  --color: #1E293B;
}

.page-content {
  --background: linear-gradient(180deg, #EBF4FF 0%, #F0F7FF 30%, #F8FAFC 100%);
}

.input-footer {
  --background: #ffffff;
  box-shadow: 0 -1px 8px rgba(0, 0, 0, 0.06);
}

.input-footer ion-toolbar {
  --background: #ffffff;
  --padding-top: 0;
  --padding-bottom: 0;
}

.disclaimer-row {
  text-align: center;
  font-size: 11px;
  color: #94A3B8;
  padding: 6px 16px 2px;
  line-height: 1.4;
}

.input-row {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 6px 12px calc(8px + env(safe-area-inset-bottom));
}

.input-wrap {
  flex: 1;
  background: #F1F5F9;
  border-radius: 22px;
  overflow: hidden;
}

.message-input {
  --background: transparent;
  --padding-start: 16px;
  --padding-end: 16px;
  --placeholder-color: #94A3B8;
  font-size: 14px;
}

.send-button {
  --background: #3B82F6;
  --border-radius: 20px;
  --padding-start: 16px;
  --padding-end: 16px;
  font-size: 14px;
  font-weight: 600;
  height: 36px;
}

.send-button[disabled] {
  --background: #CBD5E1;
}

.retry-row {
  display: flex;
  justify-content: flex-end;
  padding: 0 10px calc(4px + env(safe-area-inset-bottom));
}
</style>
