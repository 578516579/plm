import type { BaseEntity, PageQuery } from '@/types/api/common'

/** 测试方案表单/列表行（PRD F4.1 + 4 状态机） */
export interface TestPlanForm extends BaseEntity {
  testplanId?: number | string
  testplanNo?: string
  projectId?: number | string
  sprintId?: number | string
  title?: string
  testTypes?: string
  testCycleDays?: number | string
  scope?: string
  strategy?: string
  toolsRecommended?: string
  resourcesPlan?: string
  riskAssessment?: string
  aiGenerated?: string
  status?: string
  authorUserId?: number | string
}

/** 测试方案查询条件 */
export interface TestPlanQuery extends PageQuery {
  testplanNo?: string
  projectId?: number | string
  sprintId?: number | string
  title?: string
  aiGenerated?: string
  status?: string
  authorUserId?: number | string
}
