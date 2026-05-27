import type { BaseEntity, PageQuery } from '@/types/api/common'

/** tb_dashboard 表单/列表行 */
export interface DashboardForm extends BaseEntity {
  dashboardId?: number | string
  dashboardNo?: string
  title?: string
  ownerUserId?: number | string
  layoutJson?: string
  widgetTypes?: string
  refreshInterval?: number
  isDefault?: string
  status?: string
}

/** tb_dashboard 查询条件 */
export interface DashboardQuery extends PageQuery {
  dashboardNo?: string
  title?: string
  ownerUserId?: number | string
  isDefault?: string
  status?: string
}

/** /business/dashboard/aggregate 返回结构 — UI §4.2 6 类 widget */
export interface DashboardAggregate {
  stats: StatsWidget
  activeProjects: ProjectProgressItem[]
  myTodos: TodoItem[]
  qualitySnapshot: QualitySnapshotWidget
  aiMetrics: AiMetricsWidget
  lifecycle: string[]
  ownerUserId?: number | string | null
}

/** widget 1: 4 大顶部统计卡片 */
export interface StatsWidget {
  activeProjects: number
  aiDocsGenerated: number
  currentDefects: number
  autoTestCoverage: number
}

/** widget 2 行: 在办项目进度 */
export interface ProjectProgressItem {
  name: string
  progress: number
  /** 进度条配色 (primary/success/warning/danger/info) */
  color: string
}

/** widget 3 行: 我的待办 */
export interface TodoItem {
  title: string
  /** P0/P1/P2 */
  priority: string
  /** YYYY-MM-DD */
  dueDate: string
}

/** widget 4: 本迭代质量快照 */
export interface QualitySnapshotWidget {
  defectCount: number
  testPassRate: number
  codeCoverage: number
}

/** widget 5: AI 改进指标 */
export interface AiMetricsWidget {
  hoursSaved: number
  docsGenerated: number
  recommendations: string[]
}
