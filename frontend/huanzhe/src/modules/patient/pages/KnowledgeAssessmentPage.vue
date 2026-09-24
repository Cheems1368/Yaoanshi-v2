<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start"><ion-back-button default-href="/tabs/home" /></ion-buttons>
        <ion-title>用药知识测评</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content class="page-content">
      <div class="container">
        <ion-card class="intro-card">
          <ion-card-content>
            <h2>{{ phaseText }}</h2>
            <p>共 10 题，用于一般用药科普，不替代医生或药师的个体化诊疗意见。</p>
          </ion-card-content>
        </ion-card>

        <div v-if="loading" class="loading"><ion-spinner name="crescent" /></div>
        <ion-card v-else-if="completedAssessment" class="result-card">
          <ion-card-content>
            <div class="result-score">{{ completedAssessment.score }} / {{ completedAssessment.maxScore }}</div>
            <p>本阶段已完成 · {{ formatDateTime(completedAssessment.submittedAt) }}</p>
          </ion-card-content>
        </ion-card>
        <ion-card v-else-if="status && !status.due" class="result-card">
          <ion-card-content>
            <h2>当前没有待完成测评</h2>
            <p>测评不会阻塞用药和求助主流程，到期后首页会再次提醒。</p>
          </ion-card-content>
        </ion-card>
        <template v-else>
          <ion-card v-for="(question, index) in questions" :key="question.questionCode" class="question-card">
            <ion-card-header>
              <ion-card-title>{{ index + 1 }}. {{ question.questionText }}</ion-card-title>
            </ion-card-header>
            <ion-card-content>
              <div class="option-list">
                <button
                  v-for="option in question.options"
                  :key="option.code"
                  type="button"
                  class="option-button"
                  :class="{ 'option-button--selected': answers[question.questionCode] === option.code }"
                  @click="answers[question.questionCode] = option.code"
                >{{ option.text }}</button>
              </div>
            </ion-card-content>
          </ion-card>
          <ion-button data-testid="knowledge-submit" expand="block" :disabled="submitting || !allAnswered" @click="submit">
            {{ submitting ? '提交中...' : '提交测评' }}
          </ion-button>
        </template>
      </div>
      <ion-toast :is-open="toastOpen" :message="toastMessage" :duration="2200" @did-dismiss="toastOpen = false" />
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import {
  IonBackButton, IonButton, IonButtons, IonCard, IonCardContent, IonCardHeader, IonCardTitle,
  IonContent, IonHeader, IonPage, IonSpinner, IonTitle,
  IonToast, IonToolbar, onIonViewWillEnter
} from '@ionic/vue'
import { computed, reactive, ref } from 'vue'
import { patientSession } from '@/app/session'
import {
  assessmentService,
  type KnowledgeAssessmentDTO,
  type KnowledgeAssessmentStatusDTO,
  type KnowledgeQuestionDTO
} from '@/modules/patient/api/mvp'

const loading = ref(false)
const submitting = ref(false)
const questions = ref<KnowledgeQuestionDTO[]>([])
const status = ref<KnowledgeAssessmentStatusDTO | null>(null)
const answers = reactive<Record<string, string>>({})
const toastOpen = ref(false)
const toastMessage = ref('')
let requestEpoch = 0

const phase = computed(() => status.value?.currentPhase || 'DAY_1')
const phaseText = computed(() => phase.value === 'WEEK_4' ? '第 4 周复测' : '首次用药科普测评')
const completedAssessment = computed<KnowledgeAssessmentDTO | null>(() =>
  phase.value === 'WEEK_4' ? status.value?.week4 ?? null : status.value?.day1 ?? null
)
const allAnswered = computed(() => questions.value.length > 0
  && questions.value.every((question) => Boolean(answers[question.questionCode])))

const clearAssessmentState = () => {
  questions.value = []
  status.value = null
  Object.keys(answers).forEach((questionCode) => delete answers[questionCode])
}

const load = async () => {
  const expectedAccountSubject = patientSession.getAccountSubject()
  const expectedRequestEpoch = ++requestEpoch
  clearAssessmentState()
  submitting.value = false
  loading.value = true
  if (!expectedAccountSubject) {
    loading.value = false
    return
  }
  try {
    const [questionResponse, statusResponse] = await Promise.all([
      assessmentService.getKnowledgeQuestions(),
      assessmentService.getKnowledgeStatus()
    ])
    if (expectedRequestEpoch !== requestEpoch
      || patientSession.getAccountSubject() !== expectedAccountSubject) return
    questions.value = questionResponse.data ?? []
    status.value = statusResponse.data ?? null
  } catch (error: any) {
    if (expectedRequestEpoch !== requestEpoch
      || patientSession.getAccountSubject() !== expectedAccountSubject) return
    toastMessage.value = error?.message || '测评加载失败'
    toastOpen.value = true
  } finally {
    if (expectedRequestEpoch === requestEpoch) loading.value = false
  }
}

const submit = async () => {
  if (!allAnswered.value || !status.value) return
  const expectedAccountSubject = patientSession.getAccountSubject()
  if (!expectedAccountSubject) return
  const expectedRequestEpoch = requestEpoch
  const submittedPhase = phase.value
  const submittedQuestionSetVersion = status.value.questionSetVersion
  const submittedAnswers = questions.value.map((question) => ({
    questionCode: question.questionCode,
    selectedOption: answers[question.questionCode]
  }))
  submitting.value = true
  try {
    const response = await assessmentService.submitKnowledge({
      phase: submittedPhase,
      questionSetVersion: submittedQuestionSetVersion,
      answers: submittedAnswers
    })
    if (expectedRequestEpoch !== requestEpoch
      || patientSession.getAccountSubject() !== expectedAccountSubject
      || !status.value) return
    if (submittedPhase === 'WEEK_4') status.value.week4 = response.data
    else status.value.day1 = response.data
    toastMessage.value = `提交成功，得分 ${response.data.score}/${response.data.maxScore}`
    toastOpen.value = true
  } catch (error: any) {
    if (expectedRequestEpoch !== requestEpoch
      || patientSession.getAccountSubject() !== expectedAccountSubject) return
    toastMessage.value = error?.message || '提交失败'
    toastOpen.value = true
  } finally {
    if (expectedRequestEpoch === requestEpoch
      && patientSession.getAccountSubject() === expectedAccountSubject) submitting.value = false
  }
}

const formatDateTime = (value: string) => new Date(value).toLocaleString('zh-CN', { hour12: false })
onIonViewWillEnter(load)
</script>

<style scoped>
.page-content { --background: var(--patient-bg-page); }
.container { padding: var(--patient-space-4); display: flex; flex-direction: column; gap: var(--patient-space-3); }
.intro-card, .question-card, .result-card { margin: 0; }
.intro-card h2 { margin: 0 0 var(--patient-space-2); font-size: var(--patient-font-title); }
.intro-card p, .result-card p { margin: 0; color: var(--patient-text-secondary); line-height: var(--patient-leading-normal); }
.question-card ion-card-title { font-size: var(--patient-font-body); line-height: var(--patient-leading-normal); }
.loading { padding: var(--patient-space-8); text-align: center; }
.result-score { color: var(--patient-primary); font-size: 36px; font-weight: var(--patient-font-bold); }
.option-list { display: flex; flex-direction: column; gap: var(--patient-space-2); }
.option-button { width: 100%; min-height: 48px; padding: var(--patient-space-3); text-align: left; border: 1px solid var(--patient-border); border-radius: var(--patient-radius-md); background: var(--patient-bg-card); color: var(--patient-text-primary); line-height: var(--patient-leading-normal); }
.option-button--selected { border-color: var(--patient-primary); background: var(--patient-primary-light); color: var(--patient-primary); font-weight: var(--patient-font-semibold); }
</style>
