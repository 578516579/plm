import request from '@/utils/request'
import type { TestPlanQuery, TestPlanForm } from '../types'

export function listTestPlan(query: TestPlanQuery) {
  return request({ url: '/business/testplan/list', method: 'get', params: query })
}
export function getTestPlan(id: number | string) {
  return request({ url: '/business/testplan/' + id, method: 'get' })
}
export function addTestPlan(data: TestPlanForm) {
  return request({ url: '/business/testplan', method: 'post', data })
}
export function updateTestPlan(data: TestPlanForm) {
  return request({ url: '/business/testplan', method: 'put', data })
}
export function delTestPlan(ids: (number | string)[]) {
  return request({ url: '/business/testplan/' + ids.join(','), method: 'delete' })
}
