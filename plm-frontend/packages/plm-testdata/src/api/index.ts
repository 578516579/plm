import request from '@/utils/request'
import type { TestdataQuery, TestdataForm } from '../types'

export function listTestdata(query: TestdataQuery) {
  return request({ url: '/business/testdata/list', method: 'get', params: query })
}

export function getTestdata(id: number | string) {
  return request({ url: '/business/testdata/' + id, method: 'get' })
}

export function addTestdata(data: TestdataForm) {
  return request({ url: '/business/testdata', method: 'post', data })
}

export function updateTestdata(data: TestdataForm) {
  return request({ url: '/business/testdata', method: 'put', data })
}

export function delTestdata(ids: (number | string)[]) {
  return request({ url: '/business/testdata/' + ids.join(','), method: 'delete' })
}

export function aiGenerateTestdata(id: number | string) {
  return request({ url: `/business/testdata/ai/generate/${id}`, method: 'post' })
}
