import { describe, it, expect } from 'vitest'
import {
  TESTREPORT_STATUS, TESTREPORT_RISK,
  statusTagFor, riskTagFor,
  riskIcon, riskLongLabel, riskHint, riskCls,
  type TagType
} from '../testreportDict'

describe('testreportDict — 测试报告状态 biz_testreport_status (3 态;对齐 business-testreport.sql)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '草稿',   'info'],
    ['01', '审核中', 'warning'],
    ['02', '已发布', 'success']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(TESTREPORT_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 3 码,与 SQL biz_testreport_status value+label+tag 完整对齐(无漂移)', () => {
    expect(Object.keys(TESTREPORT_STATUS)).toEqual(['00', '01', '02'])
  })
  it('回归锁: 空/undefined 状态默认落「草稿」(00) — 保持旧 statusTagFor(s||"00") 行为', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '草稿', type: 'info' })
    expect(statusTagFor('')).toEqual({ label: '草稿', type: 'info' })
  })
  it('未知状态码 → 裸码 + info fallback', () => {
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
  })
})

describe('testreportDict — 风险评级 biz_testreport_risk (3 级 绿/黄/红;label 含 emoji)', () => {
  const cases: Array<[string, string, TagType, string, string]> = [
    ['green',  '🟢 绿灯', 'success', '🟢', '绿灯 - 可以发布'],
    ['yellow', '🟡 黄灯', 'warning', '🟡', '黄灯 - 需谨慎,建议二次评审'],
    ['red',    '🔴 红灯', 'danger',  '🔴', '红灯 - 禁止上线']
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」/ icon「%s」/ longLabel「%s」', (code, label, tag, icon, longLabel) => {
    expect(TESTREPORT_RISK[code].label).toBe(label)
    expect(TESTREPORT_RISK[code].tag).toBe(tag)
    expect(TESTREPORT_RISK[code].icon).toBe(icon)
    expect(TESTREPORT_RISK[code].longLabel).toBe(longLabel)
    expect(riskTagFor(code)).toEqual({ label, type: tag })
    expect(riskIcon(code)).toBe(icon)
    expect(riskLongLabel(code)).toBe(longLabel)
  })
  it('共 3 级,与 SQL biz_testreport_risk value 完整对齐', () => {
    expect(Object.keys(TESTREPORT_RISK)).toEqual(['green', 'yellow', 'red'])
  })
  it('riskHint 含 P0/灰度/覆盖率 关键词,逐级锁定', () => {
    expect(riskHint('green')).toMatch(/可走标准发布流程/)
    expect(riskHint('yellow')).toMatch(/灰度/)
    expect(riskHint('red')).toMatch(/P0/)
  })
  it('riskCls 走 `risk-${level}` 模板,允许未知值(保持旧行为)', () => {
    expect(riskCls('green')).toBe('risk-green')
    expect(riskCls('yellow')).toBe('risk-yellow')
    expect(riskCls('red')).toBe('risk-red')
    expect(riskCls('unknown')).toBe('risk-unknown')  // ⚠ 旧实现允许未知值
    expect(riskCls(undefined)).toBe('risk-green')    // 空→默认 green
  })
  it('回归锁: 空/undefined 风险默认落「绿灯」(green) — 保持旧 riskTagFor(s||"green") 行为', () => {
    expect(riskTagFor(undefined)).toEqual({ label: '🟢 绿灯', type: 'success' })
    expect(riskIcon(undefined)).toBe('🟢')
    expect(riskLongLabel(undefined)).toBe('绿灯 - 可以发布')
    expect(riskHint(undefined)).toMatch(/可走标准发布流程/)
  })
  it('未知 risk → 裸值 + info fallback (label/icon/longLabel/hint 走兜底)', () => {
    expect(riskTagFor('xxx')).toEqual({ label: 'xxx', type: 'info' })
    expect(riskIcon('xxx')).toBe('⚪')
    expect(riskLongLabel('xxx')).toBe('未评级')
    expect(riskHint('xxx')).toBe('')
  })
})

describe('⚠ 已知显示层小漂移(risk label 风格不同,锁当前前端约定,UED 走 spawn 任务卡)', () => {
  it('drift §1 risk label 风格:前端 emoji 前缀「🟢 绿灯」, SQL 纯文本+描述「绿 (低风险)」', () => {
    expect(TESTREPORT_RISK.green.label).toBe('🟢 绿灯')
    expect(TESTREPORT_RISK.yellow.label).toBe('🟡 黄灯')
    expect(TESTREPORT_RISK.red.label).toBe('🔴 红灯')
    // SQL 真值: dict_label='绿 (低风险)' / '黄 (中风险)' / '红 (高风险)'
    // — 见 plm-backend/sql/business-testreport.sql:47-49
  })
})
