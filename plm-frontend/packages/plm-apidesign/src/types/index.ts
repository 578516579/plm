export interface ApidesignForm {
  apidesignId?: number | string
  apidesignNo?: string
  projectId?: number | string
  archId?: number | string
  title: string
  httpMethod?: string
  path?: string
  description?: string
  requestSchema?: string
  responseSchema?: string
  openapiSpec?: string
  mockEnabled?: string
  mockResponse?: string
  aiGenerated?: string
  aiGeneratedAt?: string
  status?: string
  authorUserId?: number | string
  reviewerUserId?: number | string
}

export interface ApidesignQuery {
  title?: string
  httpMethod?: string
  path?: string
  status?: string
  pageNum?: number
  pageSize?: number
}
