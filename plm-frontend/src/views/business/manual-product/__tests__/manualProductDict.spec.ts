import { describe, it, expect } from 'vitest'
import { MANUAL_PRODUCT_STATUS, statusTagFor, type TagType } from '../manualProductDict'

describe('manualProductDict — 产品手册状态 (4 态机)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '草稿',   'info'],
    ['01', '生成中', 'warning'],
    ['02', '已生成', 'success'],
    ['03', '已发布', 'primary']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(MANUAL_PRODUCT_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 4 码', () => {
    expect(Object.keys(MANUAL_PRODUCT_STATUS)).toEqual(['00', '01', '02', '03'])
  })
  it('空 → 默认草稿;未知 → 裸码', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '草稿', type: 'info' })
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
  })
})
