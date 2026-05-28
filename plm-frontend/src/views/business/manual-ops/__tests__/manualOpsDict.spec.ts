import { describe, it, expect } from 'vitest'
import {
  MANUAL_OPS_STATUS, MONITORING_LABEL, ALERT_CHANNEL_LABEL,
  statusTagFor, monLabel, channelLabel,
  type TagType
} from '../manualOpsDict'

describe('manualOpsDict — 运维手册状态 (4 态机)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '草稿',   'info'],
    ['01', '生成中', 'warning'],
    ['02', '已生成', 'success'],
    ['03', '已发布', 'primary']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(MANUAL_OPS_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('空 → 默认草稿', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '草稿', type: 'info' })
  })
})

describe('manualOpsDict — 监控选型 (3 项)', () => {
  const cases: Array<[string, string]> = [
    ['prometheus_grafana', 'Prom+Grafana'],
    ['aliyun_monitor',     '阿里云'],
    ['zabbix',             'Zabbix']
  ]
  it.each(cases)('%s → 「%s」', (code, label) => {
    expect(monLabel(code)).toBe(label)
  })
  it('未知 → 裸值', () => {
    expect(monLabel('datadog')).toBe('datadog')
    expect(monLabel(undefined)).toBe('-')
  })
})

describe('manualOpsDict — 告警通道 (4 项)', () => {
  const cases: Array<[string, string]> = [
    ['dingtalk', '钉钉'],
    ['feishu',   '飞书'],
    ['wecom',    '企微'],
    ['email',    '邮件']
  ]
  it.each(cases)('%s → 「%s」', (code, label) => {
    expect(channelLabel(code)).toBe(label)
  })
  it('未知 → 裸值', () => {
    expect(channelLabel('slack')).toBe('slack')
    expect(channelLabel(undefined)).toBe('-')
  })
})
