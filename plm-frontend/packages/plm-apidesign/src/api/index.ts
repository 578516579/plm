import request from '@/utils/request'
import type { ApiDesignQuery, ApiDesignForm } from '../types'

export function listApiDesign(query: ApiDesignQuery) {
  return request({ url: '/business/apidesign/list', method: 'get', params: query })
}
export function getApiDesign(id: number | string) {
  return request({ url: '/business/apidesign/' + id, method: 'get' })
}
export function addApiDesign(data: ApiDesignForm) {
  return request({ url: '/business/apidesign', method: 'post', data })
}
export function updateApiDesign(data: ApiDesignForm) {
  return request({ url: '/business/apidesign', method: 'put', data })
}
export function delApiDesign(ids: (number | string)[]) {
  return request({ url: '/business/apidesign/' + ids.join(','), method: 'delete' })
}
