import request from '@/utils/request'
import type { TestCaseQuery, TestCaseForm, TestCaseExecuteRequest } from '../types'

export function listTestCase(query: TestCaseQuery) {
  return request({ url: '/business/testcase/list', method: 'get', params: query })
}
export function getTestCase(id: number | string) {
  return request({ url: '/business/testcase/' + id, method: 'get' })
}
export function addTestCase(data: TestCaseForm) {
  return request({ url: '/business/testcase', method: 'post', data })
}
export function updateTestCase(data: TestCaseForm) {
  return request({ url: '/business/testcase', method: 'put', data })
}
export function delTestCase(ids: (number | string)[]) {
  return request({ url: '/business/testcase/' + ids.join(','), method: 'delete' })
}
export function executeTestCase(id: number | string, data: TestCaseExecuteRequest) {
  return request({ url: '/business/testcase/' + id + '/execute', method: 'post', data })
}
