/**
 * 竞品情报 API — PRD §F1.3 + 原型 competitive.html
 * 后端: /business/competitive/*
 */
import request from '@/utils/request'

export interface Competitive {
  competitiveId?: number
  competitiveNo?: string
  projectId: number
  competitorName: string
  vendor?: string
  website?: string
  pricingModel?: string
  pricingTier?: string  // free / midrange / enterprise
  featureMatrix?: string  // JSON 12 维
  strengths?: string
  weaknesses?: string
  opportunities?: string
  threats?: string
  aiAnalysisReport?: string  // Markdown
  aiGenerated?: string
  aiGeneratedAt?: string
  monitorEnabled?: string
  monitorKeywords?: string
  lastMonitoredAt?: string
  status?: string
  authorUserId?: number
}

export interface CompetitiveQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  competitorName?: string
  pricingTier?: string
  status?: string
}

export function listCompetitive(query: CompetitiveQuery): Promise<any> {
  return request({ url: '/business/competitive/list', method: 'get', params: query })
}

export function getCompetitive(id: number): Promise<any> {
  return request({ url: `/business/competitive/${id}`, method: 'get' })
}

export function addCompetitive(data: Competitive): Promise<any> {
  return request({ url: '/business/competitive', method: 'post', data })
}

export function updateCompetitive(data: Competitive): Promise<any> {
  return request({ url: '/business/competitive', method: 'put', data })
}

export function delCompetitive(ids: number | number[]): Promise<any> {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/competitive/${idStr}`, method: 'delete' })
}

// AI 竞品综合分析 — PRD §F1.3 competitive-analysis-flow
export function aiAnalyzeCompetitive(id: number): Promise<any> {
  return request({ url: `/business/competitive/ai/analyze/${id}`, method: 'post' })
}

// 项目下拉 (复用)
export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
