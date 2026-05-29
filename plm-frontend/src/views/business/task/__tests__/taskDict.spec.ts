import { describe, it, expect } from 'vitest'
import {
  TASK_STATUS, TASK_PRIORITY, kanbanColumns,
  priorityTag, taskStatusTag,
  type TagType
} from '../taskDict'

describe('taskDict — 任务状态 biz_task_status (6 态机;✓ SQL 完整对齐,含 05 已取消)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '待开发',   'info'],
    ['01', '开发中',   'primary'],
    ['02', '代码评审', 'warning'],
    ['03', '测试中',   'warning'],
    ['04', '已完成',   'success'],
    ['05', '已取消',   'danger']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(TASK_STATUS[code]).toEqual({ label, tag: type })
    expect(taskStatusTag(code)).toEqual({ label, type })
  })
  it('共 6 码,与 SQL biz_task_status 完整对齐(无漂移)', () => {
    expect(Object.keys(TASK_STATUS)).toEqual(['00', '01', '02', '03', '04', '05'])
  })
  it('未知/空 → "-" + info', () => {
    expect(taskStatusTag(undefined)).toEqual({ label: '-', type: 'info' })
    expect(taskStatusTag('')).toEqual({ label: '-', type: 'info' })
    expect(taskStatusTag('99')).toEqual({ label: '99', type: 'info' })
  })
})

describe('taskDict — 优先级 (⚠ 前端 P0/P1/P2 vs SQL 00/01/02,同 testcase/defect 范式)', () => {
  const cases: Array<[string, TagType]> = [
    ['P0', 'danger'],
    ['P1', 'warning'],
    ['P2', 'info']
  ]
  it.each(cases)('%s → tag「%s」', (code, tag) => {
    expect(TASK_PRIORITY[code].tag).toBe(tag)
    expect(priorityTag(code)).toBe(tag)
  })
  it('共 3 档(锁前端 P0/P1/P2)', () => {
    expect(Object.keys(TASK_PRIORITY)).toEqual(['P0', 'P1', 'P2'])
  })
  it('未知/空 → info', () => {
    expect(priorityTag('P9')).toBe('info')
    expect(priorityTag(undefined)).toBe('info')
  })
})

describe('taskDict — 看板列(6 列,从 TASK_STATUS 派生)', () => {
  it('共 6 列,顺序与 status 码一致', () => {
    expect(kanbanColumns.map(c => c.status)).toEqual(['00', '01', '02', '03', '04', '05'])
  })
  it('每列 label 与 TASK_STATUS 一致', () => {
    kanbanColumns.forEach(c => expect(c.label).toBe(TASK_STATUS[c.status].label))
  })
})

describe('⚠ 已知契约漂移(PRIORITY 同 task/testcase/defect 范式,api-contract 评审走任务卡)', () => {
  it('PRIORITY 前端 P0-P2 作为 value vs SQL 00/01/02', () => {
    expect(Object.keys(TASK_PRIORITY)).toEqual(['P0', 'P1', 'P2'])
    // SQL value: '00' '01' '02' (label='P0 紧急'/'P1 重要'/'P2 一般')
    // 见 plm-backend/sql/business-task.sql:64-66
  })
})
