/**
 * 测试数据工厂 API — PRD §F4.3 + 原型 testdata.html
 */
import request from '@/utils/request'

export interface TestData {
  testdataId?: number
  testdataNo?: string
  projectId: number
  title: string
  targetTable?: string  // soil_sensor / weather / crop / pest / irrigation
  outputFormat?: string  // json / sql / csv
  generateCount?: number
  fieldSemantics?: string
  ruleChinaCoord?: string  // Y / N
  ruleTimeContinuity?: string
  ruleSensorRange?: string
  ruleIncludeOutliers?: string
  generatedContent?: string
  generatedAt?: string
  aiGenerated?: string
  status?: string
  authorUserId?: number
}

export interface TestDataQuery {
  pageNum?: number; pageSize?: number; projectId?: number; title?: string; status?: string
}

export const listTestData = (q: TestDataQuery): Promise<any> =>
  request({ url: '/business/testdata/list', method: 'get', params: q })
export const getTestData = (id: number): Promise<any> =>
  request({ url: `/business/testdata/${id}`, method: 'get' })
export const addTestData = (d: TestData): Promise<any> =>
  request({ url: '/business/testdata', method: 'post', data: d })
export const updateTestData = (d: TestData): Promise<any> =>
  request({ url: '/business/testdata', method: 'put', data: d })
export const delTestData = (ids: number | number[]): Promise<any> =>
  request({ url: `/business/testdata/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const aiGenerateTestData = (id: number): Promise<any> =>
  request({ url: `/business/testdata/ai/generate/${id}`, method: 'post' })
export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
