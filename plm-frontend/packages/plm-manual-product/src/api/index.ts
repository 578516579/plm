import request from '@/utils/request'
import type { ManualProductQuery, ManualProductForm } from '../types'

export function listManualProduct(query: ManualProductQuery) {
  return request({ url: '/business/manual-product/list', method: 'get', params: query })
}
export function getManualProduct(id: number | string) {
  return request({ url: '/business/manual-product/' + id, method: 'get' })
}
export function addManualProduct(data: ManualProductForm) {
  return request({ url: '/business/manual-product', method: 'post', data })
}
export function updateManualProduct(data: ManualProductForm) {
  return request({ url: '/business/manual-product', method: 'put', data })
}
export function delManualProduct(ids: (number | string)[]) {
  return request({ url: '/business/manual-product/' + ids.join(','), method: 'delete' })
}
