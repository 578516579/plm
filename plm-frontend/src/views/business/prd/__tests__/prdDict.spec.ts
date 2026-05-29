import { describe, it, expect } from 'vitest'
import {
  PRD_STATUS,
  statusTagFor,
  type TagType
} from '../prdDict'

describe('prdDict — PRD 状态 biz_prd_status (4 态机;✓ SQL 完整对齐,无漂移)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '草稿',   'info'],
    ['01', '评审中', 'warning'],
    ['02', '已确认', 'success'],
    ['03', '已废弃', 'danger']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(PRD_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 4 码,与 SQL biz_prd_status value+label+tag 完整对齐', () => {
    expect(Object.keys(PRD_STATUS)).toEqual(['00', '01', '02', '03'])
  })
  it('回归锁: 空/undefined → "00" 草稿', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '草稿', type: 'info' })
    expect(statusTagFor('')).toEqual({ label: '草稿', type: 'info' })
  })
  it('未知 → 裸码 + info', () => {
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
  })
})
