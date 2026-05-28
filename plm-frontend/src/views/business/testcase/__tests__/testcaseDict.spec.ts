import { describe, it, expect } from 'vitest'
import {
  TESTCASE_STATUS, TESTCASE_CATEGORY, TESTCASE_PRIORITY,
  statusTagFor, categoryLabel, categoryTag, priorityTag,
  type TagType
} from '../testcaseDict'

describe('testcaseDict — 用例状态 biz_testcase_status (5 态机;✓ SQL 完美对齐)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '草稿',   'info'],
    ['01', '待执行', 'warning'],
    ['02', '执行中', 'primary'],
    ['03', '已通过', 'success'],
    ['04', '已失败', 'danger']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(TESTCASE_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 5 码,与 SQL biz_testcase_status 完整对齐', () => {
    expect(Object.keys(TESTCASE_STATUS)).toEqual(['00', '01', '02', '03', '04'])
  })
  it('回归锁: statusTagFor 空 → fallback (旧实现用 s||"" 非 s||"00",空值走兜底)', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '-', type: 'info' })
    expect(statusTagFor('')).toEqual({ label: '-', type: 'info' })
  })
  it('未知状态码 → 裸码 + info fallback', () => {
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
  })
})

describe('testcaseDict — 用例分类 (⚠ 前端 5 项,与 SQL 7 项异构,见末段 drift §1)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['functional',  '功能',     'primary'],
    ['boundary',    '边界',     'warning'],
    ['exception',   '异常',     'danger'],
    ['agri',        '农业专项', 'success'],
    ['performance', '性能',     'info']
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(TESTCASE_CATEGORY[code]).toEqual({ label, tag })
    expect(categoryLabel(code)).toBe(label)
    expect(categoryTag(code)).toBe(tag)
  })
  it('共 5 项(锁定当前前端键名集合,⚠ SQL 有 7 项 01-07 与本完全异构)', () => {
    expect(Object.keys(TESTCASE_CATEGORY)).toEqual(['functional', 'boundary', 'exception', 'agri', 'performance'])
  })
  it('未知/空 → 裸值 + info fallback', () => {
    expect(categoryLabel('e2e')).toBe('e2e')
    expect(categoryLabel(undefined)).toBe('-')
    expect(categoryTag('e2e')).toBe('info')
  })
})

describe('testcaseDict — 用例优先级 (⚠ 前端 P0/P1/P2,SQL 00/01/02,同 task 范式)', () => {
  const cases: Array<[string, TagType]> = [
    ['P0', 'danger'],
    ['P1', 'warning'],
    ['P2', 'info']
  ]
  it.each(cases)('%s → tag「%s」', (code, tag) => {
    expect(TESTCASE_PRIORITY[code].tag).toBe(tag)
    expect(priorityTag(code)).toBe(tag)
  })
  it('共 3 档(锁前端 P0/P1/P2,⚠ 与 SQL value 00/01/02 错位)', () => {
    expect(Object.keys(TESTCASE_PRIORITY)).toEqual(['P0', 'P1', 'P2'])
  })
  it('未知/空 → info fallback', () => {
    expect(priorityTag('P9')).toBe('info')
    expect(priorityTag(undefined)).toBe('info')
  })
})

describe('⚠ 已知契约漂移(锁定当前前端约定,api-contract 评审走 spawn 任务卡)', () => {
  it('drift §1 CATEGORY 完全异构:前端 5 项字符串码 vs SQL 7 项数值码 (无交集)', () => {
    expect(Object.keys(TESTCASE_CATEGORY)).toEqual(['functional', 'boundary', 'exception', 'agri', 'performance'])
    // SQL value 集: '01'/'02'/'03'/'04'/'05'/'06'/'07' (功能/接口/性能/安全/兼容性/E2E/烟雾)
    // 见 plm-backend/sql/business-testcase.sql:48-54
    // 前端无 SQL 的「接口/安全/兼容性/E2E/烟雾」5 项;SQL 无前端的「边界/异常/农业专项」3 项
    // 仅「功能」+「性能」语义对应(label 同),但前端用字符串、SQL 用数值
    expect('01' in TESTCASE_CATEGORY).toBe(false)
    expect('functional' in TESTCASE_CATEGORY).toBe(true)
  })
  it('drift §2 PRIORITY 存储表示错位:前端 P0/P1/P2,SQL 00/01/02 (同 task 范式)', () => {
    expect(Object.keys(TESTCASE_PRIORITY)).toEqual(['P0', 'P1', 'P2'])
    // SQL value: '00'/'01'/'02' (label='P0 关键'/'P1 主要'/'P2 次要')
    // 见 plm-backend/sql/business-testcase.sql:58-60
    // 颜色 tag 语义一致 (P0→danger, P1→warning, P2→info);仅 value 表示不同
  })
})
