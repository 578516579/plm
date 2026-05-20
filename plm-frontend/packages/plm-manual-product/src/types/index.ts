import type { BaseEntity, PageQuery } from '@/types/api/common'

/** 产品手册表单/列表行（PRD F5.1 + 4 状态机） */
export interface ManualProductForm extends BaseEntity {
  manualproductId?: number | string
  manualproductNo?: string
  projectId?: number | string
  title?: string
  productVersion?: string
  includeModules?: string
  content?: string
  screenshotsUrls?: string
  screenshotsCount?: number | string
  outputFormats?: string
  aiGenerated?: string
  generatedAt?: string
  status?: string
  authorUserId?: number | string
}

/** 产品手册查询条件 */
export interface ManualProductQuery extends PageQuery {
  manualproductNo?: string
  projectId?: number | string
  title?: string
  productVersion?: string
  aiGenerated?: string
  status?: string
  authorUserId?: number | string
}
