import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { API_SUCCESS_CODES } from '@/utils/constants'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
})

service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
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
    const message = payload.msg || payload.message || '请求失败'

    if (API_SUCCESS_CODES.includes(code)) {
      return payload
    }

    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
  async (error) => {
    const status = error.response?.status
    const payload = error.response?.data || {}
    const message = payload.msg || payload.message || '请求失败'

    if (status === 401) {
      const userStore = useUserStore()
      await userStore.logoutAction()
      ElMessage.error('登录已过期，请重新登录')
      return Promise.reject(error)
    }

    if (status === 403) {
      ElMessage.error('没有权限访问该资源')
    } else if (status === 404) {
      ElMessage.error('请求的资源不存在')
    } else if (status >= 500) {
      ElMessage.error('服务端发生错误')
    } else {
      ElMessage.error(message)
    }

    return Promise.reject(error)
  }
)

export default service
