export interface DashboardForm {
  dashboardId?: number | string
  dashboardNo?: string
  projectId?: number | string
  widgetName: string
  widgetType?: string
  dataSource?: string
  config?: string
  sortOrder?: number
  visible?: string
  userId?: number | string
  status?: string
}

export interface DashboardQuery {
  widgetName?: string
  widgetType?: string
  visible?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
