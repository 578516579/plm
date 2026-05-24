/**
 * DORA 指标 API — 原型 devops.html
 */
import request from '@/utils/request'

export interface DoraMetric {
  metricId?: number
  projectId?: number
  metricType?: string  // deploy_frequency / lead_time / mttr / change_failure_rate
  metricValue?: number
  metricUnit?: string
  level?: string  // elite / high / medium / low
  periodLabel?: string  // 2026-05
  measuredAt?: string
  aiSuggestion?: string
}

export interface DoraQuery { pageNum?: number; pageSize?: number; projectId?: number; metricType?: string; periodLabel?: string }

export const listDora = (q: DoraQuery): Promise<any> => request({ url: '/business/dora/list', method: 'get', params: q })
export const getDora = (id: number): Promise<any> => request({ url: `/business/dora/${id}`, method: 'get' })
export const addDora = (d: DoraMetric): Promise<any> => request({ url: '/business/dora', method: 'post', data: d })
export const delDora = (ids: number | number[]): Promise<any> => request({ url: `/business/dora/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const aiAnalyzeDora = (id: number): Promise<any> => request({ url: `/business/dora/ai/suggest/${id}`, method: 'post' })
export const listProjectsForSelect = (): Promise<any> => request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
