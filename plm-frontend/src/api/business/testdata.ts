/** 测试数据工厂 API — PRD §F4.3 */
import request from '@/utils/request'

export interface Testdata {
  testdataId?: number
  testdataNo?: string
  projectId: number
  title: string
  targetTable?: string                // t_soil_sensor_data 等 5 类
  generateCount?: number
  outputFormat?: string               // json/sql_insert/csv
  ruleCoordinate?: 'Y' | 'N'
  ruleTimeSeries?: 'Y' | 'N'
  ruleSensorRange?: 'Y' | 'N'
  ruleIncludeAbnormal?: 'Y' | 'N'
  generatedData?: string
  writeTarget?: string                // test/dev
  writeMode?: string                  // append/truncate/upsert
  aiGenerated?: 'Y' | 'N'
  aiGeneratedAt?: string
  status?: '00' | '01' | '02' | '03'
  authorUserId: number
  remark?: string
}

export interface TestdataQuery {
  pageNum?: number; pageSize?: number
  testdataNo?: string; projectId?: number; title?: string
  targetTable?: string; outputFormat?: string; status?: string
}

export const listTestdata  = (q?: TestdataQuery) => request({ url: '/business/testdata/list', method: 'get', params: q })
export const getTestdata   = (id: number) => request({ url: `/business/testdata/${id}`, method: 'get' })
export const addTestdata   = (d: Testdata) => request({ url: '/business/testdata', method: 'post', data: d })
export const updateTestdata= (d: Testdata) => request({ url: '/business/testdata', method: 'put', data: d })
export const delTestdata   = (ids: number|number[]) => request({ url: `/business/testdata/${Array.isArray(ids)?ids.join(','):ids}`, method: 'delete' })
export const aiGenerateTestdata = (id: number) => request({ url: `/business/testdata/ai/generate/${id}`, method: 'post' })
export const exportTestdata     = (q?: TestdataQuery) => request({ url: '/business/testdata/export', method: 'post', params: q, responseType: 'blob' })
