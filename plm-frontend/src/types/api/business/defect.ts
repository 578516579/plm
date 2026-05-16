import type { BaseEntity, PageQuery } from '@/types/api/common'

/** 缺陷表单/行类型 */
export interface DefectForm extends BaseEntity {
  defectId?: number | string
  defectNo?: string
  projectId?: number | string
  sprintId?: number | string
  taskId?: number | string
  title?: string
  description?: string
  severity?: string
  category?: string
  status?: string
  assigneeUserId?: number | string
  reporterUserId?: number | string
  reproduceSteps?: string
  expectedResult?: string
  actualResult?: string
  resolution?: string
  tags?: string
}

/** 缺陷查询条件 */
export interface DefectQuery extends PageQuery {
  defectNo?: string
  projectId?: number | string
  sprintId?: number | string
  taskId?: number | string
  title?: string
  severity?: string
  category?: string
  status?: string
  assigneeUserId?: number | string
  reporterUserId?: number | string
  tags?: string
}
