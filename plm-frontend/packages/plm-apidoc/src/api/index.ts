import request from '@/utils/request'
import type { ApiDocQuery, ApiDocForm } from '../types'

export function listApiDoc(query: ApiDocQuery) {
  return request({ url: '/business/apidoc/list', method: 'get', params: query })
}
export function getApiDoc(id: number | string) {
  return request({ url: '/business/apidoc/' + id, method: 'get' })
}
export function addApiDoc(data: ApiDocForm) {
  return request({ url: '/business/apidoc', method: 'post', data })
}
export function updateApiDoc(data: ApiDocForm) {
  return request({ url: '/business/apidoc', method: 'put', data })
}
export function delApiDoc(ids: (number | string)[]) {
  return request({ url: '/business/apidoc/' + ids.join(','), method: 'delete' })
}
