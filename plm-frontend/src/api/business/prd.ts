/** PRD 文档 API — PRD §F2.2 + 原型 prd.html (含 JS generatePRD 4 段输出) */
import request from '@/utils/request'

export interface PrdUserStory { role: string; want: string; why: string }
export interface PrdCoreFeature { code: string; name: string; description: string }
export interface PrdAcceptance { category: string; criterion: string; target: string }

export interface Prd {
  prdId?: number
  prdNo?: string
  projectId: number
  title: string
  description?: string
  sceneTemplate?: string
  targetUser?: string
  content?: string
  // 4 段 AI 结构化输出 (原型 generatePRD 的 4 个 <h4>)
  aiBackground?: string
  aiUserStories?: string         // JSON 字符串 → PrdUserStory[]
  aiCoreFeatures?: string        // JSON 字符串 → PrdCoreFeature[]
  aiAcceptance?: string          // JSON 字符串 → PrdAcceptance[]
  completenessScore?: number
  version?: string
  aiGenerated?: 'Y' | 'N'
  aiGeneratedAt?: string
  status?: '00' | '01' | '02' | '03'
  authorUserId: number
  reviewerUserId?: number
  remark?: string
}

export interface PrdQuery {
  pageNum?: number; pageSize?: number
  prdNo?: string; projectId?: number; title?: string; version?: string; status?: string
}

export const listPrd = (q?: PrdQuery) => request({ url: '/business/prd/list', method: 'get', params: q })
export const getPrd = (id: number) => request({ url: `/business/prd/${id}`, method: 'get' })
export const addPrd = (d: Prd) => request({ url: '/business/prd', method: 'post', data: d })
export const updatePrd = (d: Prd) => request({ url: '/business/prd', method: 'put', data: d })
export const delPrd = (ids: number | number[]) =>
  request({ url: `/business/prd/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const aiGeneratePrd = (id: number) => request({ url: `/business/prd/ai/generate/${id}`, method: 'post' })
export const exportPrd = (q?: PrdQuery) =>
  request({ url: '/business/prd/export', method: 'post', params: q, responseType: 'blob' })

export function parseUserStories(p: Prd): PrdUserStory[] {
  try { return p.aiUserStories ? JSON.parse(p.aiUserStories) : [] } catch { return [] }
}
export function parseCoreFeatures(p: Prd): PrdCoreFeature[] {
  try { return p.aiCoreFeatures ? JSON.parse(p.aiCoreFeatures) : [] } catch { return [] }
}
export function parseAcceptance(p: Prd): PrdAcceptance[] {
  try { return p.aiAcceptance ? JSON.parse(p.aiAcceptance) : [] } catch { return [] }
}
