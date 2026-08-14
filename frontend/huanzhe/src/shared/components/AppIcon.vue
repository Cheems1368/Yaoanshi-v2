<template>
  <component
    :is="iconComponent"
    :size="size"
    :color="color"
    :stroke-width="strokeWidth"
    :class="props.class"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  House,
  Pill,
  MessageCircleQuestion,
  User,
  FileText,
  Sparkles,
  CircleCheck,
  CircleCheckBig,
  Calendar,
  MessageSquareMore,
  ChevronLeft,
  ChevronRight,
  Camera,
  Pencil,
  CircleX,
  Trash2,
  Download,
  Hourglass,
  Mic,
  Square,
} from 'lucide-vue-next'
import type { Component } from 'vue'

const iconMap: Record<string, Component> = {
  // Tab bar
  home: House,
  medkit: Pill,
  helpCircle: MessageCircleQuestion,
  person: User,
  // Documents & prescription
  documentTextOutline: FileText,
  // AI assistant
  sparkles: Sparkles,
  sparklesOutline: Sparkles,
  // Completion states
  checkmarkCircleOutline: CircleCheck,
  checkmarkCircle: CircleCheckBig,
  // Calendar
  calendarOutline: Calendar,
  // Chat
  chatboxEllipsesOutline: MessageSquareMore,
  // Navigation
  chevronBackOutline: ChevronLeft,
  chevronForwardOutline: ChevronRight,
  // Profile actions
  cameraOutline: Camera,
  createOutline: Pencil,
  // Actions
  closeCircle: CircleX,
  trash: Trash2,
  download: Download,
  hourglass: Hourglass,
  // Voice
  mic: Mic,
  stop: Square,
}

const props = withDefaults(defineProps<{
  name: string
  size?: number | string
  color?: string
  strokeWidth?: number | string
  class?: string
}>(), {
  size: 24,
  color: 'currentColor',
  strokeWidth: 1.75,
})

const iconComponent = computed(() => {
  const icon = iconMap[props.name]
  if (!icon) {
    console.warn(`[AppIcon] Unknown icon: "${props.name}"`)
    return CircleX
  }
  return icon
})
</script>
