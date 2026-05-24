/**
 * 效能分析 API — PRD §F6 + 原型 analytics.html
 */
import request from '@/utils/request'

export interface AnalyticsSnapshot {
  snapshotId?: number
  snapshotNo?: string
  projectId?: number
  period?: string  // month / quarter / year
  periodLabel?: string  // 2026-05
  reqThroughput?: number
  sprintOnTimeRate?: number
  defectDensity?: number
  aiSavedHours?: number
  stageEfficiency?: string  // JSON
  projectHealthScores?: string  // JSON
  aiSuggestions?: string  // Markdown
  aiGenerated?: string
  status?: string
}

export interface AnalyticsQuery { pageNum?: number; pageSize?: number; projectId?: number; period?: string }

export const listAnalytics = (q: AnalyticsQuery): Promise<any> => request({ url: '/business/analytics/list', method: 'get', params: q })
export const getAnalytics = (id: number): Promise<any> => request({ url: `/business/analytics/${id}`, method: 'get' })
export const addAnalytics = (d: AnalyticsSnapshot): Promise<any> => request({ url: '/business/analytics', method: 'post', data: d })
export const updateAnalytics = (d: AnalyticsSnapshot): Promise<any> => request({ url: '/business/analytics', method: 'put', data: d })
export const delAnalytics = (ids: number | number[]): Promise<any> => request({ url: `/business/analytics/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const aiGenerateAnalytics = (id: number): Promise<any> => request({ url: `/business/analytics/ai/generate/${id}`, method: 'post' })
