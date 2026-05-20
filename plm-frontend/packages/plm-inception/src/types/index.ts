import type { BaseEntity, PageQuery } from '@/types/api/common'

export interface InceptionForm extends BaseEntity {
  inceptionId?: number | string
  inceptionNo?: string
  projectName?: string
  businessLine?: string
  inceptionType?: string
  background?: string
  estimatedDurationMonths?: number | string
  estimatedTeam?: string
  aiGenerated?: string
  aiProposalContent?: string
  aiRisks?: string
  aiGeneratedAt?: string
  status?: string
  rejectReason?: string
  submitterUserId?: number | string
  approverUserId?: number | string
  approvedAt?: string
  projectId?: number | string
}

export interface InceptionQuery extends PageQuery {
  inceptionNo?: string
  projectName?: string
  businessLine?: string
  inceptionType?: string
  aiGenerated?: string
  status?: string
  submitterUserId?: number | string
  approverUserId?: number | string
}
