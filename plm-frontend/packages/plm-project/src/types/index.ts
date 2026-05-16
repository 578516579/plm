import type { BaseEntity, PageQuery } from '@/types/api/common'

/** 项目表单/列表行类型 */
export interface ProjectForm extends BaseEntity {
  id?: number | string
  projectNo?: string
  projectName?: string
  projectType?: string
  status?: string
  managerUserId?: number | string
  startDate?: string
  endDate?: string
  budget?: number | string
  description?: string
}

/** 项目查询条件 */
export interface ProjectQuery extends PageQuery {
  projectNo?: string
  projectName?: string
  projectType?: string
  status?: string
  managerUserId?: number | string
  params?: {
    beginStartDate?: string
    endStartDate?: string
  }
}
