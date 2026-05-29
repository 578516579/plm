/**
 * OpenSpec 模块字典映射 SSoT — 对齐 plm-backend/sql/business-openspec.sql
 *
 * 承载 specType(规范类型)4 选项。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** 规范类型(4 选项) */
export const SPEC_TYPE: Record<string, DictItem> = {
  openapi_31:       { label: 'OpenAPI 3.1',  tag: 'primary' },
  asyncapi_30:      { label: 'AsyncAPI 3.0', tag: 'success' },
  ai_function_spec: { label: 'AI Function',  tag: 'warning' },
  graphql:          { label: 'GraphQL',      tag: 'info' }
}

const FALLBACK_TAG: TagType = 'info'

export const specTypeLabel = (v?: string): string => SPEC_TYPE[v || '']?.label || v || '-'
export const specTypeTag = (v?: string): TagType => SPEC_TYPE[v || '']?.tag || FALLBACK_TAG
