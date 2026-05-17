import type { BaseEntity, PageQuery } from '@/types/api/common'

/** 项目立项表单 / 列表行 */
export interface InceptionForm extends BaseEntity {
  inceptionId?: number | string
  inceptionNo?: string
  projectName?: string
  businessLine?: string
  inceptionType?: string
  background?: string
  estimatedDurationMonths?: number
  estimatedTeam?: string
  /** Y/N */
  aiGenerated?: string
  aiProposalContent?: string
  aiRisks?: string
  aiGeneratedAt?: string
  /** 00=草稿 01=已提交 02=审批中 03=已批准 04=已驳回 */
  status?: string
  rejectReason?: string
  submitterUserId?: number | string
  approverUserId?: number | string
  approvedAt?: string
  projectId?: number | string
}

/** 查询条件 */
export interface InceptionQuery extends PageQuery {
  inceptionNo?: string
  projectName?: string
  businessLine?: string
  inceptionType?: string
  status?: string
  aiGenerated?: string
}
