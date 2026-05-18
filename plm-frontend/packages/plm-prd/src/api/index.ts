import request from '@/utils/request'
import type { PrdQuery, PrdForm } from '../types'

export function listPrd(query: PrdQuery) {
  return request({ url: '/business/prd/list', method: 'get', params: query })
}

export function getPrd(id: number | string) {
  return request({ url: '/business/prd/' + id, method: 'get' })
}

export function addPrd(data: PrdForm) {
  return request({ url: '/business/prd', method: 'post', data })
}

export function updatePrd(data: PrdForm) {
  return request({ url: '/business/prd', method: 'put', data })
}

export function delPrd(ids: (number | string)[]) {
  return request({ url: '/business/prd/' + ids.join(','), method: 'delete' })
}

export function aiGeneratePrd(id: number | string) {
  return request({ url: `/business/prd/ai/generate/${id}`, method: 'post' })
}
