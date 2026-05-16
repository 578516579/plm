import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface TestCaseForm extends BaseEntity {
  testcaseId?: number | string
  testcaseNo?: string
  projectId?: number | string
  requirementId?: number | string
  title?: string
  description?: string
  category?: string
  priority?: string
  status?: string
  preconditions?: string
  steps?: string
  expectedResult?: string
  actualResult?: string
  isAutomated?: string
  automationScriptPath?: string
  executionCount?: number
  lastExecutedAt?: string
  tags?: string
}

export interface TestCaseQuery extends PageQuery {
  testcaseNo?: string
  projectId?: number | string
  requirementId?: number | string
  title?: string
  category?: string
  priority?: string
  status?: string
  isAutomated?: string
  tags?: string
}

export interface TestCaseExecuteRequest {
  status: '03' | '04'   // 03 已通过 / 04 已失败
  actualResult?: string
}
