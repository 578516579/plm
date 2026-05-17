/** AI OpenSpec API — PRD §F3.5 */
import request from '@/utils/request'

export interface Openspec {
  openspecId?: number
  openspecNo?: string
  specName: string
  specType: string                    // openapi/asyncapi/ai_function/graphql
  description?: string
  specContent?: string
  version: string
  agriKbRef?: string
  aiGenerated?: 'Y' | 'N'
  aiGeneratedAt?: string
  status?: '00' | '01' | '02'         // 00草稿/01已发布/02已弃用
  authorUserId: number
  remark?: string
}

export interface OpenspecQuery {
  pageNum?: number; pageSize?: number
  openspecNo?: string; specName?: string; specType?: string; version?: string; status?: string
}

export const listOpenspec  = (q?: OpenspecQuery) => request({ url: '/business/openspec/list', method: 'get', params: q })
export const getOpenspec   = (id: number) => request({ url: `/business/openspec/${id}`, method: 'get' })
export const addOpenspec   = (d: Openspec) => request({ url: '/business/openspec', method: 'post', data: d })
export const updateOpenspec= (d: Openspec) => request({ url: '/business/openspec', method: 'put', data: d })
export const delOpenspec   = (ids: number|number[]) => request({ url: `/business/openspec/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const aiGenerateOpenspec = (id: number) => request({ url: `/business/openspec/ai/generate/${id}`, method: 'post' })
export const exportOpenspec     = (q?: OpenspecQuery) => request({ url: '/business/openspec/export', method: 'post', params: q, responseType: 'blob' })
