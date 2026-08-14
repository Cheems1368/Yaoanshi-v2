<template>
  <ion-page>
    <ion-content class="bind-content">
      <div class="bind-container">
        <header class="page-header">
          <ion-button fill="clear" class="back-btn" @click="goBack">
            <AppIcon name="chevronBackOutline" :size="22" />
          </ion-button>
          <div>
            <p class="eyebrow">患者绑定</p>
            <h1>绑定患者身份</h1>
            <p>登录后填写患者姓名、手机号和院内患者编号，完成身份绑定后即可进入首页。</p>
          </div>
        </header>

        <section class="form-card">
          <div class="form-item">
            <label for="name">姓名</label>
            <ion-input
              id="name"
              v-model="formData.name"
              placeholder="请输入患者姓名"
              class="form-input"
            />
          </div>

          <div class="form-item">
            <label for="mobile">手机号</label>
            <ion-input
              id="mobile"
              v-model="formData.mobile"
              type="tel"
              inputmode="numeric"
              :maxlength="11"
              placeholder="请输入登录手机号"
              class="form-input"
            />
          </div>

          <div class="form-item">
            <label for="hospitalPatientId">院内患者编号</label>
            <ion-input
              id="hospitalPatientId"
              v-model="formData.hospitalPatientId"
              placeholder="请输入院内患者编号"
              class="form-input"
            />
          </div>
        </section>

        <div class="submit-section">
          <ion-button expand="block" class="submit-btn" :disabled="isLoading" @click="submitForm">
            提交绑定
          </ion-button>
          <p class="help-text">
            若服务端提示编号冲突或需要药师确认，请先按提示完成线下核验。
          </p>
        </div>
      </div>

      <ion-loading :is-open="isLoading" message="提交中..." :duration="0" />
      <ion-toast :is-open="showToast" :message="toastMessage" :duration="2500" @did-dismiss="showToast = false" />
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  IonButton,
  IonContent,
  IonInput,
  IonLoading,
  IonPage,
  IonToast
} from '@ionic/vue'
import { patientSession } from '@/app/session'
import { medicationService, patientService } from '@/modules/patient/api/mvp'
import { userDataService } from '@/services/userDataService'

const router = useRouter()
const isLoading = ref(false)
const showToast = ref(false)
const toastMessage = ref('')
const formData = reactive({
  name: '',
  mobile: patientSession.getMobile() ?? '',
  hospitalPatientId: ''
})

const normalizeMobile = (value: string) => value.replace(/\D/g, '').slice(0, 11)

const showMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}

const syncActivePlanState = async () => {
  try {
    const response = await medicationService.getActiveMedicationPlan()
    patientSession.setActivePlanStatus(Boolean(response.data))
  } catch {
    patientSession.setActivePlanStatus(false)
  }
}

const syncLocalProfile = (payload: {
  patientId?: number
  name: string
  phone: string
  idType: string
  identityMaterialArchived?: boolean
  realNameVerifiedTime?: string
}) => {
  userDataService.updateRealNameInfo({
    realName: payload.name,
    idType: payload.idType,
    ethnicity: userDataService.getUserProfile().ethnicity,
    mobile: payload.phone,
    patientId: payload.patientId,
    identityMaterialArchived: payload.identityMaterialArchived,
    realNameVerifiedTime: payload.realNameVerifiedTime
  })

  userDataService.updateProfile({
    patientId: payload.patientId,
    name: payload.name,
    phone: payload.phone,
    idType: payload.idType,
    isRealNameVerified: true,
    identityMaterialArchived: payload.identityMaterialArchived ?? true,
    realNameVerifiedTime: payload.realNameVerifiedTime
  })
}

const submitForm = async () => {
  const name = formData.name.trim()
  const mobile = normalizeMobile(formData.mobile)
  const hospitalPatientId = formData.hospitalPatientId.trim()

  if (!name) {
    showMessage('请输入患者姓名')
    return
  }

  if (!/^1\d{10}$/.test(mobile)) {
    showMessage('请输入正确的手机号')
    return
  }

  if (!hospitalPatientId) {
    showMessage('请输入院内患者编号')
    return
  }

  isLoading.value = true

  try {
    const response = await patientService.bindPatient({
      mobile,
      hospitalPatientId,
      name
    })
    const bindData = (response.data ?? {}) as { id?: number }

    patientSession.setMobile(mobile)
    patientSession.setBindingStatus(true)
    await syncActivePlanState()

    const verifiedAt = new Date().toISOString()
    let successMessage = '绑定成功'

    try {
      const profileResponse = await patientService.updateProfile({
        name,
        phone: mobile,
        idType: '院内患者编号',
        realNameVerified: true,
        realNameVerifiedTime: verifiedAt
      })

      syncLocalProfile({
        patientId: profileResponse.data?.patientId ?? bindData.id,
        name: profileResponse.data?.name || name,
        phone: profileResponse.data?.phone || mobile,
        idType: profileResponse.data?.idType || '院内患者编号',
        identityMaterialArchived: profileResponse.data?.identityMaterialArchived ?? true,
        realNameVerifiedTime: profileResponse.data?.realNameVerifiedTime || verifiedAt
      })
    } catch {
      syncLocalProfile({
        patientId: bindData.id,
        name,
        phone: mobile,
        idType: '院内患者编号',
        identityMaterialArchived: true,
        realNameVerifiedTime: verifiedAt
      })
      successMessage = '绑定成功，但档案回写失败，请稍后在个人资料页重试'
    }

    showMessage(successMessage)
    window.setTimeout(() => {
      router.replace('/tabs/home')
    }, 300)
  } catch (error: any) {
    showMessage(error?.message || '绑定失败')
  } finally {
    isLoading.value = false
  }
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }

  router.replace('/login')
}
</script>

<style scoped>
.bind-content {
  --background: linear-gradient(180deg, var(--patient-primary-light) 0%, var(--patient-bg-card) 38%, var(--patient-bg-page) 100%);
}

.bind-container {
  max-width: 520px;
  margin: 0 auto;
  padding: var(--patient-space-5) var(--patient-space-4) var(--patient-space-8);
}

.page-header {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: var(--patient-space-3);
  align-items: start;
  margin-bottom: var(--patient-space-5);
}

.back-btn {
  margin-top: 2px;
}

.eyebrow {
  margin: 0 0 var(--patient-space-2);
  color: var(--patient-primary);
  font-size: var(--patient-font-caption);
  font-weight: var(--patient-font-bold);
  letter-spacing: 0.08em;
}

.page-header h1 {
  margin: 0 0 var(--patient-space-2);
  font-size: var(--patient-font-page-title);
  color: var(--patient-text-primary);
}

.page-header p {
  margin: 0;
  color: var(--patient-text-secondary);
  line-height: var(--patient-leading-relaxed);
}

.form-card {
  padding: var(--patient-space-5) var(--patient-space-4);
  border-radius: var(--patient-radius-xl);
  background: var(--patient-bg-card);
  box-shadow: var(--patient-shadow-md);
}

.form-item {
  margin-bottom: var(--patient-space-5);
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-item label {
  display: block;
  margin-bottom: var(--patient-space-2);
  font-size: var(--patient-font-helper);
  font-weight: var(--patient-font-semibold);
  color: var(--patient-text-primary);
}

.form-input {
  --background: var(--patient-bg-page);
  --border-color: var(--patient-border-light);
  --border-radius: var(--patient-radius-lg);
  --padding-start: var(--patient-space-4);
  --padding-end: var(--patient-space-4);
  --padding-top: var(--patient-space-3);
  --padding-bottom: var(--patient-space-3);
  color: var(--patient-text-primary);
  border: 1px solid var(--patient-border-light);
  border-radius: var(--patient-radius-lg);
}

.submit-section {
  margin-top: var(--patient-space-5);
}

.submit-btn {
  --background: linear-gradient(135deg, var(--patient-primary), var(--patient-primary-dark));
  --border-radius: var(--patient-radius-lg);
  height: var(--patient-space-12);
  font-weight: var(--patient-font-bold);
}

.help-text {
  margin: var(--patient-space-3) 0 0;
  color: var(--patient-text-muted);
  font-size: var(--patient-font-caption);
  line-height: var(--patient-leading-relaxed);
}
</style>
