/**
 * 缺陷模块字典映射 SSoT — 对齐 plm-backend/sql/business-defect.sql
 *
 * 3 组字典:
 *  1. DEFECT_STATUS   — 状态 5 态(label 对齐 SQL;⚠ 04 已关闭 tag SQL list_class 为空,前端补 info)
 *  2. DEFECT_SEVERITY — 严重级别 ⚠ 前端 P0-P3 vs SQL 00-03(同 task 范式)+ 颜色策略不同
 *  3. DEFECT_CATEGORY — 分类 ⚠ 完全异构(前端 4 字符串码 vs SQL 6 数值码,无完整对应)
 *
 * ⚠ 已知跨层契约漂移(defectDict.spec.ts ⚠ drift 段详尽锁定):
 *
 *  §1 SEVERITY value 表示错位(同 task 范式):
 *     前端 'P0' / 'P1' / 'P2' / 'P3'(直接当 value 存)
 *     SQL  '00' / '01' / '02' / '03'(label='P0 阻塞' 等)
 *
 *  §2 SEVERITY tag 颜色策略差异(前端将 P0+P1 都标红):
 *     SQL  00=danger / 01=warning / 02=info    / 03=success
 *     前端 P0=danger / P1=danger  / P2=warning / P3=info
 *     —— 前端 P1 升级为 danger(治理立场更严),P2-P3 colorshift。
 *
 *  §3 CATEGORY 完全异构(values + count):
 *     前端 4 项: functional / performance / ui / security
 *     SQL  6 项: '01'-'05' + '99' → 功能/性能/兼容性/安全/易用性/其他
 *     —— 前端无 SQL 的「兼容性/易用性/其他」3 项,前端 'ui' (UI/UX) 在 SQL 也无对应。
 *
 *  §4 STATUS '04' 已关闭 tag:前端 'info', SQL list_class 为空(未定义)
 *     —— 同 release 04 已废弃 范式,前端补默认 info。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_defect_status 缺陷状态(5 态机) */
export const DEFECT_STATUS: Record<string, DictItem> = {
  '00': { label: '新建',   tag: 'info' },
  '01': { label: '已确认', tag: 'warning' },
  '02': { label: '处理中', tag: 'primary' },
  '03': { label: '已解决', tag: 'success' },
  '04': { label: '已关闭', tag: 'info' }   // ⚠ SQL list_class 空,前端补 info
}

/**
 * biz_defect_severity 严重级别(⚠ 见文件头 drift §1 + §2)
 * — 前端 P0-P3 作为 value 存,SQL 用 00-03
 * — 颜色策略前端将 P1 升级为 danger
 */
export const DEFECT_SEVERITY: Record<string, DictItem> = {
  P0: { label: 'P0', tag: 'danger' },   // SQL 00 → danger ✓
  P1: { label: 'P1', tag: 'danger' },   // ⚠ SQL 01 → warning;前端升级为 danger
  P2: { label: 'P2', tag: 'warning' },  // ⚠ SQL 02 → info;前端用 warning
  P3: { label: 'P3', tag: 'info' }      // ⚠ SQL 03 → success;前端用 info
}

/**
 * biz_defect_category 分类(⚠ 见文件头 drift §3)
 * — 前端 4 字符串码,SQL 6 数值码,values 集合无交集
 */
export const DEFECT_CATEGORY: Record<string, DictItem> = {
  functional:  { label: '功能',  tag: 'primary' },
  performance: { label: '性能',  tag: 'warning' },
  ui:          { label: 'UI/UX', tag: 'info' },     // ⚠ SQL 无对应(SQL 有 03 兼容性/05 易用性)
  security:    { label: '安全',  tag: 'danger' }
}

const FALLBACK_TAG: TagType = 'info'

/**
 * 状态 { label, type } — 兼容 index.vue 既有 statusTagFor 调用形态。
 * 旧实现用 `s || ''` 首选(空走 fallback);用 `|| '-'` 避免显示空串。
 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = DEFECT_STATUS[s || '']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}

/** 严重级别 tag(未命中返回 info;label 与 value 一致) */
export const severityTag = (s?: string): TagType => DEFECT_SEVERITY[s || '']?.tag || FALLBACK_TAG

/** 分类 label(未命中返回裸值) */
export const categoryLabel = (v?: string): string => DEFECT_CATEGORY[v || '']?.label || v || '-'

/** 分类 tag(未命中返回 info) */
export const categoryTag = (v?: string): TagType => DEFECT_CATEGORY[v || '']?.tag || FALLBACK_TAG
