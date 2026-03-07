<template>
  <div class="chat-container">
    <section
      v-for="(message, index) in messages"
      :key="`${message.role}-${index}-${message.content.slice(0, 12)}`"
      class="message-row"
      :class="message.role"
    >
      <div class="avatar">{{ message.role === 'assistant' ? 'AI' : '我' }}</div>
      <div class="bubble">
        <div v-if="message.isLoading" class="loading">正在生成回答...</div>
        <div v-else class="content" v-html="renderText(message.displayContent || message.content)" />
      </div>
    </section>

    <section v-if="suggestions.length" class="suggestions">
      <ion-chip
        v-for="suggestion in suggestions"
        :key="suggestion"
        @click="$emit('select-suggestion', suggestion)"
      >
        {{ suggestion }}
      </ion-chip>
    </section>
  </div>
</template>

<script setup lang="ts">
import { IonChip } from '@ionic/vue'

export interface AiViewMessage {
  role: 'assistant' | 'user'
  content: string
  isLoading?: boolean
  displayContent?: string
}

defineProps<{
  messages: AiViewMessage[]
  suggestions: string[]
}>()

defineEmits<{
  'select-suggestion': [text: string]
}>()

const renderText = (text: string) => {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
}
</script>

<style scoped>
.chat-container {
  padding: 14px 12px 90px;
}

.message-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.message-row.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  background: #dbeafe;
  color: #1d4ed8;
}

.message-row.user .avatar {
  background: #e2e8f0;
  color: #334155;
}

.bubble {
  max-width: calc(100% - 44px);
  padding: 10px 12px;
  border-radius: 12px;
  line-height: 1.6;
}

.message-row.assistant .bubble {
  background: #ffffff;
  border: 1px solid #dbeafe;
  color: #0f172a;
}

.message-row.user .bubble {
  background: #2563eb;
  color: #ffffff;
}

.loading {
  color: #64748b;
}

.content {
  font-size: 15px;
  word-break: break-word;
}

.suggestions {
  margin-top: 8px;
  margin-left: 40px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

ion-chip {
  --background: #dbeafe;
  --color: #1e3a8a;
}
</style>
