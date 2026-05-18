import type { BaseEntity, PageQuery } from '@/types/api/common'

/** API 文档表单/列表行（PRD F5.4 + 3 状态机） */
export interface ApiDocForm extends BaseEntity {
  apidocId?: number | string
  apidocNo?: string
  projectId?: number | string
  title?: string
  httpMethod?: string
  path?: string
  description?: string
  requestSchema?: string
  responseSchema?: string
  openapiSpec?: string
  sourceClass?: string
  sourceMethod?: string
  version?: string
  status?: string
  lastSyncedAt?: string
  autoExtracted?: string
}

/** API 文档查询条件 */
export interface ApiDocQuery extends PageQuery {
  apidocNo?: string
  projectId?: number | string
  title?: string
  httpMethod?: string
  path?: string
  version?: string
  status?: string
  autoExtracted?: string
}
