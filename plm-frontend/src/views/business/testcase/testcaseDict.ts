/**
 * 测试用例模块字典映射 SSoT — 对齐 plm-backend/sql/business-testcase.sql
 *
 * 3 组字典:
 *  1. TESTCASE_STATUS  — 用例状态 5 态(✓ 与 SQL biz_testcase_status 完美对齐)
 *  2. TESTCASE_CATEGORY — 用例分类 ⚠ 跨层契约错位(前端字符串码 vs SQL 数值码,且选项集不同)
 *  3. TESTCASE_PRIORITY — 用例优先级 ⚠ 前端 P0/P1/P2 vs SQL 00/01/02(同 task 模块范式)
 *
 * ⚠ 已知跨层契约漂移(testcaseDict.spec.ts ⚠ drift 段详尽锁定;
 * 修复需 api-contract 评审 + 跨层联动):
 *
 *  §1 CATEGORY 完全异构(values + count):
 *     前端 5 项: functional / boundary / exception / agri / performance
 *     SQL  7 项: '01'-'07' → 功能/接口/性能/安全/兼容性/E2E/烟雾
 *     —— values 集合无交集,数据契约根本性不匹配
 *
 *  §2 PRIORITY 存储表示错位(同 task 范式):
 *     前端 'P0' / 'P1' / 'P2'(直接当 value 存)
 *     SQL  '00' / '01' / '02'(label 是 'P0 关键' 等)
 *     —— 颜色语义一致(P0→danger 等),仅 value 表示不同
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_testcase_status 用例状态(5 态机,03/04 终态) ✓ SQL 完美对齐 */
export const TESTCASE_STATUS: Record<string, DictItem> = {
  '00': { label: '草稿',   tag: 'info' },
  '01': { label: '待执行', tag: 'warning' },
  '02': { label: '执行中', tag: 'primary' },
  '03': { label: '已通过', tag: 'success' },
  '04': { label: '已失败', tag: 'danger' }
}

/**
 * 用例分类(⚠ 前端 5 项,与 SQL biz_testcase_category 7 项「01-07」完全异构)
 * 前端 5 项:functional / boundary / exception / agri / performance
 */
export const TESTCASE_CATEGORY: Record<string, DictItem> = {
  functional:  { label: '功能',     tag: 'primary' },
  boundary:    { label: '边界',     tag: 'warning' },
  exception:   { label: '异常',     tag: 'danger' },
  agri:        { label: '农业专项', tag: 'success' },
  performance: { label: '性能',     tag: 'info' }
}

/**
 * 用例优先级(⚠ 前端 'P0/P1/P2' 直接当 value 存,SQL biz_testcase_priority 用 '00/01/02')
 * 颜色语义一致(P0→danger / 00→danger 等),仅 value 表示不同。同 task 模块 priority drift 范式。
 */
export const TESTCASE_PRIORITY: Record<string, DictItem> = {
  P0: { label: 'P0', tag: 'danger' },
  P1: { label: 'P1', tag: 'warning' },
  P2: { label: 'P2', tag: 'info' }
}

const FALLBACK_TAG: TagType = 'info'

/**
 * 状态 { label, type } — 兼容 index.vue 既有 statusTagFor 调用形态。
 * 注:旧实现用 `s || ''`(不默认 '00'),保持一致 — 空值走 fallback。
 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = TESTCASE_STATUS[s || '']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}

/** 分类 label(未命中返回裸值) */
export const categoryLabel = (v?: string): string => TESTCASE_CATEGORY[v || '']?.label || v || '-'

/** 分类 tag(未命中返回 info) */
export const categoryTag = (v?: string): TagType => TESTCASE_CATEGORY[v || '']?.tag || FALLBACK_TAG

/** 优先级 tag(未命中返回 info;label 与 value 一致,无需 priorityLabel) */
export const priorityTag = (p?: string): TagType => TESTCASE_PRIORITY[p || '']?.tag || FALLBACK_TAG
