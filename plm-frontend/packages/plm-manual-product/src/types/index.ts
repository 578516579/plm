import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface ManualProductForm extends BaseEntity {
  manualproductId?: number | string
  manualproductNo?: string
  projectId?: number | string
  title?: string
  status?: string
}

export interface ManualProductQuery extends PageQuery {
  manualproductNo?: string
  projectId?: number | string
  title?: string
  status?: string
}
