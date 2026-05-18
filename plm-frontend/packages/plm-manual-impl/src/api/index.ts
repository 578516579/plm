import request from '@/utils/request'
import type { ManualImplQuery, ManualImplForm } from '../types'

export function listManualImpl(query: ManualImplQuery) {
  return request({ url: '/business/manual-impl/list', method: 'get', params: query })
}

export function getManualImpl(id: number | string) {
  return request({ url: '/business/manual-impl/' + id, method: 'get' })
}

export function addManualImpl(data: ManualImplForm) {
  return request({ url: '/business/manual-impl', method: 'post', data })
}

export function updateManualImpl(data: ManualImplForm) {
  return request({ url: '/business/manual-impl', method: 'put', data })
}

export function delManualImpl(ids: (number | string)[]) {
  return request({ url: '/business/manual-impl/' + ids.join(','), method: 'delete' })
}

export function exportManualImpl(query: ManualImplQuery) {
  return request({ url: '/business/manual-impl/export', method: 'post', params: query, responseType: 'blob' })
}

export function aiGenerateManualImpl(id: number | string) {
  return request({ url: `/business/manual-impl/ai-generate/${id}`, method: 'post' })
}
