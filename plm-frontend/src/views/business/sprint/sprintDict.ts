/**
 * 迭代模块字典映射 SSoT — 对齐 plm-backend/sql/business-sprint.sql
 *
 * 收拢迭代状态字典(biz_sprint_status, 4 态机)。
 * sprintDict.spec.ts 失败 = 此处与 SQL 字典漂移,必须按 SQL 重新校对。
 *
 * ✓ 本模块字典与 SQL 完整对齐,无已知漂移。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_sprint_status 迭代状态(4 态机,02/03 终态) */
export const SPRINT_STATUS: Record<string, DictItem> = {
  '00': { label: '计划中', tag: 'info' },
  '01': { label: '进行中', tag: 'primary' },
  '02': { label: '已完成', tag: 'success' },
  '03': { label: '已取消', tag: 'danger' }
}

const FALLBACK_TAG: TagType = 'info'

/**
 * 状态 { label, type } — 兼容 index.vue 既有 statusTagFor 调用形态。
 * 旧实现用 `s || ''` 首选(空走 fallback);用 `|| '-'` 避免显示空串。
 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = SPRINT_STATUS[s || '']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}
