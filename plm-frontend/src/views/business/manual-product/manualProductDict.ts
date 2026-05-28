/**
 * 产品手册模块字典映射 SSoT — 对齐 plm-backend/sql/business-manual-product.sql
 *
 * 单 status 字典(4 态机,与 manual-impl/manual-ops 同款生成流程)。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_manualproduct_status 产品手册状态(4 态机) */
export const MANUAL_PRODUCT_STATUS: Record<string, DictItem> = {
  '00': { label: '草稿',   tag: 'info' },
  '01': { label: '生成中', tag: 'warning' },
  '02': { label: '已生成', tag: 'success' },
  '03': { label: '已发布', tag: 'primary' }
}

const FALLBACK_TAG: TagType = 'info'

export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = MANUAL_PRODUCT_STATUS[s || '00']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}
