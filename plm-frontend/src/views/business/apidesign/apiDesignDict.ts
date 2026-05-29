/**
 * 接口详细设计模块字典映射 SSoT — 对齐 plm-backend/sql/business-apidesign.sql
 *
 * 2 组字典:status(4 态机) + HTTP method(5 选项)。
 * ✓ status 与 SQL biz_apidesign_status 完整对齐;methodTag 非 SQL 字典,前端 UI 标色约定。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_apidesign_status 接口设计状态(4 态机) */
export const APIDESIGN_STATUS: Record<string, DictItem> = {
  '00': { label: '草稿',   tag: 'info' },
  '01': { label: '评审中', tag: 'warning' },
  '02': { label: '已确认', tag: 'success' },
  '03': { label: '已废弃', tag: 'danger' }
}

/** HTTP method 标色(前端约定,非 SQL 字典) */
export const HTTP_METHOD_TAG: Record<string, TagType> = {
  GET: 'success',
  POST: 'primary',
  PUT: 'warning',
  DELETE: 'danger',
  PATCH: 'info'
}

const FALLBACK_TAG: TagType = 'info'

export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = APIDESIGN_STATUS[s || '00']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}

export const methodTag = (m?: string): TagType => HTTP_METHOD_TAG[m || ''] || FALLBACK_TAG
