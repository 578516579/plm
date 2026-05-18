import request from '@/utils/request'
import type { ManualOpsQuery, ManualOpsForm } from '../types'

export function listManualOps(query: ManualOpsQuery) {
  return request({ url: '/business/manual-ops/list', method: 'get', params: query })
}

export function getManualOps(id: number | string) {
  return request({ url: '/business/manual-ops/' + id, method: 'get' })
}

export function addManualOps(data: ManualOpsForm) {
  return request({ url: '/business/manual-ops', method: 'post', data })
}

export function updateManualOps(data: ManualOpsForm) {
  return request({ url: '/business/manual-ops', method: 'put', data })
}

export function delManualOps(ids: (number | string)[]) {
  return request({ url: '/business/manual-ops/' + ids.join(','), method: 'delete' })
}

export function exportManualOps(query: ManualOpsQuery) {
  return request({ url: '/business/manual-ops/export', method: 'post', params: query, responseType: 'blob' })
}

export function aiGenerateManualOps(id: number | string) {
  return request({ url: `/business/manual-ops/ai-generate/${id}`, method: 'post' })
}
