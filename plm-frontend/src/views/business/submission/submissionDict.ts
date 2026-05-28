/**
 * 提测管理模块字典映射 SSoT — 对齐 plm-backend/sql/business-submission.sql
 *
 * 收拢提测状态字典(5 态机,含 AI 质量门禁 + 反向边 04→00 重写)。
 * 映射必须与 business-submission.sql 中 biz_submission_status 字典项逐项一致;
 * submissionDict.spec.ts 失败 = 此处与 SQL 字典漂移,必须按 SQL 重新校对
 * (参照 requirementDict.ts / releaseDict.ts 的 SSoT 漂移锁定模式)。
 *
 * ⚠ 已知显示层小漂移(values+labels 完全一致,仅 tag 颜色 2 处「互换」,非数据契约):
 *  1. status '01' 已提交:    前端 tag「warning」(黄), SQL list_class「primary」(蓝)
 *  2. status '02' 质量门禁中: 前端 tag「primary」(蓝), SQL list_class「warning」(黄)
 * 两处颜色互换,疑似前端早期约定未与 SQL 对齐;按前端现状锁定,UED 评审走 spawn 任务卡。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_submission_status 提测状态(5 态机,含反向边 04→00) */
export const SUBMISSION_STATUS: Record<string, DictItem> = {
  '00': { label: '草稿',       tag: 'info' },
  '01': { label: '已提交',     tag: 'warning' },   // ⚠ SQL list_class='primary'
  '02': { label: '质量门禁中', tag: 'primary' },   // ⚠ SQL list_class='warning'
  '03': { label: '已通过',     tag: 'success' },
  '04': { label: '已退回',     tag: 'danger' }
}

const FALLBACK_TAG: TagType = 'info'

/**
 * 状态 { label, type } —— 兼容 index.vue 既有 statusTagFor 调用形态。
 * 空值默认落 '00'(草稿),保持旧 statusTagFor(s||"00") 行为。
 * 注:旧 fallback `{label: s}` 可能传 undefined,此处用 `s ?? '-'` 兜底。
 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = SUBMISSION_STATUS[s || '00']
  return item ? { label: item.label, type: item.tag } : { label: s ?? '-', type: FALLBACK_TAG }
}
