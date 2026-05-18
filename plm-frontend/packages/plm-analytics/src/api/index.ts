import request from '@/utils/request'
import type { AnalyticsQuery, AnalyticsForm } from '../types'

export function listAnalytics(query: AnalyticsQuery) {
  return request({ url: '/business/analytics/list', method: 'get', params: query })
}

export function getAnalytics(id: number | string) {
  return request({ url: '/business/analytics/' + id, method: 'get' })
}

export function addAnalytics(data: AnalyticsForm) {
  return request({ url: '/business/analytics', method: 'post', data })
}

export function updateAnalytics(data: AnalyticsForm) {
  return request({ url: '/business/analytics', method: 'put', data })
}

export function delAnalytics(ids: (number | string)[]) {
  return request({ url: '/business/analytics/' + ids.join(','), method: 'delete' })
}

export function exportAnalytics(query: AnalyticsQuery) {
  return request({ url: '/business/analytics/export', method: 'post', params: query, responseType: 'blob' })
}

export function aiGenerateAnalytics(id: number | string) {
  return request({ url: `/business/analytics/${id}/ai-generate`, method: 'post' })
}
