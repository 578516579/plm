import { describe, it, expect } from 'vitest'
import {
  ARCH_STATUS, ARCH_MODE, ARCH_STACK,
  statusTagFor, enumLabel,
  type TagType
} from '../archDict'

describe('archDict — 架构状态 biz_arch_status (4 态机;✓ SQL 完美对齐)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '草稿',   'info'],
    ['01', '评审中', 'warning'],
    ['02', '已确认', 'success'],
    ['03', '已废弃', 'danger']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(ARCH_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 4 码,与 SQL 完整对齐', () => {
    expect(Object.keys(ARCH_STATUS)).toEqual(['00', '01', '02', '03'])
  })
  it('回归锁: 空 → "00" 草稿', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '草稿', type: 'info' })
    expect(statusTagFor('')).toEqual({ label: '草稿', type: 'info' })
  })
})

describe('archDict — 架构模式 biz_arch_mode (4 选项;⚠ label 简化,见末段 drift §1)', () => {
  const cases: Array<[string, string]> = [
    ['microservice', '微服务'],   // ⚠ SQL '微服务架构'
    ['monolith',     '单体'],     // ⚠ SQL '单体架构'
    ['serverless',   'Serverless'],
    ['layered',      '分层']      // ⚠ SQL '分层架构'
  ]
  it.each(cases)('%s → label「%s」', (code, label) => {
    expect(ARCH_MODE[code].label).toBe(label)
    expect(enumLabel('arch', code)).toBe(label)
  })
  it('未知 → 裸值', () => {
    expect(enumLabel('arch', 'event_driven')).toBe('event_driven')
    expect(enumLabel('arch', undefined)).toBe('-')
  })
})

describe('archDict — 技术栈 biz_arch_stack (4 选项;⚠ label 简化,见末段 drift §2)', () => {
  const cases: Array<[string, string]> = [
    ['java_sb3',       'Java SB3'],  // ⚠ SQL 'Java (SpringBoot3)'
    ['go_gin',         'Go Gin'],    // ⚠ SQL 'Go (Gin)'
    ['python_fastapi', 'Python'],    // ⚠ SQL 'Python (FastAPI)'
    ['nodejs',         'Node.js']
  ]
  it.each(cases)('%s → label「%s」', (code, label) => {
    expect(ARCH_STACK[code].label).toBe(label)
    expect(enumLabel('stack', code)).toBe(label)
  })
  it('未知 → 裸值', () => {
    expect(enumLabel('stack', 'rust_axum')).toBe('rust_axum')
  })
})

describe('⚠ 已知显示层小漂移(label 简化,锁当前)', () => {
  it('drift §1 archMode label 简化:前端「微服务/单体/分层」, SQL 含「架构」后缀', () => {
    expect(ARCH_MODE.microservice.label).toBe('微服务')
    expect(ARCH_MODE.monolith.label).toBe('单体')
    expect(ARCH_MODE.layered.label).toBe('分层')
    // SQL 真值: '微服务架构'/'单体架构'/'分层架构' — 见 plm-backend/sql/business-arch.sql:49-52
  })
  it('drift §2 primaryStack label 简化:前端短缩写, SQL 含括号版本', () => {
    expect(ARCH_STACK.java_sb3.label).toBe('Java SB3')      // SQL 'Java (SpringBoot3)'
    expect(ARCH_STACK.go_gin.label).toBe('Go Gin')          // SQL 'Go (Gin)'
    expect(ARCH_STACK.python_fastapi.label).toBe('Python')  // SQL 'Python (FastAPI)'
  })
})
