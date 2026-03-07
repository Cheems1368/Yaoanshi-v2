import request from '@/utils/request'

const backendV2BaseUrl = import.meta.env.VITE_BACKEND_V2_API_BASE_URL || ''

export function getWorkOrders(params) {
  return request({
    baseURL: backendV2BaseUrl,
    url: '/admin-api/help-orders',
    method: 'get',
    params
  })
}

export function getWorkOrderDetail(id) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/help-orders/${id}`,
    method: 'get'
  })
}

export function claimWorkOrder(id) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/help-orders/${id}/claim`,
    method: 'post'
  })
}

export function replyWorkOrder(id, data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/help-orders/${id}/reply`,
    method: 'post',
    data
  })
}

export function escalateWorkOrder(id, data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/help-orders/${id}/escalate`,
    method: 'post',
    data
  })
}

export function closeWorkOrder(id, data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/help-orders/${id}/close`,
    method: 'post',
    data
  })
}

export function rejectWorkOrder(id, data) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/help-orders/${id}/reject`,
    method: 'post',
    data
  })
}
