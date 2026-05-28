import { describe, it, expect } from 'vitest'
import { SPEC_TYPE, specTypeLabel, specTypeTag, type TagType } from '../openspecDict'

describe('openspecDict — 规范类型(4 选项)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['openapi_31',       'OpenAPI 3.1',  'primary'],
    ['asyncapi_30',      'AsyncAPI 3.0', 'success'],
    ['ai_function_spec', 'AI Function',  'warning'],
    ['graphql',          'GraphQL',      'info']
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(SPEC_TYPE[code]).toEqual({ label, tag })
    expect(specTypeLabel(code)).toBe(label)
    expect(specTypeTag(code)).toBe(tag)
  })
  it('共 4 项', () => {
    expect(Object.keys(SPEC_TYPE)).toEqual(['openapi_31', 'asyncapi_30', 'ai_function_spec', 'graphql'])
  })
  it('未知/空 → 裸值 + info fallback', () => {
    expect(specTypeLabel('proto3')).toBe('proto3')
    expect(specTypeLabel(undefined)).toBe('-')
    expect(specTypeTag('proto3')).toBe('info')
  })
})
