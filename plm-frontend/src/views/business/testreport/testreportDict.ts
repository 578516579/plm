/**
 * 测试报告模块字典映射 SSoT — 对齐 plm-backend/sql/business-testreport.sql
 *
 * 收拢报告状态 / 风险评级两组字典 + 风险评级元数据(icon/长描述/操作建议/CSS 类)。
 * 两组映射必须与 business-testreport.sql 中 biz_testreport_status /
 * biz_testreport_risk 字典项逐项一致;testreportDict.spec.ts 失败 = 此处与 SQL
 * 字典漂移,必须按 SQL 重新校对(参照 requirementDict.ts / submissionDict.ts)。
 *
 * ⚠ 已知显示层小漂移(values+tags 完全一致,仅 label 文案风格不同,非数据契约):
 *  1. risk label 风格:前端「🟢 绿灯」/「🟡 黄灯」/「🔴 红灯」(emoji + 短语),
 *     SQL biz_testreport_risk「绿 (低风险)」/「黄 (中风险)」/「红 (高风险)」(纯文本 + 描述子)
 *     —— values 一致,仅 UI 偏好,锁当前;UED 走任务卡。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface StatusItem {
  label: string
  tag: TagType
}

interface RiskItem extends StatusItem {
  icon: string       // 单 emoji
  longLabel: string  // 含动作描述的长 label,如「绿灯 - 可以发布」
  hint: string       // 操作建议
}

/** biz_testreport_status 测试报告状态(3 态) */
export const TESTREPORT_STATUS: Record<string, StatusItem> = {
  '00': { label: '草稿',   tag: 'info' },
  '01': { label: '审核中', tag: 'warning' },
  '02': { label: '已发布', tag: 'success' }
}

/**
 * biz_testreport_risk 风险评级(3 级:绿/黄/红)
 * label 含 emoji 前缀(前端约定;⚠ SQL label 为「绿 (低风险)」纯文本,见文件头)。
 */
export const TESTREPORT_RISK: Record<string, RiskItem> = {
  green: {
    label: '🟢 绿灯', tag: 'success', icon: '🟢',
    longLabel: '绿灯 - 可以发布',
    hint: '所有指标达标,可走标准发布流程'
  },
  yellow: {
    label: '🟡 黄灯', tag: 'warning', icon: '🟡',
    longLabel: '黄灯 - 需谨慎,建议二次评审',
    hint: '存在风险,建议增加灰度观察期或补充测试'
  },
  red: {
    label: '🔴 红灯', tag: 'danger', icon: '🔴',
    longLabel: '红灯 - 禁止上线',
    hint: '必须先修复 P0 / 提升覆盖率才能上线'
  }
}

const FALLBACK_TAG: TagType = 'info'

/**
 * 状态 { label, type } — 兼容 index.vue 既有 statusTagFor 调用形态。
 * 空值默认落 '00'(草稿),保持旧行为。
 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = TESTREPORT_STATUS[s || '00']
  return item ? { label: item.label, type: item.tag } : { label: s ?? '-', type: FALLBACK_TAG }
}

/**
 * 风险 { label, type } — 兼容 index.vue 既有 riskTagFor 调用形态。
 * 空值默认落 'green',保持旧行为。
 */
export function riskTagFor(s?: string): { label: string; type: TagType } {
  const item = TESTREPORT_RISK[s || 'green']
  return item ? { label: item.label, type: item.tag } : { label: s ?? '-', type: FALLBACK_TAG }
}

/** 风险评级 icon(单 emoji,fallback '⚪') */
export const riskIcon = (s?: string): string => TESTREPORT_RISK[s || 'green']?.icon || '⚪'

/** 风险评级长描述(含动作,fallback '未评级') */
export const riskLongLabel = (s?: string): string => TESTREPORT_RISK[s || 'green']?.longLabel || '未评级'

/** 风险评级操作建议(fallback '') */
export const riskHint = (s?: string): string => TESTREPORT_RISK[s || 'green']?.hint || ''

/** 风险评级 CSS 类(保持旧实现 `risk-${level}` 模板形式,允许未知值落 'risk-unknown') */
export const riskCls = (s?: string): string => `risk-${s || 'green'}`
