import request from '@/utils/request'
import type { TestReportQuery, TestReportForm } from '../types'

export function listTestReport(query: TestReportQuery) {
  return request({ url: '/business/testreport/list', method: 'get', params: query })
}
export function getTestReport(id: number | string) {
  return request({ url: '/business/testreport/' + id, method: 'get' })
}
export function addTestReport(data: TestReportForm) {
  return request({ url: '/business/testreport', method: 'post', data })
}
export function updateTestReport(data: TestReportForm) {
  return request({ url: '/business/testreport', method: 'put', data })
}
export function delTestReport(ids: (number | string)[]) {
  return request({ url: '/business/testreport/' + ids.join(','), method: 'delete' })
}
