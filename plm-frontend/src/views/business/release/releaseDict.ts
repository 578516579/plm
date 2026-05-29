/**
 * 发布管理模块字典映射 SSoT — 对齐 plm-backend/sql/business-release.sql
 *
 * 收拢发布状态 / 发布策略两组字典 + 策略提示文本 + 策略 emoji icon。
 * 两组映射必须与 business-release.sql 中 biz_release_status / biz_release_strategy
 * 字典项逐项一致;releaseDict.spec.ts 失败 = 此处与 SQL 字典漂移,
 * 必须按 SQL 重新校对(参照 requirementDict.ts / aiAgentDict.ts 的 SSoT 漂移锁定模式)。
 *
 * ⚠ 已知显示层小漂移(values 一致,仅 UI 偏好,非数据契约):
 *  1. status '04' 已废弃 前端 tag「info」(灰), SQL biz_release_status list_class 为空(未定义)
 *     —— SQL 未给 04 设 list_class,前端补默认 info 灰色徽章;非冲突,UED 决策走任务卡。
 *
 * 设计扩展:STRATEGY 含 hint(form 帮助文本)与 icon(emoji,index.vue radio 模板内联用);
 * 当前 strategyLabel/strategyHint 函数化,icon 暂留模板内联(radio 按钮 slot 形式)。
 * tag 字段对齐 SQL list_class(primary/warning/success),当前 UI 未渲染 strategy tag,Dict 备用。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface StatusDictItem {
  label: string
  tag: TagType
}

interface StrategyDictItem extends StatusDictItem {
  hint: string  // 表单帮助文本
  icon: string  // emoji,frontend-only(SQL 无对应字段)
}

/** biz_release_status 发布状态(5 态机;含反向边 → 03 已回滚 / 04 终态已废弃) */
export const RELEASE_STATUS: Record<string, StatusDictItem> = {
  '00': { label: '计划中', tag: 'info' },
  '01': { label: '发布中', tag: 'warning' },
  '02': { label: '已发布', tag: 'success' },
  '03': { label: '已回滚', tag: 'danger' },
  '04': { label: '已废弃', tag: 'info' }   // ⚠ SQL list_class 为空,前端补默认 info,见文件头
}

/** biz_release_strategy 发布策略(3 种;tag = SQL list_class,hint/icon 为前端扩展) */
export const RELEASE_STRATEGY: Record<string, StrategyDictItem> = {
  blue_green: {
    label: '蓝绿',
    tag: 'primary',
    icon: '🟦',
    hint: '🟦 蓝绿:同时存在新旧两套环境,流量切换瞬时完成;回滚最快但资源 2x'
  },
  canary: {
    label: '金丝雀',
    tag: 'warning',
    icon: '🐤',
    hint: '🐤 金丝雀:先放 5%~10% 流量到新版本,观察 metrics 再渐进扩量'
  },
  rolling: {
    label: '滚动',
    tag: 'success',
    icon: '🔄',
    hint: '🔄 滚动:批次替换实例(如每次 25%);资源占用少但回滚慢'
  }
}

const FALLBACK_TAG: TagType = 'info'

/**
 * 状态 { label, type } —— 兼容 index.vue 既有 statusTagFor 调用形态。
 * 空值默认落 '00'(计划中),保持旧 statusTagFor(s||"00") 行为。
 * 注:相对旧版 `{ label: s, ... }`(可能为 undefined),此处用 `s ?? '-'` 兜底,避免显示 "undefined"。
 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = RELEASE_STATUS[s || '00']
  return item ? { label: item.label, type: item.tag } : { label: s ?? '-', type: FALLBACK_TAG }
}

export const strategyLabel = (s?: string): string => RELEASE_STRATEGY[s || '']?.label || s || '-'
export const strategyTag = (s?: string): TagType => RELEASE_STRATEGY[s || '']?.tag || FALLBACK_TAG
export const strategyHint = (s?: string): string => RELEASE_STRATEGY[s || '']?.hint || ''
export const strategyIcon = (s?: string): string => RELEASE_STRATEGY[s || '']?.icon || ''
