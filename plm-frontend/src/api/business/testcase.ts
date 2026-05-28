/**
 * 测试用例管理 API — PRD §F4.2 + 原型 testcase.html
 * 5 态: 00 草稿 → 01 待执行 → 02 执行中 → 03 已通过 → 04 已失败 (含反向边 03/04 → 01; 对齐 biz_testcase_status 字典)
 */
import request from '@/utils/request'

export interface TestCase {
  testcaseId?: number
  testcaseNo?: string
  projectId: number
  requirementId?: number
  title: string
  description?: string
  category?: string  // functional / boundary / exception / agri / performance
  priority?: string  // P0 / P1 / P2
  status?: string
  preconditions?: string
  steps?: string
  expectedResult?: string
  actualResult?: string
  isAutomated?: string  // Y / N
  automationScriptPath?: string
  executionCount?: number
  lastExecutedAt?: string
}

export interface TestCaseQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  requirementId?: number
  category?: string
  status?: string
  title?: string
}

export const listTestCase = (q: TestCaseQuery): Promise<any> =>
  request({ url: '/business/testcase/list', method: 'get', params: q })

export const getTestCase = (id: number): Promise<any> =>
  request({ url: `/business/testcase/${id}`, method: 'get' })

export const addTestCase = (data: TestCase): Promise<any> =>
  request({ url: '/business/testcase', method: 'post', data })

export const updateTestCase = (data: TestCase): Promise<any> =>
  request({ url: '/business/testcase', method: 'put', data })

export const delTestCase = (ids: number | number[]): Promise<any> => {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/testcase/${idStr}`, method: 'delete' })
}

export const executeTestCase = (id: number, result: { status: string; actualResult?: string }): Promise<any> =>
  request({ url: `/business/testcase/${id}/execute`, method: 'post', data: result })

// AI 补全单条用例要素(前置条件/步骤/预期结果)— PRD §F3.5,后端 /ai/generate/{id}(全模块约定)
export const aiGenerateTestCaseElements = (id: number): Promise<any> =>
  request({ url: `/business/testcase/ai/generate/${id}`, method: 'post' })

// AI 批量生成测试用例 — PRD §F4.2 testcase-gen-flow(⚠ 后端批量端点待实现,见在途任务)
export const aiGenerateTestCases = (params: {
  projectId: number; requirementId?: number; categories: string[]
}): Promise<any> =>
  request({ url: `/business/testcase/ai/generate`, method: 'post', data: params })

export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })

export const listRequirementsForSelect = (): Promise<any> =>
  request({ url: '/business/requirement/list', method: 'get', params: { pageSize: 200 } })
