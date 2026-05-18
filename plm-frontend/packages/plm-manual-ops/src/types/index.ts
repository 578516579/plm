export interface ManualOpsForm {
  manualOpsId?: number | string
  manualOpsNo?: string
  projectId?: number | string
  title: string
  monitoringPlan?: string
  alertChannels?: string
  iotDeviceTypes?: string
  content?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number | string
  reviewerUserId?: number | string
}

export interface ManualOpsQuery {
  title?: string
  monitoringPlan?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
