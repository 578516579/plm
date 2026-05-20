/**
 * Unit tests for @/utils/plm (pure utility functions — no DOM / Vue needed)
 * Run: npm run test:unit
 */
import { describe, it, expect } from 'vitest'
import {
  parseTime,
  selectDictLabel,
  selectDictLabels,
  sprintf,
  parseStrEmpty,
  handleTree,
  tansParams,
  getNormalPath,
  blobValidate
} from './plm'

describe('parseTime', () => {
  it('returns null for falsy input', () => {
    expect(parseTime(null)).toBeNull()
    expect(parseTime(undefined)).toBeNull()
    expect(parseTime('')).toBeNull()
  })

  it('formats a timestamp number', () => {
    // 2024-01-01 00:00:00 UTC+8 = 1704038400000 ms
    const ts = new Date('2024-01-01T00:00:00+08:00').getTime()
    const result = parseTime(ts)
    expect(result).toMatch(/2024-01-01/)
  })

  it('accepts custom pattern', () => {
    const ts = new Date('2024-06-15T00:00:00+08:00').getTime()
    const result = parseTime(ts, '{y}/{m}/{d}')
    expect(result).toMatch(/2024\/06\/15/)
  })
})

describe('selectDictLabel', () => {
  const dicts = [
    { value: '0', label: '男' },
    { value: '1', label: '女' }
  ]

  it('returns label for matching value', () => {
    expect(selectDictLabel(dicts, '0')).toBe('男')
    expect(selectDictLabel(dicts, '1')).toBe('女')
  })

  it('returns value itself when not found', () => {
    expect(selectDictLabel(dicts, '99')).toBe('99')
  })

  it('returns empty string for undefined value', () => {
    expect(selectDictLabel(dicts, undefined)).toBe('')
  })
})

describe('selectDictLabels', () => {
  const dicts = [
    { value: '1', label: '已启用' },
    { value: '2', label: '已停用' }
  ]

  it('returns empty string for undefined', () => {
    expect(selectDictLabels(dicts, undefined)).toBe('')
  })

  it('joins multiple labels with comma', () => {
    const result = selectDictLabels(dicts, '1,2')
    expect(result).toContain('已启用')
    expect(result).toContain('已停用')
  })
})

describe('sprintf', () => {
  it('replaces %s placeholders in order', () => {
    expect(sprintf('hello %s, you are %s', 'world', 'great')).toBe('hello world, you are great')
  })

  it('returns empty string when placeholder has no arg', () => {
    expect(sprintf('hello %s %s', 'world')).toBe('')
  })
})

describe('parseStrEmpty', () => {
  it('converts null/undefined/string-null to empty string', () => {
    expect(parseStrEmpty(null)).toBe('')
    expect(parseStrEmpty(undefined)).toBe('')
    expect(parseStrEmpty('null')).toBe('')
    expect(parseStrEmpty('undefined')).toBe('')
  })

  it('passes through normal strings', () => {
    expect(parseStrEmpty('hello')).toBe('hello')
  })
})

describe('handleTree', () => {
  it('builds a nested tree from flat list', () => {
    const flat = [
      { id: 1, parentId: 0, name: 'root' },
      { id: 2, parentId: 1, name: 'child1' },
      { id: 3, parentId: 1, name: 'child2' }
    ]
    const tree = handleTree(flat, 'id', 'parentId')
    expect(tree).toHaveLength(1)
    expect(tree[0].children).toHaveLength(2)
  })

  it('returns all roots when no parent found', () => {
    const flat = [
      { id: 1, parentId: 0, name: 'A' },
      { id: 2, parentId: 0, name: 'B' }
    ]
    const tree = handleTree(flat)
    expect(tree).toHaveLength(2)
  })
})

describe('tansParams', () => {
  it('serializes flat params to query string', () => {
    const result = tansParams({ a: '1', b: '2' })
    expect(result).toContain('a=1')
    expect(result).toContain('b=2')
  })

  it('skips null and empty values', () => {
    const result = tansParams({ a: null, b: '', c: 'ok' })
    expect(result).not.toContain('a=')
    expect(result).not.toContain('b=')
    expect(result).toContain('c=ok')
  })
})

describe('getNormalPath', () => {
  it('removes trailing slash', () => {
    expect(getNormalPath('/foo/bar/')).toBe('/foo/bar')
  })

  it('collapses double slashes', () => {
    expect(getNormalPath('/foo//bar')).toBe('/foo/bar')
  })

  it('returns empty for empty / falsy input', () => {
    expect(getNormalPath('')).toBe('')
  })
})

describe('blobValidate', () => {
  it('returns false for application/json blob', () => {
    const b = new Blob(['{}'], { type: 'application/json' })
    expect(blobValidate(b)).toBe(false)
  })

  it('returns true for non-json blob', () => {
    const b = new Blob(['data'], { type: 'application/octet-stream' })
    expect(blobValidate(b)).toBe(true)
  })
})
