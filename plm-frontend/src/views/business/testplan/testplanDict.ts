/**
 * 测试方案模块字典映射 SSoT — 对齐 plm-backend/sql/business-testplan.sql
 *
 * 收拢方案状态 + 测试类型两组映射。
 * biz_testplan_status (4 态) 必须与 SQL 字典逐项一致;
 * test_types(5 种) ⚠ 前端独有,SQL DDL 列为 CSV TEXT 无字典定义,见 spec drift 段。
 *
 * testplanDict.spec.ts 失败 = 此处与 SQL/前端约定漂移,按 SQL 重新校对
 * (参照 requirementDict.ts / testreportDict.ts 模式)。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_testplan_status 测试方案状态(4 态机) */
export const TESTPLAN_STATUS: Record<string, DictItem> = {
  '00': { label: '草稿',   tag: 'info' },
  '01': { label: '已确认', tag: 'warning' },
  '02': { label: '执行中', tag: 'primary' },
  '03': { label: '已完成', tag: 'success' }
}

/**
 * 测试类型(⚠ 前端独有约定;SQL DDL 列 test_types 为 CSV TEXT,无 biz_testplan_type 字典)
 * 5 种:functional/api/performance/automation/security
 */
export const TEST_TYPE: Record<string, DictItem> = {
  functional:  { label: '功能',   tag: 'primary' },
  api:         { label: '接口',   tag: 'success' },
  performance: { label: '性能',   tag: 'warning' },
  automation:  { label: '自动化', tag: 'info' },
  security:    { label: '安全',   tag: 'danger' }
}

const FALLBACK_TAG: TagType = 'info'

/**
 * 状态 { label, type } — 兼容 index.vue 既有 statusTagFor 调用形态。
 * 空值默认落 '00'(草稿),保持旧行为。
 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = TESTPLAN_STATUS[s || '00']
  return item ? { label: item.label, type: item.tag } : { label: s ?? '-', type: FALLBACK_TAG }
}

/** 测试类型 label(未命中返回裸值) */
export const testTypeLabel = (t?: string): string => TEST_TYPE[t || '']?.label || t || '-'

/** 测试类型 tag(未命中返回 info) */
export const testTypeTag = (t?: string): TagType => TEST_TYPE[t || '']?.tag || FALLBACK_TAG

/**
 * 测试类型 { label, type } — 供 testTypesDisplay computed 使用(value 字段由调用方拼)。
 * 这里 type 字段(非 tag)是为兼容 index.vue 既有 testTypesDisplay 的 type 命名。
 */
export function testTypeInfo(t?: string): { label: string; type: TagType } {
  const item = TEST_TYPE[t || '']
  return item ? { label: item.label, type: item.tag } : { label: t || '-', type: FALLBACK_TAG }
}
