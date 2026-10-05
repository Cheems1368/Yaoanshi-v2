<template>
  <ion-page>
    <ion-content class="login-content">
      <div class="login-container">
        <section class="hero-card">
          <p class="eyebrow">药按时 MVP</p>
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
    isBound = patientResponse.data?.bound === true
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

    patientSession.setDeviceId(response.data.deviceId || deviceId)
    patientSession.setMobile(mobile)

    const gateState = await syncGateState()
    showMessage('登录成功')

    window.setTimeout(() => {
      router.replace(gateState.isBound ? '/tabs/home' : '/realname')
    }, 300)
  } catch (error: any) {
    const messages: Record<string, string> = {
      APP_AUTH_OTP_EXPIRED: '验证码已过期，请重新获取',
      APP_AUTH_OTP_INVALID: '验证码不正确，请核对后重试'
    }
    showMessage(messages[error?.message] || error?.message || '登录失败，请重试')
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
  --background: var(--patient-bg-gradient);
}

.login-container {
  min-height: 100%;
  max-width: 520px;
  margin: 0 auto;
  padding: var(--patient-space-12) var(--patient-space-5) var(--patient-space-8);
}

.hero-card,
.form-card {
  background: var(--patient-bg-card);
  border-radius: var(--patient-radius-xl);
  box-shadow: var(--patient-shadow-sm);
}

.hero-card {
  padding: var(--patient-space-6) var(--patient-space-5);
  margin-bottom: var(--patient-space-4);
  border: var(--patient-border-light);
}

.eyebrow {
  margin: 0 0 var(--patient-space-3);
  color: var(--patient-primary);
  font-size: var(--patient-font-caption);
  font-weight: var(--patient-font-bold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-card h1 {
  margin: 0 0 var(--patient-space-3);
  font-size: var(--patient-font-page-title);
  color: var(--patient-text-primary);
  font-weight: var(--patient-font-bold);
  line-height: var(--patient-leading-tight);
}

.hero-text {
  margin: 0;
  color: var(--patient-text-secondary);
  line-height: var(--patient-leading-relaxed);
  font-size: var(--patient-font-body);
}

.form-card {
  padding: var(--patient-space-5);
}

.form-item {
  margin-bottom: var(--patient-space-4);
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
  --border-color: var(--patient-primary-light);
  --border-radius: var(--patient-radius-md);
  --padding-start: var(--patient-space-4);
  --padding-end: var(--patient-space-4);
  --padding-top: var(--patient-space-3);
  --padding-bottom: var(--patient-space-3);
  color: var(--patient-text-primary);
  border: var(--patient-border-default);
  border-radius: var(--patient-radius-md);
  font-size: var(--patient-font-body);
}

.otp-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: var(--patient-space-3);
}

.otp-button {
  min-width: var(--patient-space-10);
  --border-color: var(--patient-primary);
  --color: var(--patient-primary);
  --border-radius: var(--patient-radius-md);
  height: var(--patient-space-12);
}

.login-button {
  margin-top: var(--patient-space-2);
  --background: linear-gradient(135deg, var(--patient-primary), var(--patient-primary-dark));
  --border-radius: var(--patient-radius-lg);
  height: var(--patient-space-12);
  font-size: var(--patient-font-body);
  font-weight: var(--patient-font-bold);
}

.tips-text {
  margin: var(--patient-space-4) 0 0;
  color: var(--patient-text-secondary);
  font-size: var(--patient-font-caption);
  line-height: var(--patient-leading-relaxed);
}
</style>
