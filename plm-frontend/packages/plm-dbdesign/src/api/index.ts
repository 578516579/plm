import request from '@/utils/request'
import type { DbDesignQuery, DbDesignForm } from '../types'

export function listDbDesign(query: DbDesignQuery) {
  return request({ url: '/business/dbdesign/list', method: 'get', params: query })
}
export function getDbDesign(id: number | string) {
  return request({ url: '/business/dbdesign/' + id, method: 'get' })
}
export function addDbDesign(data: DbDesignForm) {
  return request({ url: '/business/dbdesign', method: 'post', data })
}
export function updateDbDesign(data: DbDesignForm) {
  return request({ url: '/business/dbdesign', method: 'put', data })
}
export function delDbDesign(ids: (number | string)[]) {
  return request({ url: '/business/dbdesign/' + ids.join(','), method: 'delete' })
}
