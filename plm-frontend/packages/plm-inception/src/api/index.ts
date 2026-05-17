import request from '@/utils/request'
import type { InceptionQuery, InceptionForm } from '../types'

export function listInception(query: InceptionQuery) {
  return request({ url: '/business/inception/list', method: 'get', params: query })
}

export function getInception(id: number | string) {
  return request({ url: '/business/inception/' + id, method: 'get' })
}

export function addInception(data: InceptionForm) {
  return request({ url: '/business/inception', method: 'post', data })
}

export function updateInception(data: InceptionForm) {
  return request({ url: '/business/inception', method: 'put', data })
}

export function delInception(ids: (number | string)[]) {
  return request({ url: '/business/inception/' + ids.join(','), method: 'delete' })
}

/** PRD §F1.1 — 触发 AI 生成立项建议书（project-inception-flow） */
export function aiGenerateInception(id: number | string) {
  return request({ url: `/business/inception/ai/generate/${id}`, method: 'post' })
}
