/** Feature Flag API — DevOps 扩展 */
import request from '@/utils/request'

export interface FeatureFlag {
  flagId?: number
  flagNo?: string
  flagKey: string                     // snake_case
  title: string
  description?: string
  environment: string                 // test/staging/prod
  rolloutPercentage: number           // 0-100
  rolloutStrategy: string             // all_on/canary/all_off
  targetUserSegment?: string
  status?: '00' | '01'                // 00开启/01关闭
  authorUserId: number
  remark?: string
}

export interface FeatureFlagQuery {
  pageNum?: number; pageSize?: number
  flagNo?: string; flagKey?: string; title?: string
  environment?: string; rolloutStrategy?: string; status?: string
}

export const listFeatureFlag  = (q?: FeatureFlagQuery) => request({ url: '/business/feature-flag/list', method: 'get', params: q })
export const getFeatureFlag   = (id: number) => request({ url: `/business/feature-flag/${id}`, method: 'get' })
export const addFeatureFlag   = (d: FeatureFlag) => request({ url: '/business/feature-flag', method: 'post', data: d })
export const updateFeatureFlag= (d: FeatureFlag) => request({ url: '/business/feature-flag', method: 'put', data: d })
export const delFeatureFlag   = (ids: number|number[]) => request({ url: `/business/feature-flag/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const checkFeatureFlag = (flagKey: string, environment: string, userId?: number) =>
  request({ url: '/business/feature-flag/check', method: 'get', params: { flagKey, environment, userId } })
export const exportFeatureFlag = (q?: FeatureFlagQuery) => request({ url: '/business/feature-flag/export', method: 'post', params: q, responseType: 'blob' })
