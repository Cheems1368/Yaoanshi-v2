import request from '@/utils/request'

// 登录
export function login(data) {
  return request({
    url: '/admin-api/system/auth/login',
    method: 'post',
    data
  })
}

// 登出
export function logout() {
  return request({
    url: '/admin-api/system/auth/logout',
    method: 'post'
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
    url: '/admin-api/system/auth/refresh-token',
    method: 'post'
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/admin-api/system/auth/change-password',
    method: 'post',
    data
  })
}



