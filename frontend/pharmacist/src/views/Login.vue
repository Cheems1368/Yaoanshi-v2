<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon size="48" color="var(--pro-brand-primary)">
          <FirstAidKit />
        </el-icon>
        <h1>药师工作台</h1>
        <p>登录后进入药师端 MVP 入口，处理患者、处方和帮助工单。</p>
      </div>

      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            size="large"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>固定以药师角色进入，不再显示非 MVP 角色分流。</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { FirstAidKit, Lock, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度需在 3 到 20 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需在 6 到 20 个字符之间', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true

    const success = await userStore.loginAction({
      username: loginForm.username,
      password: loginForm.password,
      role: 'pharmacist'
    })

    if (success) {
      await router.push('/dashboard')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  padding: 20px;
  display: grid;
  place-items: center;
  background: linear-gradient(180deg, var(--pro-brand-secondary) 0%, var(--pro-bg-primary) 48%, var(--pro-bg-secondary) 100%);
}

.login-box {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  border-radius: var(--pro-radius-lg);
  background: var(--pro-bg-primary);
  box-shadow: var(--pro-shadow-md);
}

.login-header {
  margin-bottom: 28px;
  text-align: center;
}

.login-header h1 {
  margin: 12px 0 8px;
  font-size: 28px;
  color: var(--pro-text-primary);
  font-family: var(--pro-title-font-family);
}

.login-header p,
.login-footer {
  margin: 0;
  color: var(--pro-text-secondary);
  font-size: 14px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.login-footer {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 480px) {
  .login-box {
    padding: 28px 20px;
  }
}
</style>
