import request from '@/utils/request'
import type { TestDataQuery, TestDataForm } from '../types'

export function listTestData(query: TestDataQuery) {
  return request({ url: '/business/testdata/list', method: 'get', params: query })
}
export function getTestData(id: number | string) {
  return request({ url: '/business/testdata/' + id, method: 'get' })
}
export function addTestData(data: TestDataForm) {
  return request({ url: '/business/testdata', method: 'post', data })
}
export function updateTestData(data: TestDataForm) {
  return request({ url: '/business/testdata', method: 'put', data })
}
export function delTestData(ids: (number | string)[]) {
  return request({ url: '/business/testdata/' + ids.join(','), method: 'delete' })
}
