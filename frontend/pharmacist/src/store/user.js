import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import { login, logout, getUserInfo, refreshToken } from '@/api/auth'
import { API_SUCCESS_CODES } from '@/utils/constants'

const EMPTY_USER = {
  id: null,
  name: '',
  username: '',
  nickname: '',
  avatar: '',
  roles: [],
  permissions: []
}

const ROLE_DISPLAY_LABELS = {
  pharmacist: '药师',
  patient: '患者'
}

const derivePrimaryRole = (info = {}) => {
  const rawRoles = []

  if (info.role) {
    rawRoles.push(info.role)
  }

  if (Array.isArray(info.roles)) {
    rawRoles.push(...info.roles)
  } else if (info.roles) {
    rawRoles.push(info.roles)
  }

  const normalized = rawRoles
    .map((role) => (role || '').toString().toLowerCase())
    .filter(Boolean)

  if (normalized.includes('pharmacist') || normalized.includes('pharmacists')) {
    return 'pharmacist'
  }

  if (normalized.includes('super_admin') || normalized.includes('tenant_admin') || normalized.includes('admin')) {
    return 'pharmacist'
  }

  const nickname = (info.nickname || info.name || '').toString().toLowerCase()
  if (nickname.includes('药师') || nickname.includes('pharmacist')) {
    return 'pharmacist'
  }

  return 'pharmacist'
}

const normalizeUserInfo = (info = {}) => {
  const roles = Array.isArray(info.roles) ? info.roles : info.roles ? [info.roles] : []
  const permissions = Array.isArray(info.permissions)
    ? info.permissions
    : info.permissions
      ? [info.permissions]
      : []
  const primaryRole = derivePrimaryRole({ ...info, roles })

  return {
    id: info.id ?? null,
    name: info.nickname || info.name || info.username || ROLE_DISPLAY_LABELS[primaryRole] || '',
    username: info.username || '',
    nickname: info.nickname || info.name || '',
    avatar: info.avatar || '',
    roles,
    permissions
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const sessionStatus = ref('unknown')
  const userInfo = ref({ ...EMPTY_USER })
  const userRole = ref('')
  let refreshPromise = null

  const isLoggedIn = computed(() => sessionStatus.value === 'authenticated' && Boolean(token.value))
  const isDoctor = computed(() => false)
  const isPharmacist = computed(() => userRole.value === 'pharmacist')

  const resetUserState = () => {
    userInfo.value = { ...EMPTY_USER }
    userRole.value = ''
  }

  const setToken = (newToken) => {
    token.value = newToken || ''
    if (token.value) {
      sessionStatus.value = 'authenticated'
    }
  }

  const clearAuthState = () => {
    setToken('')
    sessionStatus.value = 'anonymous'
    resetUserState()
  }

  const refreshAccessToken = async () => {
    if (sessionStatus.value === 'anonymous') {
      return null
    }

    if (!refreshPromise) {
      refreshPromise = refreshToken()
        .then((response) => {
          if (!API_SUCCESS_CODES.includes(response.code)) {
            throw new Error(response.msg || response.message || 'Token 刷新失败')
          }

          const nextAccessToken = response.data?.accessToken || ''
          if (!nextAccessToken) {
            throw new Error('登录响应缺少访问令牌')
          }

          setToken(nextAccessToken)
          return nextAccessToken
        })
        .catch(() => {
          clearAuthState()
          return null
        })
        .finally(() => {
          refreshPromise = null
        })
    }

    return refreshPromise
  }

  const setUserInfo = (info = {}) => {
    const normalizedInfo = normalizeUserInfo(info)
    userRole.value = derivePrimaryRole(normalizedInfo)
    userInfo.value = normalizedInfo
  }

  const loginAction = async (loginData) => {
    try {
      const response = await login(loginData)

      if (!API_SUCCESS_CODES.includes(response.code)) {
        ElMessage.error(response.msg || response.message || '登录失败')
        return false
      }

      const { accessToken } = response.data || {}
      if (!accessToken) {
        throw new Error('登录响应缺少访问令牌')
      }

      setToken(accessToken)

      await initUserInfo()
      ElMessage.success('登录成功')
      return true
    } catch (error) {
      console.error('登录错误:', error)
      ElMessage.error(error.message || '登录失败')
      return false
    }
  }

  const logoutAction = async (options = {}) => {
    const { silent = false } = options

    try {
      await logout()
    } catch (error) {
      console.error('退出请求失败:', error)
    } finally {
      clearAuthState()
      if (!silent) {
        ElMessage.success('已退出登录')
      }
    }
  }

  const initUserInfo = async () => {
    if (!token.value) {
      const restoredToken = await refreshAccessToken()
      if (!restoredToken) {
        return
      }
    }

    try {
      const response = await getUserInfo()
      if (!API_SUCCESS_CODES.includes(response.code)) {
        throw new Error(response.msg || response.message || '获取用户信息失败')
      }

      const { user, roles, permissions } = response.data || {}
      if (!user) {
        throw new Error('未获取到用户信息')
      }

      setUserInfo({
        id: user.id ?? null,
        username: user.username || '',
        nickname: user.nickname || '',
        name: user.nickname || user.username || '',
        avatar: user.avatar || '',
        roles: Array.isArray(roles) ? roles : roles ? Array.from(roles) : [],
        permissions: Array.isArray(permissions) ? permissions : permissions ? Array.from(permissions) : []
      })
    } catch (error) {
      console.error('获取用户信息失败:', error)
      await logoutAction({ silent: true })
    }
  }

  const restoreSession = async () => {
    if (token.value) {
      return true
    }
    return Boolean(await refreshAccessToken())
  }

  const updateUserInfo = (newInfo) => {
    setUserInfo({ ...userInfo.value, ...newInfo })
  }

  return {
    token,
    sessionStatus,
    userInfo,
    userRole,
    isLoggedIn,
    isDoctor,
    isPharmacist,
    setToken,
    refreshAccessToken,
    restoreSession,
    setUserInfo,
    loginAction,
    logoutAction,
    initUserInfo,
    updateUserInfo
  }
})
