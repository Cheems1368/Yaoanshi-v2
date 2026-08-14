import { httpService } from '@/shared/http/client'

export interface AiChatMessage {
  role: 'user' | 'assistant' | 'system'
  content: string
}

export interface AiChatRequest {
  messages: AiChatMessage[]
}

export interface AiChatResponse {
  reply: string
  model: string
  requestId: string
  suggestions?: string[]
}

export const aiAssistantService = {
  chat(payload: AiChatRequest) {
    return httpService.post<AiChatResponse>('/app-api/ai/chat', payload)
  }
}
