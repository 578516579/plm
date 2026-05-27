/**
 * @file validate.spec.ts
 * @description 校验工具函数单元测试 — src/utils/validate.ts
 *
 * 覆盖 12 个纯函数（无 DOM / 无依赖,RuoYi 沿用 + 路径匹配）:
 *   isPathMatch / isEmpty / isHttp / isExternal / validUsername / validURL /
 *   validLowerCase / validUpperCase / validAlphabets / validEmail / isString / isArray
 */
import { describe, it, expect } from 'vitest'
import {
  isPathMatch,
  isEmpty,
  isHttp,
  isExternal,
  validUsername,
  validURL,
  validLowerCase,
  validUpperCase,
  validAlphabets,
  validEmail,
  isString,
  isArray,
} from '../validate'

// ───────────────────────────────────────────────────────────────────────────
describe('isPathMatch', () => {
  it('精确匹配', () => {
    expect(isPathMatch('/system/user', '/system/user')).toBe(true)
    expect(isPathMatch('/system/user', '/system/role')).toBe(false)
  })

  it('单星号 * 不跨 / 段', () => {
    expect(isPathMatch('/system/*', '/system/user')).toBe(true)
    expect(isPathMatch('/system/*', '/system/user/1')).toBe(false)
  })

  it('双星号 ** 跨多段', () => {
    expect(isPathMatch('/system/**', '/system/user/1/edit')).toBe(true)
    expect(isPathMatch('/system/**', '/system/')).toBe(true)
  })

  it('问号 ? 匹配单个非 / 字符', () => {
    expect(isPathMatch('/user/?', '/user/1')).toBe(true)
    expect(isPathMatch('/user/?', '/user/12')).toBe(false)
  })

  it('正则特殊字符按字面量处理', () => {
    expect(isPathMatch('/a.b/c', '/a.b/c')).toBe(true)
    expect(isPathMatch('/a.b/c', '/axb/c')).toBe(false)
  })
})

// ───────────────────────────────────────────────────────────────────────────
describe('isEmpty', () => {
  it('null / undefined / 空串 / "undefined" 字面量 → true', () => {
    expect(isEmpty(null)).toBe(true)
    expect(isEmpty(undefined)).toBe(true)
    expect(isEmpty('')).toBe(true)
    expect(isEmpty('undefined')).toBe(true)
  })

  it('非空字符串 → false', () => {
    expect(isEmpty('x')).toBe(false)
    expect(isEmpty('null')).toBe(false) // 注意:仅 "undefined" 字面量被判空,"null" 不是
    expect(isEmpty('0')).toBe(false)
  })

  it('数字 0 因 0 == "" 松散相等被判空（RuoYi 历史 == 行为,刻意固化）', () => {
    expect(isEmpty(0)).toBe(true)
  })
})

// ───────────────────────────────────────────────────────────────────────────
describe('isHttp', () => {
  it('含 http:// 或 https:// → true', () => {
    expect(isHttp('http://x.com')).toBe(true)
    expect(isHttp('https://x.com')).toBe(true)
  })

  it('无协议前缀 → false', () => {
    expect(isHttp('/local/path')).toBe(false)
    expect(isHttp('ftp://x.com')).toBe(false)
  })
})

// ───────────────────────────────────────────────────────────────────────────
describe('isExternal', () => {
  it('http(s) / mailto / tel → true', () => {
    expect(isExternal('https://x.com')).toBe(true)
    expect(isExternal('http://x.com')).toBe(true)
    expect(isExternal('mailto:a@b.com')).toBe(true)
    expect(isExternal('tel:10086')).toBe(true)
  })

  it('内部相对路径 → false', () => {
    expect(isExternal('/business/project')).toBe(false)
    expect(isExternal('dashboard')).toBe(false)
  })
})

// ───────────────────────────────────────────────────────────────────────────
describe('validUsername', () => {
  it('admin / editor → true（含首尾空格 trim）', () => {
    expect(validUsername('admin')).toBe(true)
    expect(validUsername('editor')).toBe(true)
    expect(validUsername('  admin  ')).toBe(true)
  })

  it('其它用户名 → false', () => {
    expect(validUsername('guest')).toBe(false)
    expect(validUsername('')).toBe(false)
  })
})

// ───────────────────────────────────────────────────────────────────────────
describe('validURL', () => {
  it('合法 http/https/ftp URL → true', () => {
    expect(validURL('https://www.example.com')).toBe(true)
    expect(validURL('http://example.org/path?q=1')).toBe(true)
    expect(validURL('ftp://files.example.net')).toBe(true)
  })

  it('非法 URL → false', () => {
    expect(validURL('just-text')).toBe(false)
    expect(validURL('htp://bad')).toBe(false)
  })
})

// ───────────────────────────────────────────────────────────────────────────
describe('validLowerCase / validUpperCase / validAlphabets', () => {
  it('validLowerCase 仅全小写字母 → true', () => {
    expect(validLowerCase('abc')).toBe(true)
    expect(validLowerCase('aBc')).toBe(false)
    expect(validLowerCase('ab1')).toBe(false)
  })

  it('validUpperCase 仅全大写字母 → true', () => {
    expect(validUpperCase('ABC')).toBe(true)
    expect(validUpperCase('AbC')).toBe(false)
  })

  it('validAlphabets 仅大小写字母 → true', () => {
    expect(validAlphabets('AbCdEf')).toBe(true)
    expect(validAlphabets('Abc1')).toBe(false)
  })
})

// ───────────────────────────────────────────────────────────────────────────
describe('validEmail', () => {
  it('合法邮箱 → true', () => {
    expect(validEmail('admin@example.com')).toBe(true)
    expect(validEmail('a.b-c@sub.example.org')).toBe(true)
  })

  it('非法邮箱 → false', () => {
    expect(validEmail('bad-email')).toBe(false)
    expect(validEmail('a@b')).toBe(false)
    expect(validEmail('@example.com')).toBe(false)
  })
})

// ───────────────────────────────────────────────────────────────────────────
describe('isString', () => {
  it('字符串字面量与 String 对象 → true', () => {
    expect(isString('x')).toBe(true)
    // eslint-disable-next-line no-new-wrappers
    expect(isString(new String('x'))).toBe(true)
  })

  it('非字符串 → false', () => {
    expect(isString(123)).toBe(false)
    expect(isString(null)).toBe(false)
    expect(isString(['x'])).toBe(false)
  })
})

// ───────────────────────────────────────────────────────────────────────────
describe('isArray', () => {
  it('数组 → true', () => {
    expect(isArray([])).toBe(true)
    expect(isArray([1, 2])).toBe(true)
  })

  it('非数组 → false', () => {
    expect(isArray('x')).toBe(false)
    expect(isArray({ length: 0 })).toBe(false)
    expect(isArray(null)).toBe(false)
  })
})
