import { describe, it, expect } from 'vitest'
import {
  RELEASE_STATUS, RELEASE_STRATEGY,
  statusTagFor, strategyLabel, strategyTag, strategyHint, strategyIcon,
  type TagType
} from '../releaseDict'

describe('releaseDict — 发布状态 biz_release_status (5 态机;对齐 business-release.sql)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '计划中', 'info'],
    ['01', '发布中', 'warning'],
    ['02', '已发布', 'success'],
    ['03', '已回滚', 'danger'],
    ['04', '已废弃', 'info']   // ⚠ SQL list_class 为空,见末段 drift
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(RELEASE_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 5 码,与 SQL biz_release_status value+label 完整对齐', () => {
    expect(Object.keys(RELEASE_STATUS)).toEqual(['00', '01', '02', '03', '04'])
  })
  it('回归锁: 空/undefined 状态默认落「计划中」(00) — 保持旧 statusTagFor(s||"00") 行为', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '计划中', type: 'info' })
    expect(statusTagFor('')).toEqual({ label: '计划中', type: 'info' })
  })
  it('未知状态码 → 裸码 + info fallback (相比旧版 undefined 兜底为 "-",更稳)', () => {
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
  })
})

describe('releaseDict — 发布策略 biz_release_strategy (3 种;tag 来自 SQL list_class)', () => {
  const cases: Array<[string, string, TagType, string]> = [
    ['blue_green', '蓝绿',  'primary', '🟦'],
    ['canary',     '金丝雀', 'warning', '🐤'],
    ['rolling',    '滚动',  'success', '🔄']
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」/ icon「%s」', (code, label, tag, icon) => {
    expect(RELEASE_STRATEGY[code].label).toBe(label)
    expect(RELEASE_STRATEGY[code].tag).toBe(tag)
    expect(RELEASE_STRATEGY[code].icon).toBe(icon)
    expect(strategyLabel(code)).toBe(label)
    expect(strategyTag(code)).toBe(tag)
    expect(strategyIcon(code)).toBe(icon)
  })
  it('共 3 种,与 SQL biz_release_strategy value 完整对齐', () => {
    expect(Object.keys(RELEASE_STRATEGY)).toEqual(['blue_green', 'canary', 'rolling'])
  })
  it('hint 含策略 emoji 前缀 + 关键描述(逐策略锁定)', () => {
    expect(strategyHint('blue_green')).toMatch(/^🟦 蓝绿:.*瞬时完成.*资源 2x$/)
    expect(strategyHint('canary')).toMatch(/^🐤 金丝雀:.*5%~10%.*渐进扩量$/)
    expect(strategyHint('rolling')).toMatch(/^🔄 滚动:.*25%.*回滚慢$/)
  })
  it('未知/空 strategy → 裸值 + info fallback + 空 hint/icon', () => {
    expect(strategyLabel('xxx')).toBe('xxx')
    expect(strategyLabel(undefined)).toBe('-')
    expect(strategyTag('xxx')).toBe('info')
    expect(strategyHint('xxx')).toBe('')
    expect(strategyHint(undefined)).toBe('')
    expect(strategyIcon('xxx')).toBe('')
  })
})

describe('⚠ 已知显示层小漂移(status 04 已废弃 tag,锁当前前端约定;UED/SQL 对齐走任务卡)', () => {
  it('status 04: 前端 tag「info」(灰), SQL biz_release_status list_class 为空(未定义)', () => {
    expect(RELEASE_STATUS['04'].tag).toBe('info')
    // SQL 真值: list_class='' (空) — 见 plm-backend/sql/business-release.sql:54
    // 前端补默认 info 灰色徽章;若 UED 决策填具体颜色(如 warning 灰黄),改本断言并同步 SQL
  })
})
