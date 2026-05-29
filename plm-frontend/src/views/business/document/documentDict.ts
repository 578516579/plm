/**
 * 文档模块字典映射 SSoT — 对齐 plm-backend/sql/business-document.sql
 *
 * 2 组字典:文档类型 + 文档状态。
 *
 * ⚠ 已知契约漂移(documentDict.spec.ts ⚠ drift 段详尽锁定):
 *
 *  §1 docType 完全异构(同 testcase/defect 范式,但程度更甚):
 *     前端 11 项: prd / hld / lld / db / api / req / arch / test / manual / changelog / other
 *     SQL  12 项: prd / arch / db_design / api_design / proposal / ued / test_plan /
 *                test_report / api_doc / manual_product / manual_impl / manual_ops
 *     —— 仅 'prd' + 'arch' 2 项 value 一致;前端用短缩写(hld/lld/db/api/req/test/manual...),
 *        SQL 用完整名(db_design/api_design/test_plan...)
 *
 *  §2 status '03' 已归档 tag:前端 'danger', SQL list_class 为空(未定义)
 *     —— 同 release/defect 04 范式,前端补默认 danger(更醒目)
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

interface DocTypeItem {
  label: string
  icon: string
  tag: TagType
}

/**
 * biz_doc_status 文档状态(4 态机)
 * ⚠ 03 已归档 tag 前端 danger / SQL list_class 空
 */
export const DOCUMENT_STATUS: Record<string, DictItem> = {
  '00': { label: '草稿',   tag: 'info' },
  '01': { label: '待评审', tag: 'warning' },
  '02': { label: '已发布', tag: 'success' },
  '03': { label: '已归档', tag: 'danger' }   // ⚠ SQL list_class 空,前端补默认
}

/**
 * 文档类型(⚠ 前端 11 项缩写 vs SQL 12 项完整名,无 1:1 对应,见文件头 §1)
 */
export const DOC_TYPE: Record<string, DocTypeItem> = {
  prd:       { label: 'PRD',       icon: '📄', tag: 'primary' },   // ✓ SQL value 同
  hld:       { label: 'HLD',       icon: '🏗️', tag: 'success' },  // ⚠ SQL 无 (有 arch)
  lld:       { label: 'LLD',       icon: '🔌', tag: 'warning' },   // ⚠ SQL 无 (有 api_design)
  db:        { label: 'DB',        icon: '🗄️', tag: 'info' },     // ⚠ SQL 'db_design'
  api:       { label: 'API',       icon: '📗', tag: 'primary' },   // ⚠ SQL 无 (有 api_doc)
  req:       { label: '需求',      icon: '📋', tag: 'warning' },   // ⚠ SQL 无
  arch:      { label: '架构',      icon: '🏛️', tag: 'success' },  // ✓ SQL value 同
  test:      { label: '测试',      icon: '🧪', tag: 'primary' },   // ⚠ SQL 无 (有 test_plan/test_report)
  manual:    { label: '手册',      icon: '📖', tag: 'info' },      // ⚠ SQL 无 (有 manual_product 等 3 个)
  changelog: { label: 'Changelog', icon: '📝', tag: 'info' },      // ⚠ SQL 无
  other:     { label: '其他',      icon: '📁', tag: 'info' }       // ⚠ SQL 无
}

const FALLBACK_TAG: TagType = 'info'

/**
 * 状态 { label, type } — 兼容 index.vue 既有 statusTagFor 调用形态。
 * 旧实现用 `s || ''`(空走 fallback)。
 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = DOCUMENT_STATUS[s || '']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}

/** 文档类型 label(含 emoji,与旧 docTypeLabel 行为一致:命中返回 "icon label",未命中返回裸值) */
export const docTypeLabel = (v?: string): string => {
  const item = DOC_TYPE[v || '']
  return item ? `${item.icon} ${item.label}` : (v || '-')
}

/** 文档类型 tag */
export const docTypeTag = (v?: string): TagType => DOC_TYPE[v || '']?.tag || FALLBACK_TAG
