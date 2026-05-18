import request from '@/utils/request'
import type { DoraQuery, DoraForm } from '../types'

export function listDora(query: DoraQuery) {
  return request({ url: '/business/dora/list', method: 'get', params: query })
}

export function getDora(id: number | string) {
  return request({ url: '/business/dora/' + id, method: 'get' })
}

export function addDora(data: DoraForm) {
  return request({ url: '/business/dora', method: 'post', data })
}

export function updateDora(data: DoraForm) {
  return request({ url: '/business/dora', method: 'put', data })
}

export function delDora(ids: (number | string)[]) {
  return request({ url: '/business/dora/' + ids.join(','), method: 'delete' })
}

export function exportDora(query: DoraQuery) {
  return request({ url: '/business/dora/export', method: 'post', params: query, responseType: 'blob' })
}

export function aiGenerateDora(id: number | string) {
  return request({ url: `/business/dora/${id}/ai-generate`, method: 'post' })
}
