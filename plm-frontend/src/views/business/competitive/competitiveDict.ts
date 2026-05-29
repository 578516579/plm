/**
 * 竞品模块字典映射 SSoT — 对齐 plm-backend/sql/business-competitive.sql
 *
 * 收拢价格档 / 状态两组字典的 code → label + el-tag type 映射。
 * 两组映射必须与 business-competitive.sql 中 biz_competitive_tier /
 * biz_competitive_status 字典项逐项一致;competitiveDict.spec.ts 失败 = 此处与 SQL
 * 字典漂移,必须按 SQL 重新校对(参照 requirementDict.ts / inceptionDict.ts 的 SSoT 漂移锁定模式)。
 *
 * ⚠ 已知显示层小漂移(values 完全一致,仅 UI 文案/颜色偏好不同,非数据契约层):
 *  1. pricingTier 'enterprise' 前端 label「企业」, SQL biz_competitive_tier label「企业级」
 *     —— 一字之差,value 相同 ('enterprise'),数据契约无差。
 *  2. status '02' 前端 tag「warning」(黄), SQL biz_competitive_status list_class「danger」(红)
 *     —— value+label 相同 ('02'/'已归档'),仅终态徽章颜色选择不同。
 * 两处均无数据风险,按前端现状锁定;如需对齐 SQL 文案/颜色走 UED 评审 + spawn 任务卡。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_competitive_tier 竞品价格档(3 档) */
export const COMPETITIVE_TIER: Record<string, DictItem> = {
  free: { label: '免费', tag: 'success' },
  midrange: { label: '中端', tag: 'warning' },
  enterprise: { label: '企业', tag: 'danger' }  // ⚠ SQL label「企业级」, 见文件头漂移说明
}

/** biz_competitive_status 竞品状态(3 态;02 为终态可归档) */
export const COMPETITIVE_STATUS: Record<string, DictItem> = {
  '00': { label: '草稿', tag: 'info' },
  '01': { label: '已发布', tag: 'success' },
  '02': { label: '已归档', tag: 'warning' }  // ⚠ SQL list_class='danger', 见文件头漂移说明
}

const FALLBACK_TAG: TagType = 'info'

function labelOf(map: Record<string, DictItem>, code?: string): string {
  return map[code || '']?.label || code || '-'
}

export const pricingTierLabel = (v?: string): string => labelOf(COMPETITIVE_TIER, v)
export const pricingTierTag = (v?: string): TagType => COMPETITIVE_TIER[v || '']?.tag || FALLBACK_TAG

/**
 * 状态 { label, type } —— 兼容 index.vue 既有 statusTagFor 调用形态。
 * 注意:旧实现默认空值落 '00'(草稿);此处保持一致(空 → 草稿)。
 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = COMPETITIVE_STATUS[s || '00']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}
