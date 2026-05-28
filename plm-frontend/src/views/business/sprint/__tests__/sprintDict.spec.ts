import { describe, it, expect } from 'vitest'
import {
  SPRINT_STATUS,
  statusTagFor,
  type TagType
} from '../sprintDict'

describe('sprintDict — 迭代状态 biz_sprint_status (4 态机;✓ SQL 完整对齐,无漂移)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '计划中', 'info'],
    ['01', '进行中', 'primary'],
    ['02', '已完成', 'success'],
    ['03', '已取消', 'danger']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(SPRINT_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 4 码,与 SQL biz_sprint_status value+label+tag 完整对齐', () => {
    expect(Object.keys(SPRINT_STATUS)).toEqual(['00', '01', '02', '03'])
  })
  it('回归锁: 空/undefined → "-" + info (空走 fallback)', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '-', type: 'info' })
    expect(statusTagFor('')).toEqual({ label: '-', type: 'info' })
  })
  it('未知 → 裸码 + info', () => {
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
  })
})
