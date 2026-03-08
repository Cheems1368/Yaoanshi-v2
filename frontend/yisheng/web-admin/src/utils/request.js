import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { API_SUCCESS_CODES } from '@/utils/constants'

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
    const message = payload.msg || payload.message || '璇锋眰澶辫触'

    if (API_SUCCESS_CODES.includes(code)) {
      return payload
    }

    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
  async (error) => {
    const status = error.response?.status
    const payload = error.response?.data || {}
    const message = payload.msg || payload.message || '璇锋眰澶辫触'

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
      ElMessage.error('鐧诲綍宸茶繃鏈燂紝璇烽噸鏂扮櫥褰?')
      return Promise.reject(error)
    }

    if (status === 403) {
      ElMessage.error('娌℃湁鏉冮檺璁块棶璇ヨ祫婧?')
    } else if (status === 404) {
      ElMessage.error('璇锋眰鐨勮祫婧愪笉瀛樺湪')
    } else if (status >= 500) {
      ElMessage.error('鏈嶅姟绔彂鐢熼敊璇?')
    } else {
      ElMessage.error(message)
    }

    return Promise.reject(error)
  }
)

export default service
