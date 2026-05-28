/**
 * 运维手册模块字典映射 SSoT — 对齐 plm-backend/sql/business-manual-ops.sql
 *
 * 3 组字典:status(4 态机) + monitoringStack(监控选型) + alertChannel(告警通道)。
 */

export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface DictItem {
  label: string
  tag: TagType
}

/** biz_manualops_status 运维手册状态(4 态机) */
export const MANUAL_OPS_STATUS: Record<string, DictItem> = {
  '00': { label: '草稿',   tag: 'info' },
  '01': { label: '生成中', tag: 'warning' },
  '02': { label: '已生成', tag: 'success' },
  '03': { label: '已发布', tag: 'primary' }
}

/** 监控选型 label(3 项) */
export const MONITORING_LABEL: Record<string, string> = {
  prometheus_grafana: 'Prom+Grafana',
  aliyun_monitor: '阿里云',
  zabbix: 'Zabbix'
}

/** 告警通道 label(4 项) */
export const ALERT_CHANNEL_LABEL: Record<string, string> = {
  dingtalk: '钉钉',
  feishu: '飞书',
  wecom: '企微',
  email: '邮件'
}

const FALLBACK_TAG: TagType = 'info'

export function statusTagFor(s?: string): { label: string; type: TagType } {
  const item = MANUAL_OPS_STATUS[s || '00']
  return item ? { label: item.label, type: item.tag } : { label: s || '-', type: FALLBACK_TAG }
}

export const monLabel = (v?: string): string => MONITORING_LABEL[v || ''] || v || '-'
export const channelLabel = (v?: string): string => ALERT_CHANNEL_LABEL[v || ''] || v || '-'
