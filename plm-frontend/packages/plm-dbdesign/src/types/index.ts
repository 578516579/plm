export interface DbdesignForm {
  dbdesignId?: number | string
  dbdesignNo?: string
  projectId?: number | string
  archId?: number | string
  title: string
  dbEngine?: string
  erDiagramContent?: string
  dataDictionary?: string
  ddlScript?: string
  normalizationCheck?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number | string
  reviewerUserId?: number | string
}

export interface DbdesignQuery {
  title?: string
  dbEngine?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
