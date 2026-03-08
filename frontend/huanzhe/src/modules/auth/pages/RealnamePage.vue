<template>
  <ion-page>
    <ion-content class="bind-content">
      <div class="bind-container">
        <header class="page-header">
          <ion-button fill="clear" class="back-btn" @click="goBack">
            <ion-icon :icon="chevronBackOutline" />
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
  IonIcon,
  IonInput,
  IonLoading,
  IonPage,
  IonToast
} from '@ionic/vue'
import { chevronBackOutline } from 'ionicons/icons'
import { patientSession } from '@/app/session'
import { medicationService, patientService } from '@/modules/patient/api/mvp'
import { patientApiService } from '@/modules/patient/api/patientApi'
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
      const profileResponse = await patientApiService.updatePatientInfo({
        name,
        phone: mobile,
        idType: '院内患者编号',
        idCard: hospitalPatientId,
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
  --background: linear-gradient(180deg, #dbeafe 0%, #ffffff 38%, #f8fafc 100%);
}

.bind-container {
  max-width: 520px;
  margin: 0 auto;
  padding: 20px 16px 32px;
}

.page-header {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 12px;
  align-items: start;
  margin-bottom: 20px;
}

.back-btn {
  margin-top: 2px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.page-header h1 {
  margin: 0 0 8px;
  font-size: 28px;
  color: #0f172a;
}

.page-header p {
  margin: 0;
  color: #475569;
  line-height: 1.6;
}

.form-card {
  padding: 18px 16px;
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 12px 24px rgba(15, 36, 80, 0.08);
}

.form-item {
  margin-bottom: 18px;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.form-input {
  --background: #f8fafc;
  --border-color: #dbeafe;
  --border-radius: 14px;
  --padding-start: 14px;
  --padding-end: 14px;
  --padding-top: 12px;
  --padding-bottom: 12px;
  color: #0f172a;
  border: 1px solid #dbeafe;
  border-radius: 14px;
}

.submit-section {
  margin-top: 20px;
}

.submit-btn {
  --background: linear-gradient(135deg, #2563eb, #1d4ed8);
  --border-radius: 14px;
  height: 48px;
  font-weight: 700;
}

.help-text {
  margin: 12px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}
</style>
