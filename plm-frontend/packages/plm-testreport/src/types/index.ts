import type { BaseEntity, PageQuery } from '@/types/api/common'

/** 测试报告表单/列表行（PRD F4.7 + 3 状态机 + 风险评级） */
export interface TestReportForm extends BaseEntity {
  testreportId?: number | string
  testreportNo?: string
  projectId?: number | string
  sprintId?: number | string
  testplanId?: number | string
  title?: string
  totalCases?: number | string
  passedCases?: number | string
  failedCases?: number | string
  coverageRate?: number | string
  defectSummary?: string
  p0Defects?: number | string
  p1Defects?: number | string
  p2Defects?: number | string
  riskLevel?: string
  riskEvaluation?: string
  recommendations?: string
  aiGenerated?: string
  status?: string
  generatedAt?: string
  reviewerUserId?: number | string
}

/** 测试报告查询条件 */
export interface TestReportQuery extends PageQuery {
  testreportNo?: string
  projectId?: number | string
  sprintId?: number | string
  testplanId?: number | string
  title?: string
  riskLevel?: string
  aiGenerated?: string
  status?: string
}
