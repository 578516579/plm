/**
 * 自动化测试 API — PRD §F4.5 + 原型 autotest.html
 */
import request from '@/utils/request'

export interface AutoTest {
  autotestId?: number
  autotestNo?: string
  projectId: number
  title: string
  testSuiteType?: string  // ui / api / perf / regression
  framework?: string  // playwright / selenium / jmeter / cypress
  scriptContent?: string
  scheduleCron?: string  // 0 2 * * *
  scheduleEnabled?: string  // Y / N
  targetUrl?: string
  lastExecutedAt?: string
  lastRootCauseAnalysis?: string
  executionDurationSec?: number
  passRate?: number  // 0-100
  totalCases?: number
  passedCases?: number
  failedCases?: number
  aiGenerated?: string
  status?: string
  authorUserId?: number
}

export interface AutoTestQuery {
  pageNum?: number; pageSize?: number; projectId?: number; title?: string; testSuiteType?: string; status?: string
}

export const listAutoTest = (q: AutoTestQuery): Promise<any> =>
  request({ url: '/business/autotest/list', method: 'get', params: q })
export const getAutoTest = (id: number): Promise<any> =>
  request({ url: `/business/autotest/${id}`, method: 'get' })
export const addAutoTest = (d: AutoTest): Promise<any> =>
  request({ url: '/business/autotest', method: 'post', data: d })
export const updateAutoTest = (d: AutoTest): Promise<any> =>
  request({ url: '/business/autotest', method: 'put', data: d })
export const delAutoTest = (ids: number | number[]): Promise<any> =>
  request({ url: `/business/autotest/${Array.isArray(ids) ? ids.join(',') : ids}`, method: 'delete' })
export const aiGenerateAutoTest = (id: number): Promise<any> =>
  request({ url: `/business/autotest/ai/generate/${id}`, method: 'post' })
export const runAutoTest = (id: number): Promise<any> =>
  request({ url: `/business/autotest/run/${id}`, method: 'post' })
export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
