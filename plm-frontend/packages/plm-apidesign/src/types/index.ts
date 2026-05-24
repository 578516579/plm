import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface ApiDesignForm extends BaseEntity {
  apidesignId?: number | string
  apidesignNo?: string
  projectId?: number | string
  archId?: number | string
  title?: string
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

export interface ApiDesignQuery extends PageQuery {
  apidesignNo?: string
  projectId?: number | string
  archId?: number | string
  title?: string
  httpMethod?: string
  path?: string
  mockEnabled?: string
  aiGenerated?: string
  status?: string
  authorUserId?: number | string
}
