/**
 * UED 模块字典映射 SSoT — 对齐 plm-backend/sql/business-ued.sql
 *
 * 当前承载 biz_ued_status(4 态机,含反向边 01→00)。
 * ✓ 与 SQL 完整对齐,无已知漂移。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_ued_status UED 设计状态(4 态机) */
export const UED_STATUS: Record<string, DictItem> = {
  '00': { label: '草稿',   tag: 'info' },
  '01': { label: '评审中', tag: 'warning' },
  '02': { label: '已确认', tag: 'success' },
  '03': { label: '已废弃', tag: 'danger' }
}

const FALLBACK_TAG: TagType = 'info'

/**
 * 状态 { label, type } — 兼容 index.vue 既有 statusTagFor 调用形态。
 * 空值默认落 '00'(草稿)。
 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = UED_STATUS[s || '00']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}
