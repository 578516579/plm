import request from '@/utils/request'
import type { SubmissionQuery, SubmissionForm } from '../types'

export function listSubmission(query: SubmissionQuery) {
  return request({ url: '/business/submission/list', method: 'get', params: query })
}
export function getSubmission(id: number | string) {
  return request({ url: '/business/submission/' + id, method: 'get' })
}
export function addSubmission(data: SubmissionForm) {
  return request({ url: '/business/submission', method: 'post', data })
}
export function updateSubmission(data: SubmissionForm) {
  return request({ url: '/business/submission', method: 'put', data })
}
export function delSubmission(ids: (number | string)[]) {
  return request({ url: '/business/submission/' + ids.join(','), method: 'delete' })
}
