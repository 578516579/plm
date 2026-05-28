import { describe, it, expect } from 'vitest'
import { UED_STATUS, statusTagFor, type TagType } from '../uedDict'

describe('uedDict — UED 设计状态 biz_ued_status (4 态机;✓ SQL 完整对齐)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '草稿',   'info'],
    ['01', '评审中', 'warning'],
    ['02', '已确认', 'success'],
    ['03', '已废弃', 'danger']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(UED_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 4 码,与 SQL 完整对齐', () => {
    expect(Object.keys(UED_STATUS)).toEqual(['00', '01', '02', '03'])
  })
  it('回归锁: 空 → "00" 草稿;未知 → 裸码', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '草稿', type: 'info' })
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
  })
})
