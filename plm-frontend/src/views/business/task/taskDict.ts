/**
 * 任务模块字典映射 SSoT — 对齐 plm-backend/sql/business-task.sql
 *
 * 收拢任务状态 / 优先级两组字典。
 * TASK_STATUS 与 biz_task_status 6 码完整对齐 ✓(并行 session 已补 05 已取消)。
 *
 * ⚠ 已知契约漂移(同 task 早期 yield 经验,见 99-跨阶段/在途任务.md):
 *  1. PRIORITY value 表示错位(同 testcase/defect 范式):
 *     前端 'P0' / 'P1' / 'P2'(直接当 value 存,Task.priority?: string 注释「P0/P1/P2」)
 *     SQL  '00' / '01' / '02'(label='P0 紧急' / 'P1 重要' / 'P2 一般')
 *     —— 颜色 tag 一致 (P0→danger 等);仅 value 表示不同。
 *
 * STATUS 历史:本模块最初 yield 给并行 session(它需补 '05' 已取消 inline),完成后
 * (commit 在 fbcca9e 主体),前端 status 已与 SQL biz_task_status 6 码对齐。本次抽取
 * 在 yield 完成后回收,做完整 SSoT 抽取。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_task_status 任务状态(6 态机,05 终态;✓ SQL 完整对齐) */
export const TASK_STATUS: Record<string, DictItem> = {
  '00': { label: '待开发',   tag: 'info' },
  '01': { label: '开发中',   tag: 'primary' },
  '02': { label: '代码评审', tag: 'warning' },
  '03': { label: '测试中',   tag: 'warning' },
  '04': { label: '已完成',   tag: 'success' },
  '05': { label: '已取消',   tag: 'danger' }
}

/**
 * biz_task_priority 优先级(⚠ 前端 P0/P1/P2 vs SQL 00/01/02,见文件头 §1)
 * 颜色对齐 SQL list_class:P0→danger / P1→warning / P2→info。
 */
export const TASK_PRIORITY: Record<string, DictItem> = {
  P0: { label: 'P0', tag: 'danger' },
  P1: { label: 'P1', tag: 'warning' },
  P2: { label: 'P2', tag: 'info' }
}

const FALLBACK_TAG: TagType = 'info'

/** 看板列(6 列,从 TASK_STATUS 派生;若 task.spec.ts 期望 5 列需 .filter(s=>s.status!=='05')) */
export const kanbanColumns: { status: string; label: string }[] =
  ['00', '01', '02', '03', '04', '05'].map(status => ({ status, label: TASK_STATUS[status].label }))

/** 优先级 el-tag type */
export const priorityTag = (p?: string): TagType => TASK_PRIORITY[p || '']?.tag || FALLBACK_TAG

/** 状态 { label, type } — 兼容 index.vue 既有 taskStatusTag 调用形态 */
export function taskStatusTag(s?: string): { label: string; type: TagType } {
  const item = TASK_STATUS[s || '']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}
