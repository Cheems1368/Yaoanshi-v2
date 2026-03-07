<template>
  <ion-page>
    <ion-content class="login-content">
      <div class="login-container">
        <section class="hero-card">
          <p class="eyebrow">药安师 MVP</p>
          <h1>患者登录</h1>
          <p class="hero-text">
            请输入手机号和验证码登录，完成患者绑定后即可进入用药流程。
          </p>
        </section>

        <section class="form-card">
          <div class="form-item">
            <label for="mobile">手机号</label>
            <ion-input
              id="mobile"
              v-model="formData.mobile"
              type="tel"
              inputmode="numeric"
              :maxlength="11"
              placeholder="请输入 11 位手机号"
              class="form-input"
            />
          </div>

          <div class="form-item">
            <label for="otpCode">验证码</label>
            <div class="otp-row">
              <ion-input
                id="otpCode"
                v-model="formData.otpCode"
                type="tel"
                inputmode="numeric"
                :maxlength="6"
                placeholder="请输入短信验证码"
                class="form-input"
              />
              <ion-button
                class="otp-button"
                fill="outline"
                :disabled="otpSending || otpCountdown > 0 || !canSendOtp"
                @click="handleSendOtp"
              >
                {{ otpCountdown > 0 ? `${otpCountdown}s` : '发送验证码' }}
              </ion-button>
            </div>
          </div>

          <ion-button expand="block" class="login-button" :disabled="isLoading || !canLogin" @click="handleLogin">
            登录
          </ion-button>

          <p class="tips-text">
            登录后系统会自动检查患者绑定状态和当前生效的用药计划。
          </p>
        </section>
      </div>

      <ion-loading :is-open="isLoading" message="登录中..." :duration="0" />
      <ion-toast :is-open="showToast" :message="toastMessage" :duration="2500" @did-dismiss="showToast = false" />
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { IonButton, IonContent, IonInput, IonLoading, IonPage, IonToast } from '@ionic/vue'
import { patientSession } from '@/app/session'
import { appAuthService, medicationService, patientService } from '@/modules/patient/api/mvp'

const OTP_COUNTDOWN_SECONDS = 60

const router = useRouter()
const isLoading = ref(false)
const otpSending = ref(false)
const otpCountdown = ref(0)
const showToast = ref(false)
const toastMessage = ref('')
const formData = reactive({
  mobile: patientSession.getMobile() ?? '',
  otpCode: ''
})

let countdownTimer: number | null = null

const normalizedMobile = computed(() => formData.mobile.replace(/\D/g, '').slice(0, 11))
const normalizedOtpCode = computed(() => formData.otpCode.replace(/\D/g, '').slice(0, 6))
const canSendOtp = computed(() => /^1\d{10}$/.test(normalizedMobile.value))
const canLogin = computed(() => canSendOtp.value && normalizedOtpCode.value.length >= 4)

const showMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}

const stopCountdown = () => {
  if (countdownTimer) {
    window.clearInterval(countdownTimer)
    countdownTimer = null
  }
}

const startCountdown = () => {
  stopCountdown()
  otpCountdown.value = OTP_COUNTDOWN_SECONDS
  countdownTimer = window.setInterval(() => {
    if (otpCountdown.value <= 1) {
      otpCountdown.value = 0
      stopCountdown()
      return
    }

    otpCountdown.value -= 1
  }, 1000)
}

const syncGateState = async () => {
  let isBound = false

  try {
    const patientResponse = await patientService.getCurrentPatient()
    isBound = Boolean(patientResponse.data)
  } catch {
    isBound = false
  }

  patientSession.setBindingStatus(isBound)

  let hasActivePlan = false
  if (isBound) {
    try {
      const planResponse = await medicationService.getActiveMedicationPlan()
      hasActivePlan = Boolean(planResponse.data)
    } catch {
      hasActivePlan = false
    }
  }

  patientSession.setActivePlanStatus(hasActivePlan)
  return { isBound, hasActivePlan }
}

const handleSendOtp = async () => {
  if (!canSendOtp.value) {
    showMessage('请输入正确的手机号')
    return
  }

  otpSending.value = true

  try {
    await appAuthService.sendOtp(normalizedMobile.value)
    patientSession.setMobile(normalizedMobile.value)
    formData.mobile = normalizedMobile.value
    showMessage('验证码发送成功')
    startCountdown()
  } catch (error: any) {
    showMessage(error?.message || '验证码发送失败')
  } finally {
    otpSending.value = false
  }
}

const handleLogin = async () => {
  if (!canLogin.value) {
    showMessage('请输入手机号和验证码')
    return
  }

  isLoading.value = true

  try {
    const mobile = normalizedMobile.value
    const otpCode = normalizedOtpCode.value
    const deviceId = patientSession.ensureDeviceId()
    const response = await appAuthService.login({
      mobile,
      otpCode,
      deviceId,
      deviceName: 'patient-web',
      platform: 'web'
    })

    patientSession.setAccessToken(response.data.accessToken)
    patientSession.setRefreshToken(response.data.refreshToken)
    patientSession.setDeviceId(response.data.deviceId || deviceId)
    patientSession.setMobile(mobile)

    const gateState = await syncGateState()
    showMessage('登录成功')

    window.setTimeout(() => {
      router.replace(gateState.isBound ? '/tabs/home' : '/realname')
    }, 300)
  } catch (error: any) {
    showMessage(error?.message || '登录失败')
  } finally {
    isLoading.value = false
  }
}

onBeforeUnmount(() => {
  stopCountdown()
})
</script>

<style scoped>
.login-content {
  --background: linear-gradient(180deg, #eff6ff 0%, #ffffff 42%, #f8fafc 100%);
}

.login-container {
  min-height: 100%;
  max-width: 520px;
  margin: 0 auto;
  padding: 48px 20px 32px;
}

.hero-card,
.form-card {
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 18px 40px rgba(37, 99, 235, 0.08);
}

.hero-card {
  padding: 28px 24px;
  margin-bottom: 18px;
  border: 1px solid rgba(37, 99, 235, 0.12);
}

.eyebrow {
  margin: 0 0 12px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-card h1 {
  margin: 0 0 12px;
  font-size: 30px;
  color: #0f172a;
}

.hero-text {
  margin: 0;
  color: #475569;
  line-height: 1.7;
}

.form-card {
  padding: 24px 20px;
}

.form-item {
  margin-bottom: 18px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
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

.otp-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.otp-button {
  min-width: 122px;
  --border-color: #2563eb;
  --color: #2563eb;
  --border-radius: 14px;
}

.login-button {
  margin-top: 8px;
  --background: linear-gradient(135deg, #2563eb, #1d4ed8);
  --border-radius: 16px;
  height: 48px;
  font-weight: 700;
}

.tips-text {
  margin: 16px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}
</style>
