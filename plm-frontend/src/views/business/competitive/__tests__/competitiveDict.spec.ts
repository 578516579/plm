import { describe, it, expect } from 'vitest'
import {
  COMPETITIVE_TIER, COMPETITIVE_STATUS,
  pricingTierLabel, pricingTierTag, statusTagFor,
  type TagType
} from '../competitiveDict'

describe('competitiveDict — 价格档 biz_competitive_tier (对齐 business-competitive.sql)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['free', '免费', 'success'],
    ['midrange', '中端', 'warning'],
    ['enterprise', '企业', 'danger']  // ⚠ SQL label 是「企业级」,详见末段 drift describe
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(COMPETITIVE_TIER[code]).toEqual({ label, tag })
    expect(pricingTierLabel(code)).toBe(label)
    expect(pricingTierTag(code)).toBe(tag)
  })
  it('共 3 档,与 SQL value 完整对齐', () => {
    expect(Object.keys(COMPETITIVE_TIER)).toEqual(['free', 'midrange', 'enterprise'])
  })
  it('未知/空 → 裸值 + info fallback', () => {
    expect(pricingTierLabel('premium')).toBe('premium')
    expect(pricingTierLabel(undefined)).toBe('-')
    expect(pricingTierLabel('')).toBe('-')
    expect(pricingTierTag('premium')).toBe('info')
    expect(pricingTierTag(undefined)).toBe('info')
  })
})

describe('competitiveDict — 状态 biz_competitive_status (3 态机:00→01→02 草稿→已发布→已归档)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '草稿', 'info'],
    ['01', '已发布', 'success'],
    ['02', '已归档', 'warning']  // ⚠ SQL list_class 是 'danger',详见末段 drift describe
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(COMPETITIVE_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 3 码,与 SQL biz_competitive_status value+label 完整对齐', () => {
    expect(Object.keys(COMPETITIVE_STATUS)).toEqual(['00', '01', '02'])
  })
  it('回归锁: 空/undefined 状态默认落「草稿」(00) — 保持旧 statusTagFor(s||"00") 行为', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '草稿', type: 'info' })
    expect(statusTagFor('')).toEqual({ label: '草稿', type: 'info' })
  })
  it('未知状态码 → 裸码 + info fallback', () => {
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
  })
})

describe('⚠ 已知显示层小漂移(锁定当前前端约定,UED/契约对齐走 spawn 任务卡)', () => {
  it('pricingTier enterprise: 前端 label「企业」, SQL biz_competitive_tier label 是「企业级」(仅一字之差,value 相同)', () => {
    expect(COMPETITIVE_TIER.enterprise.label).toBe('企业')
    // SQL 真值: dict_label='企业级' — 见 plm-backend/sql/business-competitive.sql:48
    // 若 UED 评审决定对齐 SQL,改本断言为 '企业级' 并同步 index.vue Dialog 下拉选项 line 254
  })
  it('status 02 已归档: 前端 tag「warning」(黄), SQL list_class 是「danger」(红)', () => {
    expect(COMPETITIVE_STATUS['02'].tag).toBe('warning')
    // SQL 真值: list_class='danger' — 见 plm-backend/sql/business-competitive.sql:53
    // 若 UED 评审决定对齐 SQL,改本断言为 'danger' (终态归档=红更醒目)
  })
})
