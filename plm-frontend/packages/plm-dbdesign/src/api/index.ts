import request from '@/utils/request'
import type { DbdesignQuery, DbdesignForm } from '../types'

export function listDbdesign(query: DbdesignQuery) {
  return request({ url: '/business/dbdesign/list', method: 'get', params: query })
}

export function getDbdesign(id: number | string) {
  return request({ url: '/business/dbdesign/' + id, method: 'get' })
}

export function addDbdesign(data: DbdesignForm) {
  return request({ url: '/business/dbdesign', method: 'post', data })
}

export function updateDbdesign(data: DbdesignForm) {
  return request({ url: '/business/dbdesign', method: 'put', data })
}

export function delDbdesign(ids: (number | string)[]) {
  return request({ url: '/business/dbdesign/' + ids.join(','), method: 'delete' })
}

export function aiGenerateDbdesign(id: number | string) {
  return request({ url: `/business/dbdesign/ai/generate/${id}`, method: 'post' })
}
