import { describe, it, expect } from 'vitest'
import { HTTP_METHOD_TAG, methodTag, type TagType } from '../apiDocDict'

describe('apiDocDict — HTTP method 标色(5 项,前端 UI 约定)', () => {
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
  it('未知 → info fallback', () => {
    expect(methodTag('OPTIONS')).toBe('info')
    expect(methodTag(undefined)).toBe('info')
  })
})
