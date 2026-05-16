import type { BaseEntity, PageQuery } from '@/types/api/common'

/** 任务表单/列表行类型 */
export interface TaskForm extends BaseEntity {
  taskId?: number | string
  taskNo?: string
  projectId?: number | string
  requirementId?: number | string
  sprintId?: number | string
  title?: string
  description?: string
  status?: string
  priority?: string
  assigneeUserId?: number | string
  estimatedHours?: number | string
  actualHours?: number | string
  mrUrl?: string
  mrBranch?: string
}

/** 任务查询条件 */
export interface TaskQuery extends PageQuery {
  taskNo?: string
  projectId?: number | string
  requirementId?: number | string
  sprintId?: number | string
  title?: string
  status?: string
  priority?: string
  assigneeUserId?: number | string
}

/** 看板列 */
export interface KanbanColumn {
  status: string
  label: string
  tasks: TaskForm[]
  count: number
}

/** 看板视图 */
export interface KanbanView {
  columns: KanbanColumn[]
}
