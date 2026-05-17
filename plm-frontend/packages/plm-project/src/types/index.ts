import type { BaseEntity, PageQuery } from '@/types/api/common'

/**
 * 项目表单/列表行类型
 * 字段对照表:PRD-MAPPING.md §2 "Project (F1.2)" (commit 20b5bb6)
 */
export interface ProjectForm extends BaseEntity {
  id?: number | string
  projectNo?: string
  projectName?: string
  /** 业务线(必填):biz_project_business_line — plant_protection / precision_agri / agri_supply / quality_trace */
  businessLine?: string
  /** 项目类型:biz_project_type — rnd / upgrade / ops */
  projectType?: string
  /** 优先级:biz_project_priority — P0 / P1 / P2 / P3 */
  priority?: string
  /** 交付阶段:biz_project_phase — 00 规划 / 01 研发 / 02 测试 / 03 验收 */
  lifecyclePhase?: string
  /** 总状态:biz_project_status — 00 进行中 / 01 暂停 / 02 已完成 / 03 已取消 */
  status?: string
  /** 进度 0-100 */
  progress?: number | string
  /** 健康度:biz_project_health — green / amber / red */
  health?: string
  managerUserId?: number | string
  startDate?: string
  endDate?: string
  description?: string
}

/** 项目查询条件 */
export interface ProjectQuery extends PageQuery {
  projectNo?: string
  projectName?: string
  businessLine?: string
  projectType?: string
  priority?: string
  lifecyclePhase?: string
  status?: string
  health?: string
  managerUserId?: number | string
  params?: {
    beginStartDate?: string
    endStartDate?: string
  }
}
