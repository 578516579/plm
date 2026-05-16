import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface TestPlanForm extends BaseEntity {
  testplanId?: number | string
  testplanNo?: string
  projectId?: number | string
  title?: string
  status?: string
}

export interface TestPlanQuery extends PageQuery {
  testplanNo?: string
  projectId?: number | string
  title?: string
  status?: string
}
