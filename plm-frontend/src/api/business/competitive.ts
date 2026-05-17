/** 竞品情报 API — PRD §F1.3 */
import request from '@/utils/request'

export interface Competitive {
  competitiveId?: number
  competitiveNo?: string
  projectId: number
  productName: string
  vendor?: string
  category?: string
  pricing?: string
  strengths?: string
  weaknesses?: string
  marketShare?: number
  threatLevel?: string
  aiGenerated?: 'Y' | 'N'
  aiAnalysis?: string
  aiGeneratedAt?: string
  status?: '00' | '01' | '02'
  authorUserId: number
  remark?: string
}

export interface CompetitiveQuery {
  pageNum?: number; pageSize?: number
  competitiveNo?: string; projectId?: number; productName?: string; vendor?: string
  category?: string; threatLevel?: string; status?: string
}

export const listCompetitive  = (q?: CompetitiveQuery) => request({ url: '/business/competitive/list', method: 'get', params: q })
export const getCompetitive   = (id: number) => request({ url: `/business/competitive/${id}`, method: 'get' })
export const addCompetitive   = (d: Competitive) => request({ url: '/business/competitive', method: 'post', data: d })
export const updateCompetitive= (d: Competitive) => request({ url: '/business/competitive', method: 'put', data: d })
export const delCompetitive   = (ids: number|number[]) => request({ url: `/business/competitive/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const aiAnalyzeCompetitive = (id: number) => request({ url: `/business/competitive/ai/analyze/${id}`, method: 'post' })
export const exportCompetitive    = (q?: CompetitiveQuery) => request({ url: '/business/competitive/export', method: 'post', params: q, responseType: 'blob' })
