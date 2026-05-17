/**
 * 提测管理 API — PRD §F4.4 + 原型 submit.html
 * AI 质量门禁: 单测覆盖率 ≥60% + 代码扫描 + PRD 完整 + API 文档更新
 */
import request from '@/utils/request'

export interface Submission {
  submissionId?: number
  submissionNo?: string
  projectId?: number
  sprintId?: number
  title: string
  scope?: string
  environment?: string
  expectedTestDays?: number
  riskNotes?: string
  unitTestCoverage?: number
  codeScanPassed?: string
  prdCompleted?: string
  apiDocUpdated?: string
  qualityGatePassed?: string
  status?: string
  rejectReason?: string
  submitterUserId?: number
  reviewerUserId?: number
  submittedAt?: string
  approvedAt?: string
}

export interface SubmissionQuery {
  pageNum?: number
  pageSize?: number
  projectId?: number
  title?: string
  status?: string
}

export const listSubmission = (q: SubmissionQuery): Promise<any> =>
  request({ url: '/business/submission/list', method: 'get', params: q })

export const getSubmission = (id: number): Promise<any> =>
  request({ url: `/business/submission/${id}`, method: 'get' })

export const addSubmission = (data: Submission): Promise<any> =>
  request({ url: '/business/submission', method: 'post', data })

export const updateSubmission = (data: Submission): Promise<any> =>
  request({ url: '/business/submission', method: 'put', data })

export const delSubmission = (ids: number | number[]): Promise<any> => {
  const idStr = Array.isArray(ids) ? ids.join(',') : ids
  return request({ url: `/business/submission/${idStr}`, method: 'delete' })
}

export const listProjectsForSelect = (): Promise<any> =>
  request({ url: '/business/project/list', method: 'get', params: { pageSize: 200 } })
