import request from '@/utils/request'
import type { ReleaseQuery, ReleaseForm } from '../types'

export function listRelease(query: ReleaseQuery) {
  return request({ url: '/business/release/list', method: 'get', params: query })
}
export function getRelease(id: number | string) {
  return request({ url: '/business/release/' + id, method: 'get' })
}
export function addRelease(data: ReleaseForm) {
  return request({ url: '/business/release', method: 'post', data })
}
export function updateRelease(data: ReleaseForm) {
  return request({ url: '/business/release', method: 'put', data })
}
export function delRelease(ids: (number | string)[]) {
  return request({ url: '/business/release/' + ids.join(','), method: 'delete' })
}
