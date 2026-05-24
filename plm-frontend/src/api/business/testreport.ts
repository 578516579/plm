/**
 * 测试报告 API — PRD §F4.7 + 原型 testreport.html
 * 上线风险评级 (green/yellow/red) + P0/P1/P2 缺陷统计 + AI 推荐
 */
import request from '@/utils/request'

export interface TestReport {
  testreportId?: number
  testreportNo?: string
  projectId?: number
  sprintId?: number
  testplanId?: number
  title: string
  totalCases?: number
  passedCases?: number
  failedCases?: number
  coverageRate?: number
  defectSummary?: string
  p0Defects?: number
  p1Defects?: number
  p2Defects?: number
  riskLevel?: string  // green / yellow / red
  riskEvaluation?: string
  recommendations?: string
  aiGenerated?: string
  generatedAt?: string
  status?: string
  reviewerUserId?: number
}

export interface TestReportQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  title?: string
  riskLevel?: string
  status?: string
}

export const listTestReport = (q: TestReportQuery): Promise<any> =>
  request({ url: '/business/testreport/list', method: 'get', params: q })

export const getTestReport = (id: number): Promise<any> =>
  request({ url: `/business/testreport/${id}`, method: 'get' })

export const addTestReport = (data: TestReport): Promise<any> =>
  request({ url: '/business/testreport', method: 'post', data })

export const updateTestReport = (data: TestReport): Promise<any> =>
  request({ url: '/business/testreport', method: 'put', data })

export const delTestReport = (ids: number | number[]): Promise<any> => {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/testreport/${idStr}`, method: 'delete' })
}

export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
