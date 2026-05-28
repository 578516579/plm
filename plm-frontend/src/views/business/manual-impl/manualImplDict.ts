/**
 * 实施手册模块字典映射 SSoT — 对齐 plm-backend/sql/business-manual-impl.sql
 *
 * 3 组字典:status(4 态机) + deployType(部署方式) + osType(操作系统)。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_manualimpl_status 实施手册状态(4 态机) */
export const MANUAL_IMPL_STATUS: Record<string, DictItem> = {
  '00': { label: '草稿',   tag: 'info' },
  '01': { label: '生成中', tag: 'warning' },
  '02': { label: '已生成', tag: 'success' },
  '03': { label: '已发布', tag: 'primary' }
}

/** 部署方式 label(3 项) */
export const DEPLOY_LABEL: Record<string, string> = {
  docker_compose: 'Docker',
  k8s: 'K8s',
  baremetal: '裸机'
}

/** 操作系统 label(3 项) */
export const OS_LABEL: Record<string, string> = {
  centos: 'CentOS',
  ubuntu: 'Ubuntu',
  kylin: '麒麟'
}

const FALLBACK_TAG: TagType = 'info'

export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = MANUAL_IMPL_STATUS[s || '00']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}

export const deployLabel = (v?: string): string => DEPLOY_LABEL[v || ''] || v || '-'
export const osLabel = (v?: string): string => OS_LABEL[v || ''] || v || '-'
