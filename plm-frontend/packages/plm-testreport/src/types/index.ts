import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface TestReportForm extends BaseEntity {
  testreportId?: number | string
  testreportNo?: string
  projectId?: number | string
  title?: string
  status?: string
}

export interface TestReportQuery extends PageQuery {
  testreportNo?: string
  projectId?: number | string
  title?: string
  status?: string
}
