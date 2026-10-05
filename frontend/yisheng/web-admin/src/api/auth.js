import request from '@/utils/request'

// 登录
export function login(data) {
  return request({
    url: '/admin-api/system/auth/session/login',
    method: 'post',
    data,
    skipAuthHeader: true,
    skipAuthRefresh: true
  })
}

// 登出
export function logout() {
  return request({
    url: '/admin-api/system/auth/session/logout',
    method: 'post',
    headers: { 'X-Auth-Refresh': '1' },
    skipAuthHeader: true,
    skipAuthRefresh: true
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/admin-api/system/auth/get-permission-info',
    method: 'get'
  })
}

// 刷新token
export function refreshToken() {
  return request({
    url: '/admin-api/system/auth/session/refresh',
    method: 'post',
    headers: { 'X-Auth-Refresh': '1' },
    skipAuthHeader: true,
    skipAuthRefresh: true
  })
}

export function createNotificationSocketTicket() {
  return request({
    url: '/admin-api/notifications/socket-ticket',
    method: 'post',
    skipErrorToast: true
  })
}

export function getNotificationBaseline() {
  return request({ url: '/admin-api/notifications/me/baseline', method: 'get', skipErrorToast: true })
}

export function getPersonalNotifications(afterId, limit = 50) {
  return request({ url: '/admin-api/notifications/me/logs', method: 'get', params: { afterId, limit }, skipErrorToast: true })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/admin-api/system/auth/change-password',
    method: 'post',
    data
  })
}


