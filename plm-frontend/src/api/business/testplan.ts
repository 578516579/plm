/**
 * 测试方案 API — PRD §F4.1 + 原型 testplan.html
 * 5 种测试类型 + AI 生成测试策略 + 4 态状态机
 */
import request from '@/utils/request'

export interface TestPlan {
  testplanId?: number
  testplanNo?: string
  projectId?: number
  sprintId?: number
  title: string
  testTypes?: string  // CSV: functional/api/performance/automation/security
  testCycleDays?: number
  scope?: string
  strategy?: string
  toolsRecommended?: string
  resourcesPlan?: string
  riskAssessment?: string
  aiGenerated?: string
  status?: string
  authorUserId?: number
}

export interface TestPlanQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  title?: string
  status?: string
}

export const listTestPlan = (q: TestPlanQuery): Promise<any> =>
  request({ url: '/business/testplan/list', method: 'get', params: q })

export const getTestPlan = (id: number): Promise<any> =>
  request({ url: `/business/testplan/${id}`, method: 'get' })

export const addTestPlan = (data: TestPlan): Promise<any> =>
  request({ url: '/business/testplan', method: 'post', data })

export const updateTestPlan = (data: TestPlan): Promise<any> =>
  request({ url: '/business/testplan', method: 'put', data })

export const delTestPlan = (ids: number | number[]): Promise<any> => {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/testplan/${idStr}`, method: 'delete' })
}

// PRD §F4.1 AI 生成测试方案 — test-plan-flow
export const aiGenerateTestPlan = (id: number): Promise<any> =>
  request({ url: `/business/testplan/ai/generate/${id}`, method: 'post' })

export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
