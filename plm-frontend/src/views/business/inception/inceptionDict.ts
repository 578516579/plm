/**
 * 立项模块字典映射 SSoT — 对齐 plm-backend/sql/business-inception.sql
 *
 * 收拢业务线 / 项目类型 / 状态三组字典的 code → label + el-tag type 映射。
 * 三组映射必须与 business-inception.sql 中 biz_inception_biz_line /
 * biz_inception_type / biz_inception_status 字典项逐项一致;
 * inceptionDict.spec.ts 失败 = 此处与 SQL 字典漂移,必须按 SQL 重新校对
 * (参照 utils/businessRoute.ts / requirementDict.ts 的 SSoT 漂移锁定模式)。
 *
 * tag 颜色取自 SQL 各字典 list_class 列(逐项一致)。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_inception_biz_line 立项业务线(4 选项,农业垂直) */
export const INCEPTION_BIZ_LINE: Record<string, DictItem> = {
  plant_protection: { label: '植保服务', tag: 'primary' },
  precision_farming: { label: '精准农业', tag: 'success' },
  agri_supply: { label: '农资流通', tag: 'warning' },
  traceability: { label: '质量溯源', tag: 'info' }
}

/** biz_inception_type 立项项目类型(4 选项) */
export const INCEPTION_TYPE: Record<string, DictItem> = {
  new_product: { label: '新产品研发', tag: 'primary' },
  iteration: { label: '版本迭代', tag: 'success' },
  refactor: { label: '技术重构', tag: 'warning' },
  platform: { label: '平台建设', tag: 'info' }
}

/** biz_inception_status 立项状态(5 状态机,03 已批准为终态可转项目,04 已驳回可反向边 04→00) */
export const INCEPTION_STATUS: Record<string, DictItem> = {
  '00': { label: '草稿', tag: 'info' },
  '01': { label: '已提交', tag: 'warning' },
  '02': { label: '审批中', tag: 'primary' },
  '03': { label: '已批准', tag: 'success' },
  '04': { label: '已驳回', tag: 'danger' }
}

const FALLBACK_TAG: TagType = 'info'

function labelOf(map: Record<string, DictItem>, code?: string): string {
  return map[code || '']?.label || code || '-'
}

export const businessLineLabel = (v?: string): string => labelOf(INCEPTION_BIZ_LINE, v)
export const businessLineTag = (v?: string): TagType => INCEPTION_BIZ_LINE[v || '']?.tag || FALLBACK_TAG
export const inceptionTypeLabel = (v?: string): string => labelOf(INCEPTION_TYPE, v)
export const inceptionTypeTag = (v?: string): TagType => INCEPTION_TYPE[v || '']?.tag || FALLBACK_TAG

/**
 * 状态 { label, type } —— 兼容 index.vue 既有 statusTagFor 调用形态。
 * ⚠ 保持旧实现的空值默认:空 / undefined 状态落 '00'(草稿),而非 '-'。
 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = INCEPTION_STATUS[s || '00']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}
