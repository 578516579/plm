/** 效能分析 API — PRD §F6 */
import request from '@/utils/request'

export interface AnalyticsSnapshot {
  snapshotId?: number
  snapshotNo?: string
  projectId?: number                  // NULL=全局
  title: string
  periodType: string                  // month/quarter/year
  snapshotDate: string                // YYYY-MM-DD
  requirementThroughput?: number
  sprintOnTimeRate?: number
  defectDensity?: number
  autoTestCoverage?: number
  deploymentFrequency?: number
  leadTimeHours?: number
  mttrHours?: number
  changeFailureRate?: number
  aiHoursSaved?: number
  activeProjects?: number
  projectsAtRisk?: number
  aiRecommendations?: string
  aiGenerated?: 'Y' | 'N'
  aiGeneratedAt?: string
  status?: '00' | '01' | '02'
  authorUserId: number
  remark?: string
}

export interface AnalyticsQuery {
  pageNum?: number; pageSize?: number
  snapshotNo?: string; projectId?: number; title?: string
  periodType?: string; status?: string
}

export const listAnalytics  = (q?: AnalyticsQuery) => request({ url: '/business/analytics/list', method: 'get', params: q })
export const getAnalytics   = (id: number) => request({ url: `/business/analytics/${id}`, method: 'get' })
export const addAnalytics   = (d: AnalyticsSnapshot) => request({ url: '/business/analytics', method: 'post', data: d })
export const updateAnalytics= (d: AnalyticsSnapshot) => request({ url: '/business/analytics', method: 'put', data: d })
export const delAnalytics   = (ids: number|number[]) => request({ url: `/business/analytics/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const aiRecommendAnalytics = (id: number) => request({ url: `/business/analytics/ai/recommend/${id}`, method: 'post' })
export const exportAnalytics      = (q?: AnalyticsQuery) => request({ url: '/business/analytics/export', method: 'post', params: q, responseType: 'blob' })
