import { describe, it, expect } from 'vitest'
import {
  PROJECT_STATUS, TASK_PRIORITY, DASHBOARD_TASK_STATUS, DASHBOARD_RISK,
  statusTagFor, priorityTagFor, priorityClass, taskStatusTagFor, riskTagFor,
  type TagType
} from '../dashboardDict'

describe('dashboardDict — 项目状态 (⚠ 单字符码 0-4,与典型 00-04 不同)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['0', '未启动', 'info'],
    ['1', '进行中', 'primary'],
    ['2', '暂停',   'warning'],
    ['3', '已完成', 'success'],
    ['4', '已取消', 'danger']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(PROJECT_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 5 态,单字符码(⚠ 历史 RuoYi 约定,与其他业务模块 00-04 不一致)', () => {
    expect(Object.keys(PROJECT_STATUS)).toEqual(['0', '1', '2', '3', '4'])
  })
  it('回归锁: 空/undefined 默落 "0" 未启动', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '未启动', type: 'info' })
    expect(statusTagFor('')).toEqual({ label: '未启动', type: 'info' })
  })
})

describe('dashboardDict — 任务优先级 (与 task 模块同 label)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', 'P0', 'danger'],
    ['01', 'P1', 'warning'],
    ['02', 'P2', 'info']
  ]
  it.each(cases)('优先级 %s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(TASK_PRIORITY[code]).toEqual({ label, tag })
    expect(priorityTagFor(code)).toEqual({ label, type: tag })
  })
  it('priorityClass: 00→p0 / 01→p1 / 02→p2 / 未知→p2 (fallback)', () => {
    expect(priorityClass('00')).toBe('p0')
    expect(priorityClass('01')).toBe('p1')
    expect(priorityClass('02')).toBe('p2')
    expect(priorityClass('99')).toBe('p2')   // fallback
    expect(priorityClass(undefined)).toBe('p2')  // fallback
  })
  it('回归锁: priorityTagFor 空/undefined 默落 "02" P2', () => {
    expect(priorityTagFor(undefined)).toEqual({ label: 'P2', type: 'info' })
  })
})

describe('dashboardDict — 任务状态 (与 task 模块同 label;workbench 视角)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '待开发',   'info'],
    ['01', '开发中',   'primary'],
    ['02', '代码评审', 'warning'],
    ['03', '测试中',   'warning'],
    ['04', '已完成',   'success']
  ]
  it.each(cases)('任务状态 %s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(DASHBOARD_TASK_STATUS[code]).toEqual({ label, tag })
    expect(taskStatusTagFor(code)).toEqual({ label, type: tag })
  })
  it('回归锁: taskStatusTagFor 空 → "00" 待开发', () => {
    expect(taskStatusTagFor(undefined)).toEqual({ label: '待开发', type: 'info' })
  })
})

describe('dashboardDict — 风险等级 (value 同 testreport,⚠ label 不同 — workbench 偏好)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['green',  '🟢 健康', 'success'],
    ['yellow', '🟡 一般', 'warning'],
    ['red',    '🔴 风险', 'danger']
  ]
  it.each(cases)('风险 %s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(DASHBOARD_RISK[code]).toEqual({ label, tag })
    expect(riskTagFor(code)).toEqual({ label, type: tag })
  })
  it('回归锁: riskTagFor 空 → green 健康 (workbench 默认乐观)', () => {
    expect(riskTagFor(undefined)).toEqual({ label: '🟢 健康', type: 'success' })
  })
  it('未知 risk → "—" + info', () => {
    expect(riskTagFor('xxx')).toEqual({ label: '—', type: 'info' })
  })
})

describe('⚠ 已知跨上下文 label 差异(同 value 不同 label,workbench vs 其他模块 vs SQL)', () => {
  it('drift §1 RISK label 三层差异:dashboard 健康/SQL 低风险/testreport 绿灯', () => {
    expect(DASHBOARD_RISK.green.label).toBe('🟢 健康')
    // testreport: '🟢 绿灯' — 见 testreportDict.ts
    // SQL: '绿 (低风险)' — 见 plm-backend/sql/business-testreport.sql:47
    // 三层 label 不同属各上下文偏好,value+tag 一致
  })
  it('drift §2 PROJECT_STATUS 用单字符码 0-4 vs 其他业务模块 00-04', () => {
    expect(Object.keys(PROJECT_STATUS).every(k => k.length === 1)).toBe(true)
    // 其他模块如 task/requirement/release 用 '00'-'04';project 历史保持单字符
  })
})
