import { describe, it, expect } from 'vitest'
import {
  TESTPLAN_STATUS, TEST_TYPE,
  statusTagFor, testTypeLabel, testTypeTag, testTypeInfo,
  type TagType
} from '../testplanDict'

describe('testplanDict — 方案状态 biz_testplan_status (4 态机;对齐 business-testplan.sql)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '草稿',   'info'],
    ['01', '已确认', 'warning'],
    ['02', '执行中', 'primary'],
    ['03', '已完成', 'success']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(TESTPLAN_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 4 码,与 SQL biz_testplan_status value+label+tag 完整对齐(无漂移)', () => {
    expect(Object.keys(TESTPLAN_STATUS)).toEqual(['00', '01', '02', '03'])
  })
  it('回归锁: 空/undefined 状态默认落「草稿」(00)', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '草稿', type: 'info' })
    expect(statusTagFor('')).toEqual({ label: '草稿', type: 'info' })
  })
  it('未知状态码 → 裸码 + info fallback', () => {
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
  })
})

describe('testplanDict — 测试类型 (5 种;⚠ 前端独有,SQL 无 biz_testplan_type 字典,见末段 drift)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['functional',  '功能',   'primary'],
    ['api',         '接口',   'success'],
    ['performance', '性能',   'warning'],
    ['automation',  '自动化', 'info'],
    ['security',    '安全',   'danger']
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(TEST_TYPE[code]).toEqual({ label, tag })
    expect(testTypeLabel(code)).toBe(label)
    expect(testTypeTag(code)).toBe(tag)
    expect(testTypeInfo(code)).toEqual({ label, type: tag })
  })
  it('共 5 种(锁定当前前端约定,DDL test_types CSV 不约束枚举)', () => {
    expect(Object.keys(TEST_TYPE)).toEqual(['functional', 'api', 'performance', 'automation', 'security'])
  })
  it('未知/空 type → 裸值 + info fallback', () => {
    expect(testTypeLabel('unknown')).toBe('unknown')
    expect(testTypeLabel(undefined)).toBe('-')
    expect(testTypeTag('unknown')).toBe('info')
    expect(testTypeInfo('unknown')).toEqual({ label: 'unknown', type: 'info' })
  })
})

describe('⚠ 已知漂移(test_types 字典属前端独有,SQL DDL 仅约定 CSV 文本,未定义枚举)', () => {
  it('test_types 5 项为前端约定(SQL business-testplan.sql 中无 biz_testplan_type 字典)', () => {
    expect(Object.keys(TEST_TYPE).length).toBe(5)
    // 提示:DDL 行 12 注释 `CSV: functional,api,performance,automation,security`
    // 仅说明而非约束;若未来扩展(如 ai_test/compatibility 等)需同步本文件 + DDL 注释 + 后端校验
  })
})
