<template>
  <ion-page data-testid="ai-assistant-page">
    <ion-header class="page-header ion-no-border">
      <ion-toolbar>
        <ion-buttons slot="start" data-testid="ai-back-button">
          <ion-button @click="handleBack">
            <AppIcon slot="icon-only" name="chevronBackOutline" :size="22" />
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
  IonInput,
  IonPage,
  IonTitle,
  IonToast,
  IonToolbar,
  onIonViewWillEnter
} from '@ionic/vue'
import AiHomeView from '@/modules/patient/components/ai/AiHomeView.vue'
import AiChatView, { type AiViewMessage } from '@/modules/patient/components/ai/AiChatView.vue'
import { aiAssistantService, type AiChatMessage } from '@/modules/patient/api/aiAssistant'
import { aiConsentService } from '@/modules/patient/api/mvp'

const router = useRouter()
const viewMode = ref<'home' | 'chat'>('home')
const showConsentDialog = ref(false)
const consentMessage =
  'AI 助手功能需要将您的问题发送至第三方 AI 服务（硅基流动）进行处理。您的问题内容将按照隐私协议处理，不会与您的身份信息绑定存储。'
const AI_CONSENT_KEY = 'ai_consent_granted'

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
        localStorage.setItem(AI_CONSENT_KEY, 'true')
      } catch {
        showError('同意操作失败，请检查网络后重试')
      }
    }
  }
]

onIonViewWillEnter(async () => {
  if (localStorage.getItem(AI_CONSENT_KEY) === 'true') {
    return
  }
  try {
    const res = await aiConsentService.getConsentStatus()
    if (res.data) {
      localStorage.setItem(AI_CONSENT_KEY, 'true')
    } else {
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
    if (error?.code === 40031) {
      localStorage.removeItem(AI_CONSENT_KEY)
      showConsentDialog.value = true
      return
    }
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
  --background: var(--patient-bg-card);
  --color: var(--patient-text-primary);
}

.page-header ion-toolbar {
  --background: var(--patient-bg-card);
  --color: var(--patient-text-primary);
}

.page-header ion-button {
  --color: var(--patient-text-primary);
}

.page-content {
  --background: linear-gradient(180deg, var(--patient-primary-light) 0%, var(--patient-bg-page) 40%);
}

.input-footer {
  --background: var(--patient-bg-card);
  box-shadow: 0 -1px 8px rgba(0, 0, 0, 0.06);
}

.input-footer ion-toolbar {
  --background: var(--patient-bg-card);
  --padding-top: 0;
  --padding-bottom: 0;
}

.disclaimer-row {
  text-align: center;
  font-size: var(--patient-font-caption);
  color: var(--patient-text-muted);
  padding: var(--patient-space-2) var(--patient-space-4) var(--patient-space-1);
  line-height: var(--patient-leading-normal);
}

.input-row {
  display: flex;
  gap: var(--patient-space-2);
  align-items: center;
  padding: var(--patient-space-2) var(--patient-space-3) calc(var(--patient-space-2) + env(safe-area-inset-bottom));
}

.input-wrap {
  flex: 1;
  background: var(--patient-bg-page);
  border-radius: var(--patient-radius-xl);
  overflow: hidden;
}

.message-input {
  --background: transparent;
  --padding-start: var(--patient-space-4);
  --padding-end: var(--patient-space-4);
  --placeholder-color: var(--patient-text-muted);
  font-size: var(--patient-font-helper);
}

.send-button {
  --background: var(--patient-primary);
  --border-radius: var(--patient-radius-xl);
  --padding-start: var(--patient-space-4);
  --padding-end: var(--patient-space-4);
  font-size: var(--patient-font-helper);
  font-weight: var(--patient-font-semibold);
  height: 36px;
}

.send-button[disabled] {
  --background: var(--patient-border);
}

.retry-row {
  display: flex;
  justify-content: flex-end;
  padding: 0 var(--patient-space-3) calc(var(--patient-space-1) + env(safe-area-inset-bottom));
}
</style>
