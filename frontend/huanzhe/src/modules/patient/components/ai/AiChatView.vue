<template>
  <div class="chat-container" data-testid="ai-chat-view">
    <section
      v-for="(message, index) in messages"
      :key="`${message.role}-${index}-${message.content.slice(0, 12)}`"
      class="message-row"
      :class="message.role"
      :data-testid="`ai-message-${message.role}-${index}`"
    >
      <div class="avatar" :class="message.role">
        <ion-icon v-if="message.role === 'assistant'" :icon="sparklesOutline" />
        <span v-else>我</span>
      </div>
      <div class="bubble" :class="message.role">
        <div v-if="message.isLoading" class="loading">
          <span class="dot" /><span class="dot" /><span class="dot" />
        </div>
        <div v-else class="content" v-html="renderText(message.displayContent || message.content)" />
      </div>
    </section>

    <section v-if="suggestions.length" class="suggestions">
      <button
        v-for="(suggestion, index) in suggestions"
        :key="suggestion"
        class="suggestion-chip"
        :data-testid="`ai-suggestion-${index}`"
        @click="$emit('select-suggestion', suggestion)"
      >
        {{ suggestion }}
      </button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { IonIcon } from '@ionic/vue'
import { sparklesOutline } from 'ionicons/icons'

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
  padding: 16px 14px 100px;
}

.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  align-items: flex-start;
}

.message-row.user {
  flex-direction: row-reverse;
}

/* Avatar */
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar.assistant {
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  color: #ffffff;
  font-size: 18px;
}

.avatar.user {
  background: #E2E8F0;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
}

/* Bubble */
.bubble {
  max-width: calc(100% - 56px);
  padding: 12px 14px;
  line-height: 1.7;
  word-break: break-word;
}

.bubble.assistant {
  background: #ffffff;
  border-radius: 2px 16px 16px 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  color: #1E293B;
}

.bubble.user {
  background: #3B82F6;
  border-radius: 16px 2px 16px 16px;
  color: #ffffff;
}

/* Content */
.content {
  font-size: 15px;
}

/* Loading dots animation */
.loading {
  display: flex;
  gap: 4px;
  align-items: center;
  padding: 4px 0;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #94A3B8;
  animation: pulse-dot 1.2s ease-in-out infinite;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes pulse-dot {
  0%, 80%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  40% {
    opacity: 1;
    transform: scale(1);
  }
}

/* Suggestion chips */
.suggestions {
  margin-top: 4px;
  margin-left: 46px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.suggestion-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 14px;
  border-radius: 18px;
  border: 1px solid #BFDBFE;
  background: #EFF6FF;
  color: #2563EB;
  font-size: 13px;
  line-height: 1.4;
  cursor: pointer;
  transition: all 0.15s ease;
}

.suggestion-chip:active {
  background: #DBEAFE;
  border-color: #93C5FD;
}
</style>
