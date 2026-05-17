/** DORA 效能指标 API — DevOps 扩展 */
import request from '@/utils/request'

export interface DoraMetric {
  doraId?: number
  doraNo?: string
  projectId?: number
  metricName: string
  metricType: string                  // deploy_freq/lead_time/mttr/change_fail_rate
  metricValue: number
  metricUnit?: string
  periodType: string                  // month/quarter
  snapshotDate: string                // YYYY-MM-DD
  trendChartJson?: string
  heatmapJson?: string
  leadtimeBreakdown?: string
  aiSuggestions?: string
  aiGenerated?: 'Y' | 'N'
  aiGeneratedAt?: string
  status?: '00' | '01' | '02'
  authorUserId: number
  remark?: string
}

export interface DoraQuery {
  pageNum?: number; pageSize?: number
  doraNo?: string; projectId?: number; metricName?: string
  metricType?: string; periodType?: string; status?: string
}

export const listDora  = (q?: DoraQuery) => request({ url: '/business/dora/list', method: 'get', params: q })
export const getDora   = (id: number) => request({ url: `/business/dora/${id}`, method: 'get' })
export const addDora   = (d: DoraMetric) => request({ url: '/business/dora', method: 'post', data: d })
export const updateDora= (d: DoraMetric) => request({ url: '/business/dora', method: 'put', data: d })
export const delDora   = (ids: number|number[]) => request({ url: `/business/dora/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const aiSuggestDora = (id: number) => request({ url: `/business/dora/ai/suggest/${id}`, method: 'post' })
export const exportDora    = (q?: DoraQuery) => request({ url: '/business/dora/export', method: 'post', params: q, responseType: 'blob' })
