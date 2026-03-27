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

export function getSelfReportedMedicationsForWorkOrder(workOrderId) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/help-orders/${workOrderId}/self-reported-medications`,
    method: 'get'
  })
}

export function acknowledgeSelfReportedMedication(workOrderId, srmId) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/help-orders/${workOrderId}/self-reported-medications/${srmId}/acknowledge`,
    method: 'post'
  })
}

export function rejectSelfReportedMedication(workOrderId, srmId, reason) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/help-orders/${workOrderId}/self-reported-medications/${srmId}/reject`,
    method: 'post',
    data: { reason }
  })
}

export function getSelfReportedMedicationTransferPrefill(workOrderId, srmId) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/help-orders/${workOrderId}/self-reported-medications/${srmId}/transfer-prefill`,
    method: 'get'
  })
}

export function markSelfReportedMedicationTransferred(workOrderId, srmId, planItemId) {
  return request({
    baseURL: backendV2BaseUrl,
    url: `/admin-api/help-orders/${workOrderId}/self-reported-medications/${srmId}/mark-transferred`,
    method: 'post',
    params: { planItemId }
  })
}
