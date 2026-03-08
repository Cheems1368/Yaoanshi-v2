import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { API_SUCCESS_CODES } from '@/utils/constants'

const DEFAULT_ERROR_MESSAGE = '请求失败'

const extractMessage = (payload, fallback = DEFAULT_ERROR_MESSAGE) => {
  if (!payload || typeof payload !== 'object') {
    return fallback
  }

  const candidates = [payload.msg, payload.message, payload.error, payload.detail]
  const firstReadable = candidates.find((item) => typeof item === 'string' && item.trim())
  return firstReadable || fallback
}

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
})

service.interceptors.request.use(
  async (config) => {
    const userStore = useUserStore()
    let accessToken = userStore.token
    config.headers = config.headers || {}

    if (!accessToken && !config.skipAuthHeader && !config.skipAuthRefresh) {
      accessToken = await userStore.refreshAccessToken()
    }

    if (accessToken && !config.skipAuthHeader) {
      config.headers.Authorization = `Bearer ${accessToken}`
    }
    config.headers['tenant-id'] = '1'
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const payload = response.data || {}
    const code = payload.code
    const message = extractMessage(payload)

    if (API_SUCCESS_CODES.includes(code)) {
      return payload
    }

    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
  async (error) => {
    const status = error.response?.status
    const payload = error.response?.data || {}
    const message = extractMessage(payload)

    if (status === 401) {
      const originalRequest = error.config || {}
      const userStore = useUserStore()

      if (!originalRequest._retry && !originalRequest.skipAuthRefresh) {
        originalRequest._retry = true
        const nextAccessToken = await userStore.refreshAccessToken()

        if (nextAccessToken) {
          originalRequest.headers = {
            ...(originalRequest.headers || {}),
            Authorization: `Bearer ${nextAccessToken}`
          }
          return service(originalRequest)
        }
      }

      await userStore.logoutAction({ silent: true })
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(error)
    }

    if (status === 403) {
      ElMessage.error('没有权限访问该资源')
    } else if (status === 404) {
      ElMessage.error('请求的资源不存在')
    } else if (status >= 500) {
      ElMessage.error(message === DEFAULT_ERROR_MESSAGE ? '服务端发生错误' : message)
    } else {
      ElMessage.error(message)
    }

    return Promise.reject(error)
  }
)

export default service
