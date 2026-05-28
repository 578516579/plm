/**
 * 架构模块字典映射 SSoT — 对齐 plm-backend/sql/business-arch.sql
 *
 * 当前承载 3 组前端用到的字典:status / archMode / primaryStack。
 * SQL 还定义有 biz_arch_database / biz_arch_ai_engine / biz_arch_deployment /
 * biz_arch_iot_protocol,这几个在 index.vue 仅作 form 选项(未抽出);如需 label
 * 渲染再补到本文件。
 *
 * ⚠ 已知显示层小漂移(values 一致,仅 UI label 简化):
 *  §1 archMode label:前端「微服务/单体/Serverless/分层」,SQL「微服务架构/单体架构/分层架构」(简化)
 *  §2 primaryStack label:前端「Java SB3/Go Gin/Python」,SQL「Java (SpringBoot3)/Go (Gin)/Python (FastAPI)」(简化)
 *
 * ✓ status 4 态与 SQL biz_arch_status 完整对齐。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_arch_status 架构状态(4 态机) ✓ SQL 完美对齐 */
export const ARCH_STATUS: Record<string, DictItem> = {
  '00': { label: '草稿',   tag: 'info' },
  '01': { label: '评审中', tag: 'warning' },
  '02': { label: '已确认', tag: 'success' },
  '03': { label: '已废弃', tag: 'danger' }
}

/**
 * biz_arch_mode 架构模式(4 选项,⚠ label 简化,见文件头 §1)
 */
export const ARCH_MODE: Record<string, DictItem> = {
  microservice: { label: '微服务',   tag: 'primary' },  // ⚠ SQL '微服务架构'
  monolith:     { label: '单体',     tag: 'success' },  // ⚠ SQL '单体架构'
  serverless:   { label: 'Serverless', tag: 'warning' },
  layered:      { label: '分层',     tag: 'info' }      // ⚠ SQL '分层架构'
}

/**
 * biz_arch_stack 技术栈(4 选项,⚠ label 简化,见文件头 §2)
 */
export const ARCH_STACK: Record<string, DictItem> = {
  java_sb3:       { label: 'Java SB3', tag: 'primary' },  // ⚠ SQL 'Java (SpringBoot3)'
  go_gin:         { label: 'Go Gin',   tag: 'success' },  // ⚠ SQL 'Go (Gin)'
  python_fastapi: { label: 'Python',   tag: 'warning' },  // ⚠ SQL 'Python (FastAPI)'
  nodejs:         { label: 'Node.js',  tag: 'info' }
}

const FALLBACK_TAG: TagType = 'info'

/**
 * 状态 { label, type } — 兼容 index.vue 既有 statusTagFor 调用形态。
 * 空值默认落 '00'(草稿)。
 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = ARCH_STATUS[s || '00']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}

/**
 * 枚举 label —— 兼容旧 enumLabel('arch'|'stack', v) 双参形态。
 * kind='arch' → ARCH_MODE, kind='stack' → ARCH_STACK。
 */
export function enumLabel(kind: 'arch' | 'stack', v?: string): string {
  const map = kind === 'arch' ? ARCH_MODE : ARCH_STACK
  return map[v || '']?.label || v || '-'
}
