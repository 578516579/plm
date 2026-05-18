import request from '@/utils/request'
import type { ArchQuery, ArchForm } from '../types'

export function listArch(query: ArchQuery) {
  return request({ url: '/business/arch/list', method: 'get', params: query })
}
export function getArch(id: number | string) {
  return request({ url: '/business/arch/' + id, method: 'get' })
}
export function addArch(data: ArchForm) {
  return request({ url: '/business/arch', method: 'post', data })
}
export function updateArch(data: ArchForm) {
  return request({ url: '/business/arch', method: 'put', data })
}
export function delArch(ids: (number | string)[]) {
  return request({ url: '/business/arch/' + ids.join(','), method: 'delete' })
}
