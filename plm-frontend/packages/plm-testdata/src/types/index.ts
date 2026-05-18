export interface TestdataForm {
  testdataId?: number | string
  testdataNo?: string
  projectId?: number | string
  title: string
  targetTable?: string
  generateCount?: number
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

export interface TestdataQuery {
  title?: string
  targetTable?: string
  outputFormat?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
