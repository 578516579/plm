import request from '@/utils/request'
import type { OpenspecQuery, OpenspecForm } from '../types'

export function listOpenspec(query: OpenspecQuery) {
  return request({ url: '/business/openspec/list', method: 'get', params: query })
}

export function getOpenspec(id: number | string) {
  return request({ url: '/business/openspec/' + id, method: 'get' })
}

export function addOpenspec(data: OpenspecForm) {
  return request({ url: '/business/openspec', method: 'post', data })
}

export function updateOpenspec(data: OpenspecForm) {
  return request({ url: '/business/openspec', method: 'put', data })
}

export function delOpenspec(ids: (number | string)[]) {
  return request({ url: '/business/openspec/' + ids.join(','), method: 'delete' })
}

export function exportOpenspec(query: OpenspecQuery) {
  return request({ url: '/business/openspec/export', method: 'post', params: query, responseType: 'blob' })
}

export function aiGenerateOpenspec(id: number | string) {
  return request({ url: `/business/openspec/ai/generate/${id}`, method: 'post' })
}
