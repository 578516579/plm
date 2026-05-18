import request from '@/utils/request'
import type { UedQuery, UedForm } from '../types'

export function listUed(query: UedQuery) {
  return request({ url: '/business/ued/list', method: 'get', params: query })
}

export function getUed(id: number | string) {
  return request({ url: '/business/ued/' + id, method: 'get' })
}

export function addUed(data: UedForm) {
  return request({ url: '/business/ued', method: 'post', data })
}

export function updateUed(data: UedForm) {
  return request({ url: '/business/ued', method: 'put', data })
}

export function delUed(ids: (number | string)[]) {
  return request({ url: '/business/ued/' + ids.join(','), method: 'delete' })
}

export function aiReviewUed(id: number | string) {
  return request({ url: `/business/ued/ai/generate/${id}`, method: 'post' })
}
