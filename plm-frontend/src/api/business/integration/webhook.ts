import request from '@/utils/request'

export interface WebhookEvent {
  id?: number
  connectorId?: number
  eventType?: string
  externalEventId?: string
  payloadJson?: string
  signature?: string
  signatureVerified?: string
  processStatus?: string
  processError?: string
  retryCount?: number
  sourceIp?: string
  processTime?: string
  createTime?: string
}

export interface WebhookQuery {
  pageNum?: number
  pageSize?: number
  connectorId?: number
  eventType?: string
  signatureVerified?: string
  processStatus?: string
  params?: { beginTime?: string; endTime?: string }
}

export function listEvents(query: WebhookQuery) {
  return request({ url: '/business/integration/webhook/list', method: 'get', params: query })
}
export function getEvent(id: number) {
  return request({ url: '/business/integration/webhook/' + id, method: 'get' })
}
export function retryEvent(id: number) {
  return request({ url: `/business/integration/webhook/${id}/retry`, method: 'put' })
}
export function exportEvents(query: WebhookQuery) {
  return request({ url: '/business/integration/webhook/export', method: 'post', params: query, responseType: 'blob' })
}
