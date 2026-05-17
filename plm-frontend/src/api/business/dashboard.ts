/** 工作台 API — UI §4.2 */
import request from '@/utils/request'

export interface Dashboard {
  dashboardId?: number
  dashboardNo?: string
  title: string
  ownerUserId: number
  layoutJson?: string
  widgetTypes?: string                // CSV: stats,active_projects,my_todos,quality_snapshot,lifecycle,ai_metrics
  refreshInterval?: number
  isDefault?: 'Y' | 'N'
  status?: '00' | '01'
  remark?: string
}

export interface DashboardQuery {
  pageNum?: number; pageSize?: number
  dashboardNo?: string; title?: string; ownerUserId?: number
  isDefault?: string; status?: string
}

export interface DashboardAggregate {
  stats: { activeProjects: number; aiDocsGenerated: number; currentDefects: number; autoTestCoverage: number }
  activeProjects: Array<{ name: string; progress: number; color: string }>
  myTodos: Array<{ title: string; priority: string; dueDate: string }>
  qualitySnapshot: { defectCount: number; testPassRate: number; codeCoverage: number }
  aiMetrics: { hoursSaved: number; docsGenerated: number; recommendations: string[] }
  lifecycle: string[]
  ownerUserId?: number
}

export const listDashboard  = (q?: DashboardQuery) => request({ url: '/business/dashboard/list', method: 'get', params: q })
export const getDashboard   = (id: number) => request({ url: `/business/dashboard/${id}`, method: 'get' })
export const addDashboard   = (d: Dashboard) => request({ url: '/business/dashboard', method: 'post', data: d })
export const updateDashboard= (d: Dashboard) => request({ url: '/business/dashboard', method: 'put', data: d })
export const delDashboard   = (ids: number|number[]) => request({ url: `/business/dashboard/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const aggregateDashboard = (ownerUserId?: number) => request({ url: '/business/dashboard/aggregate', method: 'get', params: { ownerUserId } })
export const exportDashboard    = (q?: DashboardQuery) => request({ url: '/business/dashboard/export', method: 'post', params: q, responseType: 'blob' })
