import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface TestDataForm extends BaseEntity {
  testdataId?: number | string
  testdataNo?: string
  projectId?: number | string
  title?: string
  targetTable?: string
  generateCount?: number | string
  outputFormat?: string
  fieldSemantics?: string
  ruleChinaCoord?: string
  ruleTimeContinuity?: string
  ruleSensorRange?: string
  ruleIncludeOutliers?: string
  generatedContent?: string
  generatedAt?: string
  aiGenerated?: string
  status?: string
  authorUserId?: number | string
}

export interface TestDataQuery extends PageQuery {
  testdataNo?: string
  projectId?: number | string
  title?: string
  targetTable?: string
  outputFormat?: string
  aiGenerated?: string
  status?: string
  authorUserId?: number | string
}
