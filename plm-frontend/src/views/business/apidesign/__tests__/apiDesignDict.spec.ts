import { describe, it, expect } from 'vitest'
import {
  APIDESIGN_STATUS, HTTP_METHOD_TAG,
  statusTagFor, methodTag,
  type TagType
} from '../apiDesignDict'

describe('apiDesignDict — 接口设计状态 biz_apidesign_status (4 态机;✓ SQL 对齐)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '草稿',   'info'],
    ['01', '评审中', 'warning'],
    ['02', '已确认', 'success'],
    ['03', '已废弃', 'danger']
  ]
  it.each(cases)('状态 %s → label「%s」/ type「%s」', (code, label, type) => {
    expect(APIDESIGN_STATUS[code]).toEqual({ label, tag: type })
    expect(statusTagFor(code)).toEqual({ label, type })
  })
  it('共 4 码', () => {
    expect(Object.keys(APIDESIGN_STATUS)).toEqual(['00', '01', '02', '03'])
  })
  it('空 → 默认草稿;未知 → 裸码', () => {
    expect(statusTagFor(undefined)).toEqual({ label: '草稿', type: 'info' })
    expect(statusTagFor('99')).toEqual({ label: '99', type: 'info' })
  })
})

describe('apiDesignDict — HTTP method 标色(5 项,前端 UI 约定)', () => {
  const cases: Array<[string, TagType]> = [
    ['GET',    'success'],
    ['POST',   'primary'],
    ['PUT',    'warning'],
    ['DELETE', 'danger'],
    ['PATCH',  'info']
  ]
  it.each(cases)('%s → tag「%s」', (method, tag) => {
    expect(methodTag(method)).toBe(tag)
    expect(HTTP_METHOD_TAG[method]).toBe(tag)
  })
  it('未知 method → info fallback', () => {
    expect(methodTag('OPTIONS')).toBe('info')
    expect(methodTag(undefined)).toBe('info')
  })
})
