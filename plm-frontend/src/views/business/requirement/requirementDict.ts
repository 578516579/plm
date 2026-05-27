/**
 * 需求模块字典映射 SSoT — 对齐 plm-backend/sql/business-requirement.sql
 *
 * 收拢需求来源 / 优先级 / 状态三组字典的 code → label + el-tag type 映射。
 * 这里的映射表必须与 business-requirement.sql 中 biz_req_source / biz_req_priority /
 * biz_req_status 字典项逐项一致;requirementDict.spec.ts 失败 = 此处与 SQL 字典漂移,
 * 必须按 SQL 重新校对(参照 utils/businessRoute.ts 的 SSoT 漂移锁定模式)。
 *
 * AI 评估(aiEvaluation: high/medium/low)非字典字段,为前端约定值,一并收拢于此。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_req_source 需求来源(business-requirement.sql) */
export const REQ_SOURCE: Record<string, DictItem> = {
  '01': { label: '客户反馈', tag: 'primary' },
  '02': { label: '内部提案', tag: 'info' },
  '03': { label: '运营数据', tag: 'warning' },
  '04': { label: '竞品分析', tag: 'success' }
}

/** biz_req_priority 优先级(label 取短码 P0/P1/P2,字典全称为「P0 紧急」等) */
export const REQ_PRIORITY: Record<string, DictItem> = {
  '00': { label: 'P0', tag: 'danger' },
  '01': { label: 'P1', tag: 'warning' },
  '02': { label: 'P2', tag: 'info' }
}

/** biz_req_status 需求生命周期状态(02/03 为终态) */
export const REQ_STATUS: Record<string, DictItem> = {
  '00': { label: '待评审', tag: 'warning' },
  '01': { label: '开发中', tag: 'primary' },
  '02': { label: '已完成', tag: 'success' },
  '03': { label: '已取消', tag: 'danger' }
}

/** AI 价值评估(非字典,前端约定 high/medium/low) */
export const REQ_AI_EVAL: Record<string, DictItem> = {
  high: { label: '高价值', tag: 'success' },
  medium: { label: '中价值', tag: 'warning' },
  low: { label: '低价值', tag: 'info' }
}

const FALLBACK_TAG: TagType = 'info'

function labelOf(map: Record<string, DictItem>, code?: string): string {
  return map[code || '']?.label || code || '-'
}
function tagOf(map: Record<string, DictItem>, code?: string): TagType {
  return map[code || '']?.tag || FALLBACK_TAG
}

export const sourceLabel = (v?: string): string => labelOf(REQ_SOURCE, v)
export const sourceTag = (v?: string): TagType => tagOf(REQ_SOURCE, v)
export const priorityLabel = (v?: string): string => labelOf(REQ_PRIORITY, v)
export const priorityTag = (v?: string): TagType => tagOf(REQ_PRIORITY, v)
export const aiEvalLabel = (v?: string): string => labelOf(REQ_AI_EVAL, v)
export const aiEvalTag = (v?: string): TagType => tagOf(REQ_AI_EVAL, v)

/** 状态返回 { label, type } —— 兼容 index.vue 既有 statusTagFor 调用形态 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = REQ_STATUS[s || '']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}
