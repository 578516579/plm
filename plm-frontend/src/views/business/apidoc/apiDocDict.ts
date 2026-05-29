/**
 * API 文档模块字典映射 SSoT — 对齐 plm-backend/sql/business-apidoc.sql
 *
 * 当前承载 HTTP method 标色(与 apidesign 同款,前端 UI 约定,非 SQL 字典)。
 * 注:apidoc/index.vue 当前不渲染 status tag,如未来需要补 APIDOC_STATUS 与 SQL 对齐。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

/** HTTP method 标色(前端约定,非 SQL 字典) */
export const HTTP_METHOD_TAG: Record<string, TagType> = {
  GET: 'success',
  POST: 'primary',
  PUT: 'warning',
  DELETE: 'danger',
  PATCH: 'info'
}

const FALLBACK_TAG: TagType = 'info'

export const methodTag = (m?: string): TagType => HTTP_METHOD_TAG[m || ''] || FALLBACK_TAG
