/**
 * 工作台模块字典映射 SSoT — 工作台聚合多个业务模块的字典展示。
 *
 * 4 组字典(workbench 上下文,部分与其他模块字典「重叠但 label 不同」):
 *  1. PROJECT_STATUS — 项目状态 ⚠ 单字符码 '0'-'4'(独特,非 '00'-'04')
 *  2. TASK_PRIORITY  — 任务优先级 '00/01/02' → P0/P1/P2(与 task 模块同)
 *  3. DASHBOARD_TASK_STATUS — 任务状态 5 态(与 task 模块同 label)
 *  4. DASHBOARD_RISK — 风险等级 green/yellow/red(value 同 testreport,⚠ label 不同)
 *
 * ⚠ 已知与其他模块字典的「同 value 异 label」(workbench 上下文偏好):
 *  1. dashboard RISK label「🟢 健康 / 🟡 一般 / 🔴 风险」(强调当下状态)
 *     testreport RISK label「🟢 绿灯 / 🟡 黄灯 / 🔴 红灯」(强调评级判断)
 *     SQL biz_testreport_risk label「绿 (低风险) ...」(纯文本+描述)
 *     —— 三层 label 不一致,但 value+tag 一致。属各上下文 UI 偏好,锁当前。
 *
 *  2. PROJECT_STATUS 用单字符码 '0'-'4'(可能源于 tb_project 历史 RuoYi 约定),
 *     与其他业务模块的 '00'-'04' 习惯不同;暂不统一(改需联动 schema/seed)。
 *
 * 修改本字典前确认与对应模块字典(taskDict 暂未抽 / testreportDict)的边界,
 * 同 value 不同 label 是有意为之时,保持现状;否则向上统一到底层模块字典。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/**
 * 项目状态(⚠ 单字符码 '0'-'4',与典型 '00'-'04' 模块不同)
 * 5 态:未启动 / 进行中 / 暂停 / 已完成 / 已取消
 */
export const PROJECT_STATUS: Record<string, DictItem> = {
  '0': { label: '未启动', tag: 'info' },
  '1': { label: '进行中', tag: 'primary' },
  '2': { label: '暂停',   tag: 'warning' },
  '3': { label: '已完成', tag: 'success' },
  '4': { label: '已取消', tag: 'danger' }
}

/** 任务优先级 (3 档,与 task 模块同) */
export const TASK_PRIORITY: Record<string, DictItem> = {
  '00': { label: 'P0', tag: 'danger' },
  '01': { label: 'P1', tag: 'warning' },
  '02': { label: 'P2', tag: 'info' }
}

/** 任务状态(5 态,workbench 视角;与 task 模块同 label) */
export const DASHBOARD_TASK_STATUS: Record<string, DictItem> = {
  '00': { label: '待开发',   tag: 'info' },
  '01': { label: '开发中',   tag: 'primary' },
  '02': { label: '代码评审', tag: 'warning' },
  '03': { label: '测试中',   tag: 'warning' },
  '04': { label: '已完成',   tag: 'success' }
}

/**
 * 风险等级(value 同 testreport,⚠ label 不同 — workbench 用「健康/一般/风险」)
 * green / yellow / red
 */
export const DASHBOARD_RISK: Record<string, DictItem> = {
  green:  { label: '🟢 健康', tag: 'success' },   // ⚠ testreport 为「🟢 绿灯」
  yellow: { label: '🟡 一般', tag: 'warning' },   // ⚠ testreport 为「🟡 黄灯」
  red:    { label: '🔴 风险', tag: 'danger' }     // ⚠ testreport 为「🔴 红灯」
}

const FALLBACK_TAG: TagType = 'info'

/** 项目状态 { label, type } — 兼容 index.vue statusTagFor 调用;空值默落 '0' 未启动 */
export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = PROJECT_STATUS[s || '0']
  return item ? { label: item.label, type: item.tag } : { label: s ?? '-', type: FALLBACK_TAG }
}

/** 任务优先级 { label, type } — 空值默落 '02' P2 */
export function priorityTagFor(p?: string): { label: string; type: TagType } {
  const item = TASK_PRIORITY[p || '02']
  return item ? { label: item.label, type: item.tag } : { label: p ?? '-', type: FALLBACK_TAG }
}

/** 任务优先级 CSS 类(p0/p1/p2),旧实现 fallback 'p2' */
export const priorityClass = (p?: string): string =>
  ({ '00': 'p0', '01': 'p1', '02': 'p2' } as Record<string, string>)[p || '02'] || 'p2'

/** 任务状态 { label, type } — 空值默落 '00' 待开发 */
export function taskStatusTagFor(s?: string): { label: string; type: TagType } {
  const item = DASHBOARD_TASK_STATUS[s || '00']
  return item ? { label: item.label, type: item.tag } : { label: s ?? '-', type: FALLBACK_TAG }
}

/** 风险等级 { label, type } — 空值默落 'green'(workbench 默认乐观状态) */
export function riskTagFor(s?: string): { label: string; type: TagType } {
  const item = DASHBOARD_RISK[s || 'green']
  return item ? { label: item.label, type: item.tag } : { label: '—', type: FALLBACK_TAG }
}
