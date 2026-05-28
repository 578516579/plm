/**
 * DORA 效能指标模块字典映射 SSoT — 对齐 plm-backend/sql/business-dora.sql
 *
 * 收拢指标类型 / 等级两组字典。
 *
 * ⚠ 本模块发现**多处跨层契约漂移**,本文件按 frontend 现状以「前端码」为键锁定,
 * 契约对齐属跨层决策(前端码 → 后端 → 存量数据 → E2E 断言),须经 api-contract
 * 评审 + 走 spawn 任务卡。doraDict.spec.ts ⚠ drift 段显式记录:
 *
 *  1. metric_type 码错位(2/4):
 *     前端 'deploy_frequency'  / 'change_failure_rate'
 *     SQL  'deploy_freq'       / 'change_fail_rate'
 *     (lead_time、mttr 一致)
 *
 *  2. metric_type label 装饰差异:
 *     前端含 emoji 前缀「📈 部署频率」、「⏱️ 前置时间」等
 *     SQL  纯文本「部署频率」、「前置时间」(+ remark='次/天' 单位提示)
 *
 *  3. level 等级字典属**前端独有**:
 *     elite/high/medium/low 仅前端定义,SQL 无 biz_dora_level,tb_dora_metric 也无 level 列
 *     (可能存于其他持久层或属前端动态计算)
 *
 *  4. biz_dora_status (00草稿/01已发布/02已归档):SQL 定义但 index.vue 未渲染
 *
 *  5. biz_dora_period (month/quarter):SQL 定义但 index.vue 用裸 month/quarter 字面量
 *     (periodFilter ref 直接用值,无 label 显示函数)
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface MetricItem {
  label: string  // 含 emoji 前缀 (frontend 习惯,与 SQL 纯文本不同)
}

interface LevelItem {
  label: string  // 含奖牌 emoji
  tag: TagType
}

/**
 * DORA 4 指标(前端键名,⚠ 见文件头 drift §1 + §2)
 * — value 与 SQL biz_dora_type 部分错位
 * — label 含 emoji 前缀(SQL dict_label 为纯文本)
 */
export const METRIC_TYPE: Record<string, MetricItem> = {
  deploy_frequency:     { label: '📈 部署频率' },   // ⚠ SQL value='deploy_freq', label='部署频率'
  lead_time:            { label: '⏱️ 前置时间' },   // ✓ value 一致;⚠ SQL label='前置时间' (无 emoji)
  mttr:                 { label: '🚨 MTTR' },        // ✓ value 一致;⚠ SQL label='平均恢复时间' (前端用英文缩写)
  change_failure_rate:  { label: '❌ 变更失败率' }   // ⚠ SQL value='change_fail_rate', label='变更失败率'
}

/**
 * DORA 等级(⚠ 前端独有,SQL 无 biz_dora_level 字典,见文件头 drift §3)
 * Tag 颜色:elite=success(金) / high=primary(银) / medium=warning(铜) / low=danger.
 */
export const LEVEL: Record<string, LevelItem> = {
  elite:  { label: '🥇 Elite',  tag: 'success' },
  high:   { label: '🥈 High',   tag: 'primary' },
  medium: { label: '🥉 Medium', tag: 'warning' },
  low:    { label: '⚠️ Low',    tag: 'danger' }
}

const FALLBACK_TAG: TagType = 'info'

/** 指标类型 label(含 emoji 前缀,与旧 metricLabel 一致) */
export const metricLabel = (v?: string): string => METRIC_TYPE[v || '']?.label || v || '-'

/** 等级 label(行级,含 emoji;旧 rowLevelLabel 形态,fallback '-') */
export const rowLevelLabel = (v?: string): string => LEVEL[v || '']?.label || '-'

/** 等级 tag(旧 rowLevelTag 形态,fallback 'info') */
export const rowLevelTag = (v?: string): TagType => LEVEL[v || '']?.tag || FALLBACK_TAG
