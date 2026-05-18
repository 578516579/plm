import request from '@/utils/request'
import type { ApidesignQuery, ApidesignForm } from '../types'

export function listApidesign(query: ApidesignQuery) {
  return request({ url: '/business/apidesign/list', method: 'get', params: query })
}

export function getApidesign(id: number | string) {
  return request({ url: '/business/apidesign/' + id, method: 'get' })
}

export function addApidesign(data: ApidesignForm) {
  return request({ url: '/business/apidesign', method: 'post', data })
}

export function updateApidesign(data: ApidesignForm) {
  return request({ url: '/business/apidesign', method: 'put', data })
}

export function delApidesign(ids: (number | string)[]) {
  return request({ url: '/business/apidesign/' + ids.join(','), method: 'delete' })
}

export function aiGenerateApidesign(id: number | string) {
  return request({ url: `/business/apidesign/ai/generate/${id}`, method: 'post' })
}
