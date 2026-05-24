/**
 * OpenSpec API — PRD §F3.5 + 原型 aispec.html
 */
import request from '@/utils/request'

export interface OpenSpec {
  openspecId?: number
  openspecNo?: string
  specName: string
  specType?: string  // openapi_31 / asyncapi_30 / ai_function_spec / graphql
  description?: string
  specVersion?: string
  specContent?: string  // YAML / JSON
  aiGenerated?: string
  status?: string
  authorUserId?: number
}

export interface OpenSpecQuery { pageNum?: number; pageSize?: number; specType?: string; specName?: string; status?: string }

export const listOpenSpec = (q: OpenSpecQuery): Promise<any> => request({ url: '/business/openspec/list', method: 'get', params: q })
export const getOpenSpec = (id: number): Promise<any> => request({ url: `/business/openspec/${id}`, method: 'get' })
export const addOpenSpec = (d: OpenSpec): Promise<any> => request({ url: '/business/openspec', method: 'post', data: d })
export const updateOpenSpec = (d: OpenSpec): Promise<any> => request({ url: '/business/openspec', method: 'put', data: d })
export const delOpenSpec = (ids: number | number[]): Promise<any> => request({ url: `/business/openspec/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const aiGenerateOpenSpec = (id: number): Promise<any> => request({ url: `/business/openspec/ai/generate/${id}`, method: 'post' })
