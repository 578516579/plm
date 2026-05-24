/**
 * UED 设计协同 API — PRD §F2.3 + 原型 ued.html
 * 后端: /business/ued/*
 */
import request from '@/utils/request'

export interface Ued {
  uedId?: number
  uedNo?: string
  projectId: number
  requirementId?: number
  title: string
  figmaUrl?: string
  figmaFileKey?: string
  versionLabel?: string
  previewUrl?: string
  annotationContent?: string
  aiReviewReport?: string
  aiReviewScore?: number
  complianceCheck?: string  // JSON
  usabilityIssues?: string
  agriComponentTags?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  designerUserId?: number
  reviewerUserId?: number
}

export interface UedQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  requirementId?: number
  title?: string
  status?: string
}

export function listUed(query: UedQuery): Promise<any> {
  return request({ url: '/business/ued/list', method: 'get', params: query })
}

export function getUed(id: number): Promise<any> {
  return request({ url: `/business/ued/${id}`, method: 'get' })
}

export function addUed(data: Ued): Promise<any> {
  return request({ url: '/business/ued', method: 'post', data })
}

export function updateUed(data: Ued): Promise<any> {
  return request({ url: '/business/ued', method: 'put', data })
}

export function delUed(ids: number | number[]): Promise<any> {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/ued/${idStr}`, method: 'delete' })
}

// AI 设计规范评审 — PRD §F2.3 ued-review-flow
export function aiReviewUed(id: number): Promise<any> {
  return request({ url: `/business/ued/ai/review/${id}`, method: 'post' })
}

export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
