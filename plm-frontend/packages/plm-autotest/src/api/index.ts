import request from '@/utils/request'
import type { AutotestQuery, AutotestForm } from '../types'

export function listAutotest(query: AutotestQuery) {
  return request({ url: '/business/autotest/list', method: 'get', params: query })
}

export function getAutotest(id: number | string) {
  return request({ url: '/business/autotest/' + id, method: 'get' })
}

export function addAutotest(data: AutotestForm) {
  return request({ url: '/business/autotest', method: 'post', data })
}

export function updateAutotest(data: AutotestForm) {
  return request({ url: '/business/autotest', method: 'put', data })
}

export function delAutotest(ids: (number | string)[]) {
  return request({ url: '/business/autotest/' + ids.join(','), method: 'delete' })
}

export function exportAutotest(query: AutotestQuery) {
  return request({ url: '/business/autotest/export', method: 'post', params: query, responseType: 'blob' })
}

export function aiGenerateAutotest(id: number | string) {
  return request({ url: `/business/autotest/generate/${id}`, method: 'post' })
}
