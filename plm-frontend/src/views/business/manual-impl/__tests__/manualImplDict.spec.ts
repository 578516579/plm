import { describe, it, expect } from 'vitest'
import {
  MANUAL_IMPL_STATUS, DEPLOY_LABEL, OS_LABEL,
  statusTagFor, deployLabel, osLabel,
  type TagType
} from '../manualImplDict'

describe('manualImplDict — 实施手册状态 (4 态机)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '草稿',   'info'],
    ['01', '生成中', 'warning'],
    ['02', '已生成', 'success'],
    ['03', '已发布', 'primary']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(MANUAL_IMPL_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('空 → 默认草稿', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '草稿', type: 'info' })
  })
})

describe('manualImplDict — 部署方式 (3 项)', () => {
  const cases: Array<[string, string]> = [
    ['docker_compose', 'Docker'],
    ['k8s',            'K8s'],
    ['baremetal',      '裸机']
  ]
  it.each(cases)('%s → 「%s」', (code, label) => {
    expect(deployLabel(code)).toBe(label)
    expect(DEPLOY_LABEL[code]).toBe(label)
  })
  it('未知 → 裸值', () => {
    expect(deployLabel('serverless')).toBe('serverless')
    expect(deployLabel(undefined)).toBe('-')
  })
})

describe('manualImplDict — 操作系统 (3 项)', () => {
  const cases: Array<[string, string]> = [
    ['centos', 'CentOS'],
    ['ubuntu', 'Ubuntu'],
    ['kylin',  '麒麟']
  ]
  it.each(cases)('%s → 「%s」', (code, label) => {
    expect(osLabel(code)).toBe(label)
    expect(OS_LABEL[code]).toBe(label)
  })
  it('未知 → 裸值', () => {
    expect(osLabel('debian')).toBe('debian')
    expect(osLabel(undefined)).toBe('-')
  })
})
