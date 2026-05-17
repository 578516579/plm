/** UED 设计 API — PRD §F2.3 + 原型 ued.html (含 JS runUEDCheck timeline 输出) */
import request from '@/utils/request'

export interface UedReviewItem {
  status: 'pass' | 'warning' | 'fail'
  category: string
  message: string
  suggestion?: string
}

export interface Ued {
  uedId?: number
  uedNo?: string
  projectId: number
  title: string
  designType?: string                // ue/ui/motion/icon
  platform?: string                  // web/mobile/iot/miniapp
  figmaFileKey?: string
  figmaUrl?: string
  version?: string
  description?: string
  reviewReport?: string
  reviewItemsJson?: string           // JSON 数组 → UedReviewItem[]
  complianceScore?: number
  aiGenerated?: 'Y' | 'N'
  aiGeneratedAt?: string
  requirementId?: number
  status?: '00' | '01' | '02' | '03'
  designerUserId: number
  reviewerUserId?: number
  remark?: string
}

export interface UedQuery {
  pageNum?: number; pageSize?: number
  uedNo?: string; projectId?: number; title?: string
  designType?: string; platform?: string; status?: string
}

export const listUed = (q?: UedQuery) => request({ url: '/business/ued/list', method: 'get', params: q })
export const getUed = (id: number) => request({ url: `/business/ued/${id}`, method: 'get' })
export const addUed = (d: Ued) => request({ url: '/business/ued', method: 'post', data: d })
export const updateUed = (d: Ued) => request({ url: '/business/ued', method: 'put', data: d })
export const delUed = (ids: number | number[]) =>
  request({ url: `/business/ued/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const aiReviewUed = (id: number) => request({ url: `/business/ued/ai/review/${id}`, method: 'post' })

export function parseReviewItems(u: Ued): UedReviewItem[] {
  try { return u.reviewItemsJson ? JSON.parse(u.reviewItemsJson) : [] } catch { return [] }
}
