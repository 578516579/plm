/**
 * Feature Flag API — 原型 featureflag.html
 */
import request from '@/utils/request'

export interface FeatureFlag {
  flagId?: number
  flagKey: string  // snake_case
  flagName: string
  description?: string
  projectId?: number
  environment?: string  // dev / staging / prod
  rolloutMode?: string  // all_on / all_off / canary
  canaryPercentage?: number  // 1-99
  status?: string  // 00 草稿 / 01 启用 / 02 已废弃
  authorUserId?: number
}

export interface FlagQuery { pageNum?: number; pageSize?: number; projectId?: number; flagKey?: string; environment?: string; status?: string }

export const listFeatureFlag = (q: FlagQuery): Promise<any> => request({ url: '/business/feature-flag/list', method: 'get', params: q })
export const getFeatureFlag = (id: number): Promise<any> => request({ url: `/business/feature-flag/${id}`, method: 'get' })
export const addFeatureFlag = (d: FeatureFlag): Promise<any> => request({ url: '/business/feature-flag', method: 'post', data: d })
export const updateFeatureFlag = (d: FeatureFlag): Promise<any> => request({ url: '/business/feature-flag', method: 'put', data: d })
export const delFeatureFlag = (ids: number | number[]): Promise<any> => request({ url: `/business/feature-flag/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const checkFeatureFlag = (key: string, userId: number): Promise<any> => request({ url: `/business/feature-flag/check`, method: 'get', params: { flagKey: key, userId } })
export const listProjectsForSelect = (): Promise<any> => request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
