import { describe, it, expect } from 'vitest'
import {
  INCEPTION_BIZ_LINE, INCEPTION_TYPE, INCEPTION_STATUS,
  businessLineLabel, businessLineTag,
  inceptionTypeLabel, inceptionTypeTag,
  statusTagFor, type TagType
} from '../inceptionDict'

/**
 * inceptionDict 漂移锁定 — 逐项对齐 plm-backend/sql/business-inception.sql:
 *   biz_inception_biz_line (4) / biz_inception_type (4) / biz_inception_status (5)
 * 任一断言失败 = 前端字典与 SQL dict_data/list_class 漂移,按 SQL 重新校对。
 */

describe('inceptionDict — 业务线 biz_inception_biz_line (4 选项)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['plant_protection', '植保服务', 'primary'],
    ['precision_farming', '精准农业', 'success'],
    ['agri_supply', '农资流通', 'warning'],
    ['traceability', '质量溯源', 'info']
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(INCEPTION_BIZ_LINE[code]).toEqual({ label, tag })
    expect(businessLineLabel(code)).toBe(label)
    expect(businessLineTag(code)).toBe(tag)
  })
  it('共 4 码,与 SQL biz_inception_biz_line 完整对齐(无多无漏)', () => {
    expect(Object.keys(INCEPTION_BIZ_LINE)).toEqual([
      'plant_protection', 'precision_farming', 'agri_supply', 'traceability'
    ])
  })
  it('未知 / 空 → 裸值 + info fallback', () => {
    expect(businessLineLabel('xxx')).toBe('xxx')
    expect(businessLineLabel(undefined)).toBe('-')
    expect(businessLineLabel('')).toBe('-')
    expect(businessLineTag('xxx')).toBe('info')
  })
})

describe('inceptionDict — 项目类型 biz_inception_type (4 选项)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['new_product', '新产品研发', 'primary'],
    ['iteration', '版本迭代', 'success'],
    ['refactor', '技术重构', 'warning'],
    ['platform', '平台建设', 'info']
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(INCEPTION_TYPE[code]).toEqual({ label, tag })
    expect(inceptionTypeLabel(code)).toBe(label)
    expect(inceptionTypeTag(code)).toBe(tag)
  })
  it('共 4 码,与 SQL biz_inception_type 完整对齐', () => {
    expect(Object.keys(INCEPTION_TYPE)).toEqual([
      'new_product', 'iteration', 'refactor', 'platform'
    ])
  })
  it('未知 / 空 → 裸值 + info fallback', () => {
    expect(inceptionTypeLabel('zzz')).toBe('zzz')
    expect(inceptionTypeLabel(undefined)).toBe('-')
    expect(inceptionTypeTag('zzz')).toBe('info')
  })
})

describe('inceptionDict — 状态 biz_inception_status (5 状态机)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '草稿', 'info'],
    ['01', '已提交', 'warning'],
    ['02', '审批中', 'primary'],
    ['03', '已批准', 'success'],
    ['04', '已驳回', 'danger']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(INCEPTION_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 5 码,与 SQL biz_inception_status 完整对齐', () => {
    expect(Object.keys(INCEPTION_STATUS)).toEqual(['00', '01', '02', '03', '04'])
  })
  it('回归锁: 空 / undefined 状态默认落「草稿」(00) — 保持旧 statusTagFor(s || "00") 行为', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '草稿', type: 'info' })
    expect(statusTagFor('')).toEqual({ label: '草稿', type: 'info' })
  })
  it('未知状态码 → 裸码 + info fallback', () => {
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
  })
})
