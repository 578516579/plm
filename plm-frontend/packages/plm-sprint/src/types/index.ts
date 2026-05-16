import type { BaseEntity, PageQuery } from '@/types/api/common'

/** 迭代表单/列表行类型 */
export interface SprintForm extends BaseEntity {
  sprintId?: number | string
  sprintNo?: string
  projectId?: number | string
  name?: string
  goal?: string
  status?: string
  plannedStartDate?: string
  plannedEndDate?: string
  actualStartDate?: string
  actualEndDate?: string
  durationDays?: number
}

/** 迭代查询条件 */
export interface SprintQuery extends PageQuery {
  sprintNo?: string
  projectId?: number | string
  name?: string
  status?: string
  params?: {
    beginPlannedStartDate?: string
    endPlannedStartDate?: string
  }
}

/** 迭代健康度统计 */
export interface SprintStats {
  sprintId: number | string
  plannedTaskCount: number
  completedTaskCount: number
  inProgressTaskCount: number
  remainingTaskCount: number
  completeRate: number
  onTime: boolean
  daysOverPlan: number
}
