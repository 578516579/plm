/**
 * PRD 模块字典映射 SSoT — 对齐 plm-backend/sql/business-prd.sql
 *
 * 当前承载 biz_prd_status(4 态机)。
 * SQL 还定义有 biz_prd_scene(4 选项)/ biz_prd_target_user(3 选项),
 * 这两个 dict 在 index.vue 仅用作 form el-option 静态选项,无 label 转换函数需求;
 * 如未来需在列表/详情中以「中文 label + tag」渲染场景或目标用户,补 PRD_SCENE /
 * PRD_TARGET_USER map 到本文件并加 helper。
 *
 * ✓ 本模块字典与 SQL biz_prd_status 完整对齐,无已知漂移。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_prd_status PRD 状态(4 态机,02/03 终态) */
export const PRD_STATUS: Record<string, DictItem> = {
  '00': { label: '草稿',   tag: 'info' },
  '01': { label: '评审中', tag: 'warning' },
  '02': { label: '已确认', tag: 'success' },
  '03': { label: '已废弃', tag: 'danger' }
}

const FALLBACK_TAG: TagType = 'info'

/**
 * 状态 { label, type } — 兼容 index.vue 既有 statusTagFor 调用形态。
 * 空值默认落 '00'(草稿),保持旧 statusTagFor(s||"00") 行为。
 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = PRD_STATUS[s || '00']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}
