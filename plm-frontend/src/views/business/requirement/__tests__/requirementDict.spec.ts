/**
 * requirementDict SSoT 单测 — 锁定需求模块字典映射对齐 business-requirement.sql
 *
 * 逐项锁定 biz_req_source(01-04) / biz_req_priority(00-02) / biz_req_status(00-03)
 * 的 code → label + el-tag type 映射。此 spec 失败 = requirementDict.ts 与
 * plm-backend/sql/business-requirement.sql 字典漂移,必须按 SQL 重新校对。
 * (参照 utils/__tests__/businessRoute.spec.ts 的漂移锁定模式)
 *
 * 字典基线(business-requirement.sql):
 *   biz_req_source   01 客户反馈/primary · 02 内部提案/info · 03 运营数据/warning · 04 竞品分析/success
 *   biz_req_priority 00 P0/danger · 01 P1/warning · 02 P2/info
 *   biz_req_status   00 待评审/warning · 01 开发中/primary · 02 已完成/success · 03 已取消/danger
 */
import { describe, it, expect } from 'vitest'
import {
  sourceLabel, sourceTag,
  priorityLabel, priorityTag,
  statusTagFor,
  aiEvalLabel, aiEvalTag,
  REQ_SOURCE, REQ_PRIORITY, REQ_STATUS, REQ_AI_EVAL
} from '../requirementDict'

// ─────────────────────────────────────────────────────────────────────────────
// 需求来源 biz_req_source
// ─────────────────────────────────────────────────────────────────────────────
describe('需求来源 sourceLabel / sourceTag (biz_req_source)', () => {
  it('01 客户反馈 → primary', () => {
    expect(sourceLabel('01')).toBe('客户反馈')
    expect(sourceTag('01')).toBe('primary')
  })
  it('02 内部提案 → info', () => {
    expect(sourceLabel('02')).toBe('内部提案')
    expect(sourceTag('02')).toBe('info')
  })
  it('03 运营数据 → warning', () => {
    expect(sourceLabel('03')).toBe('运营数据')
    expect(sourceTag('03')).toBe('warning')
  })
  it('04 竞品分析 → success', () => {
    expect(sourceLabel('04')).toBe('竞品分析')
    expect(sourceTag('04')).toBe('success')
  })
  it('共 4 项 (与 SQL 字典条数一致)', () => {
    expect(Object.keys(REQ_SOURCE)).toHaveLength(4)
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// 优先级 biz_req_priority
// ─────────────────────────────────────────────────────────────────────────────
describe('优先级 priorityLabel / priorityTag (biz_req_priority)', () => {
  it('00 P0 → danger', () => {
    expect(priorityLabel('00')).toBe('P0')
    expect(priorityTag('00')).toBe('danger')
  })
  it('01 P1 → warning', () => {
    expect(priorityLabel('01')).toBe('P1')
    expect(priorityTag('01')).toBe('warning')
  })
  it('02 P2 → info', () => {
    expect(priorityLabel('02')).toBe('P2')
    expect(priorityTag('02')).toBe('info')
  })
  it('共 3 项', () => {
    expect(Object.keys(REQ_PRIORITY)).toHaveLength(3)
  })
  // 回归: 详情对话框曾直接渲染原始 priority 码(index.vue:170),
  // source/priority 从 P0 迁到字典码 00 后会显示 "00" 而非 "P0";
  // priorityLabel 必须把字典码翻成可读短码。
  it('回归: 字典码 00 不再裸显示, 必须翻成 P0', () => {
    expect(priorityLabel('00')).toBe('P0')
    expect(priorityLabel('00')).not.toBe('00')
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// 状态 biz_req_status
// ─────────────────────────────────────────────────────────────────────────────
describe('状态 statusTagFor (biz_req_status)', () => {
  it('00 → 待评审 / warning', () => {
    expect(statusTagFor('00')).toEqual({ label: '待评审', type: 'warning' })
  })
  it('01 → 开发中 / primary', () => {
    expect(statusTagFor('01')).toEqual({ label: '开发中', type: 'primary' })
  })
  it('02 → 已完成 / success (终态)', () => {
    expect(statusTagFor('02')).toEqual({ label: '已完成', type: 'success' })
  })
  it('03 → 已取消 / danger (终态)', () => {
    expect(statusTagFor('03')).toEqual({ label: '已取消', type: 'danger' })
  })
  it('共 4 项', () => {
    expect(Object.keys(REQ_STATUS)).toHaveLength(4)
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// AI 评估 (非字典, 前端约定 high/medium/low)
// ─────────────────────────────────────────────────────────────────────────────
describe('AI 评估 aiEvalLabel / aiEvalTag', () => {
  it('high 高价值 → success', () => {
    expect(aiEvalLabel('high')).toBe('高价值')
    expect(aiEvalTag('high')).toBe('success')
  })
  it('medium 中价值 → warning', () => {
    expect(aiEvalLabel('medium')).toBe('中价值')
    expect(aiEvalTag('medium')).toBe('warning')
  })
  it('low 低价值 → info', () => {
    expect(aiEvalLabel('low')).toBe('低价值')
    expect(aiEvalTag('low')).toBe('info')
  })
  it('共 3 项', () => {
    expect(Object.keys(REQ_AI_EVAL)).toHaveLength(3)
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// 边界 / fallback
// ─────────────────────────────────────────────────────────────────────────────
describe('边界 / fallback', () => {
  it('undefined → label "-" / tag "info"', () => {
    expect(sourceLabel(undefined)).toBe('-')
    expect(sourceTag(undefined)).toBe('info')
    expect(priorityLabel(undefined)).toBe('-')
    expect(priorityTag(undefined)).toBe('info')
    expect(aiEvalLabel(undefined)).toBe('-')
    expect(aiEvalTag(undefined)).toBe('info')
    expect(statusTagFor(undefined)).toEqual({ label: '-', type: 'info' })
  })
  it('空串 → label "-" / tag "info"', () => {
    expect(sourceLabel('')).toBe('-')
    expect(priorityLabel('')).toBe('-')
    expect(statusTagFor('')).toEqual({ label: '-', type: 'info' })
  })
  it('未知码 → label 原样返回, tag 回落 info', () => {
    expect(sourceLabel('99')).toBe('99')
    expect(sourceTag('99')).toBe('info')
    expect(priorityLabel('99')).toBe('99')
    expect(priorityTag('99')).toBe('info')
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
    expect(aiEvalLabel('xyz')).toBe('xyz')
    expect(aiEvalTag('xyz')).toBe('info')
  })
})
