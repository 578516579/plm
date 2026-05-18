import request from '@/utils/request'
import type { PipelineQuery, PipelineForm } from '../types'

export function listPipeline(query: PipelineQuery) {
  return request({ url: '/business/pipeline/list', method: 'get', params: query })
}

export function getPipeline(id: number | string) {
  return request({ url: '/business/pipeline/' + id, method: 'get' })
}

export function addPipeline(data: PipelineForm) {
  return request({ url: '/business/pipeline', method: 'post', data })
}

export function updatePipeline(data: PipelineForm) {
  return request({ url: '/business/pipeline', method: 'put', data })
}

export function delPipeline(ids: (number | string)[]) {
  return request({ url: '/business/pipeline/' + ids.join(','), method: 'delete' })
}

export function exportPipeline(query: PipelineQuery) {
  return request({ url: '/business/pipeline/export', method: 'post', params: query, responseType: 'blob' })
}
