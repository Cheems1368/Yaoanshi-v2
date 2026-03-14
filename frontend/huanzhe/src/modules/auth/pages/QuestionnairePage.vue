<template>
  <ion-page>
    <ion-header class="custom-header">
      <ion-toolbar>
        <ion-button fill="clear" class="back-btn" @click="goBack">
          <ion-icon :icon="chevronBackOutline" class="back-icon" aria-hidden="true" />
        </ion-button>
        <ion-title class="page-title">患者健康管理信息表</ion-title>
      </ion-toolbar>
    </ion-header>
    
    <ion-content class="questionnaire-content">
      <div class="questionnaire-container">
        <div class="progress-section">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
          </div>
          <div class="progress-text">{{ currentStep }} / {{ totalSteps }}</div>
        </div>

        <div class="form-container">
          <div v-if="currentStep === 1" class="form-layer layer-red">
            <div class="layer-header">
              <h2 class="layer-title">第一层 - 核心必填信息</h2>
              <p class="layer-description">首次注册强制填写</p>
            </div>
            <div class="form-fields">
              <div class="form-item">
                <label class="form-label required">过敏史</label>
                <ion-textarea v-model="formData.layer1.allergy" placeholder="请详细描述您的过敏史" class="form-textarea" :rows="3"></ion-textarea>
              </div>
              <div class="form-item">
                <label class="form-label required">主要诊断</label>
                <ion-textarea v-model="formData.layer1.diagnosis" placeholder="请输入您的主要诊断信息" class="form-textarea" :rows="3"></ion-textarea>
              </div>
            </div>
          </div>

          <div v-if="currentStep === 2" class="form-layer layer-yellow">
            <div class="layer-header">
              <h2 class="layer-title">第二层 - 次要关键信息</h2>
              <p class="layer-description">首次使用核心功能前弹窗提示，可跳过</p>
            </div>
            <div class="form-fields">
              <div class="form-item">
                <label class="form-label">既往病史</label>
                <ion-textarea v-model="formData.layer2.medicalHistory" placeholder="请描述您的既往病史" class="form-textarea" :rows="3"></ion-textarea>
              </div>
              <div class="form-item">
                <label class="form-label">烟酒史</label>
                <ion-textarea v-model="formData.layer2.smokingDrinking" placeholder="请描述您的吸烟和饮酒情况" class="form-textarea" :rows="3"></ion-textarea>
              </div>
              <div class="form-item">
                <label class="form-label">肝肾功能</label>
                <ion-textarea v-model="formData.layer2.liverKidney" placeholder="请描述您的肝肾功能情况" class="form-textarea" :rows="3"></ion-textarea>
              </div>
              <div class="form-item">
                <label class="form-label">当前用药</label>
                <ion-textarea v-model="formData.layer2.currentMedication" placeholder="请描述您当前正在服用的药物" class="form-textarea" :rows="3"></ion-textarea>
              </div>
            </div>
          </div>

          <div v-if="currentStep === 3" class="form-layer layer-blue">
            <div class="layer-header">
              <h2 class="layer-title">第三层 - 一般完善信息</h2>
              <p class="layer-description">我的页面长期引导，通过档案完整度激励用户</p>
            </div>
            <div class="form-fields">
              <div class="form-item">
                <label class="form-label">详细症状</label>
                <ion-textarea v-model="formData.layer3.detailedSymptoms" placeholder="请详细描述您的症状" class="form-textarea" :rows="4"></ion-textarea>
              </div>
              <div class="form-item">
                <label class="form-label">家族史</label>
                <ion-textarea v-model="formData.layer3.familyHistory" placeholder="请描述您的家族病史" class="form-textarea" :rows="3"></ion-textarea>
              </div>
              <div class="form-item">
                <label class="form-label">手术史</label>
                <ion-textarea v-model="formData.layer3.surgeryHistory" placeholder="请描述您的手术史" class="form-textarea" :rows="3"></ion-textarea>
              </div>
              <div class="form-item">
                <label class="form-label">社会史</label>
                <ion-textarea v-model="formData.layer3.socialHistory" placeholder="请描述您的社会史" class="form-textarea" :rows="3"></ion-textarea>
              </div>
            </div>
          </div>
        </div>

        <div class="action-buttons">
          <ion-button v-if="currentStep > 1" fill="outline" class="prev-btn" @click="prevStep">上一步</ion-button>
          <ion-button v-if="currentStep < totalSteps" class="next-btn" @click="nextStep">下一步</ion-button>
          <ion-button v-if="currentStep === totalSteps" class="submit-btn" @click="submitForm">提交 (NEXT)</ion-button>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from "vue"
import { useRouter } from "vue-router"
import { IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonButton, IonIcon, IonTextarea } from "@ionic/vue"
import { chevronBackOutline } from "ionicons/icons"
import { healthDataService, type HealthData } from "@/services/healthDataService"
import { patientService } from "@/modules/patient/api/mvp"

const router = useRouter()

const currentStep = ref(1)
const totalSteps = 3

const progressPercentage = computed(() => {
  return (currentStep.value / totalSteps) * 100
})

const formData = reactive({
  layer1: { allergy: '', diagnosis: '' },
  layer2: { medicalHistory: '', smokingDrinking: '', liverKidney: '', currentMedication: '' },
  layer3: { detailedSymptoms: '', familyHistory: '', surgeryHistory: '', socialHistory: '' }
})

const nextStep = () => {
  if (currentStep.value < totalSteps) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

const submitForm = async () => {
  try {
    const response = await patientService.updateHealthProfile(formData)
    const persistedHealthData: HealthData = response.data?.submitTime
      ? (response.data as HealthData)
      : {
          ...formData,
          submitTime: new Date().toISOString()
        }
    healthDataService.replaceHealthData(persistedHealthData)

    alert('问卷提交成功！健康信息已保存到个人资料。')

    router.push('/tabs/profile')
  } catch {
    alert('保存失败，请重试')
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.custom-header {
  --background: #ffffff;
  --color: #000000;
}

.back-btn {
  --color: #666666;
  margin-left: 10px;
}

.back-icon {
  width: 20px;
  height: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #dc2626;
  text-align: center;
}

.questionnaire-content {
  --background: #f5f5f5;
}

.questionnaire-container {
  padding: 20px;
  max-width: 400px;
  margin: 0 auto;
}

.progress-section {
  margin-bottom: 20px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background-color: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #dc2626, #f59e0b, #3b82f6);
  transition: width 0.3s ease;
}

.progress-text {
  text-align: center;
  font-size: 14px;
  color: #666666;
}

.form-container {
  background: #ffffff;
  border: 3px solid #3b82f6;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  min-height: 400px;
}

.layer-header {
  margin-bottom: 20px;
  text-align: center;
}

.layer-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.layer-description {
  font-size: 14px;
  color: #666666;
  margin: 0;
}

.layer-red .layer-title {
  color: #dc2626;
}

.layer-yellow .layer-title {
  color: #f59e0b;
}

.layer-blue .layer-title {
  color: #3b82f6;
}

.form-fields {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
}

.form-label {
  font-size: 16px;
  font-weight: 500;
  color: #333333;
  margin-bottom: 8px;
}

.form-label.required::after {
  content: ' *';
  color: #dc2626;
}

.form-input {
  --background: #ffffff;
  --color: #333333;
  --border-radius: 8px;
  --border-width: 1px;
  --border-color: #d1d5db;
  --padding-start: 12px;
  --padding-end: 12px;
  height: 48px;
}

.form-textarea {
  --background: #ffffff;
  --color: #333333;
  --border-radius: 8px;
  --border-width: 1px;
  --border-color: #d1d5db;
  --padding-start: 12px;
  --padding-end: 12px;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  gap: 15px;
  margin-top: 20px;
}

.prev-btn {
  --border-color: #6b7280;
  --color: #6b7280;
  flex: 1;
  height: 48px;
}

.next-btn {
  --background: #3b82f6;
  --color: #ffffff;
  flex: 1;
  height: 48px;
}

.submit-btn {
  --background: #dc2626;
  --color: #ffffff;
  flex: 1;
  height: 48px;
  font-weight: 600;
}
</style>
