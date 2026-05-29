import { describe, it, expect } from 'vitest'
import {
  DEFECT_STATUS, DEFECT_SEVERITY, DEFECT_CATEGORY,
  statusTagFor, severityTag, categoryLabel, categoryTag,
  type TagType
} from '../defectDict'

describe('defectDict — 缺陷状态 biz_defect_status (5 态机;label 对齐 SQL)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '新建',   'info'],
    ['01', '已确认', 'warning'],
    ['02', '处理中', 'primary'],
    ['03', '已解决', 'success'],
    ['04', '已关闭', 'info']   // ⚠ SQL list_class 空,见末段 drift §4
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(DEFECT_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 5 码,与 SQL biz_defect_status 完整对齐(values+labels)', () => {
    expect(Object.keys(DEFECT_STATUS)).toEqual(['00', '01', '02', '03', '04'])
  })
  it('回归锁: 空/undefined → "-" + info (空走 fallback)', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '-', type: 'info' })
    expect(statusTagFor('')).toEqual({ label: '-', type: 'info' })
  })
  it('未知 → 裸码 + info', () => {
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
  })
})

describe('defectDict — 严重级别(⚠ 前端 P0-P3,SQL 00-03,见末段 drift §1+§2)', () => {
  const cases: Array<[string, TagType]> = [
    ['P0', 'danger'],   // SQL 00 → danger ✓
    ['P1', 'danger'],   // ⚠ SQL 01 → warning
    ['P2', 'warning'],  // ⚠ SQL 02 → info
    ['P3', 'info']      // ⚠ SQL 03 → success
  ]
  it.each(cases)('%s → tag「%s」', (code, tag) => {
    expect(DEFECT_SEVERITY[code].tag).toBe(tag)
    expect(severityTag(code)).toBe(tag)
  })
  it('共 4 档(锁前端 P0-P3 命名)', () => {
    expect(Object.keys(DEFECT_SEVERITY)).toEqual(['P0', 'P1', 'P2', 'P3'])
  })
  it('未知/空 → info fallback', () => {
    expect(severityTag('P9')).toBe('info')
    expect(severityTag(undefined)).toBe('info')
  })
})

describe('defectDict — 分类(⚠ 前端 4 字符串码,SQL 6 数值码异构,见末段 drift §3)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['functional',  '功能',  'primary'],
    ['performance', '性能',  'warning'],
    ['ui',          'UI/UX', 'info'],
    ['security',    '安全',  'danger']
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(DEFECT_CATEGORY[code]).toEqual({ label, tag })
    expect(categoryLabel(code)).toBe(label)
    expect(categoryTag(code)).toBe(tag)
  })
  it('共 4 项(锁前端 string 键名;⚠ SQL 是 6 项 01/02/03/04/05/99)', () => {
    expect(Object.keys(DEFECT_CATEGORY)).toEqual(['functional', 'performance', 'ui', 'security'])
  })
  it('未知/空 → 裸值 + info', () => {
    expect(categoryLabel('other')).toBe('other')
    expect(categoryLabel(undefined)).toBe('-')
    expect(categoryTag('other')).toBe('info')
  })
})

describe('⚠ 已知契约漂移(锁定当前前端约定,api-contract 评审走 spawn 任务卡)', () => {
  it('drift §1 SEVERITY value 表示错位:前端 P0-P3 vs SQL 00-03 (同 task 范式)', () => {
    expect(Object.keys(DEFECT_SEVERITY)).toEqual(['P0', 'P1', 'P2', 'P3'])
    // SQL value: '00' '01' '02' '03' — 见 plm-backend/sql/business-defect.sql:49-52
  })
  it('drift §2 SEVERITY tag 颜色策略差异:前端 P1 升级为 danger (P0+P1 同级)', () => {
    // SQL:  00→danger / 01→warning / 02→info    / 03→success
    // 前端: P0→danger / P1→danger  / P2→warning / P3→info
    expect(DEFECT_SEVERITY.P1.tag).toBe('danger')  // ⚠ SQL 01 是 warning
    expect(DEFECT_SEVERITY.P2.tag).toBe('warning') // ⚠ SQL 02 是 info
    expect(DEFECT_SEVERITY.P3.tag).toBe('info')    // ⚠ SQL 03 是 success
  })
  it('drift §3 CATEGORY 完全异构:前端 4 字符串码 vs SQL 6 数值码', () => {
    expect(Object.keys(DEFECT_CATEGORY).length).toBe(4)
    // SQL value: '01' '02' '03' '04' '05' '99' (功能/性能/兼容性/安全/易用性/其他)
    // 前端无 SQL 的「兼容性/易用性/其他」;前端 'ui' (UI/UX) SQL 也无对应
    // 仅「功能/性能/安全」语义对应
    expect('ui' in DEFECT_CATEGORY).toBe(true)
    expect('01' in DEFECT_CATEGORY).toBe(false)
  })
  it('drift §4 STATUS 04 已关闭 tag:前端 info, SQL list_class 空 (前端补默认)', () => {
    expect(DEFECT_STATUS['04'].tag).toBe('info')
    // SQL 真值: list_class='' — 见 plm-backend/sql/business-defect.sql:69
  })
})
