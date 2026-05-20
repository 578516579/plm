/**
 * @file plm.spec.ts
 * @description 工具函数单元测试 — src/utils/plm.ts
 *
 * 覆盖:
 *   selectDictLabel   — 字典 label 选取（正常 / 未命中 / 边界）
 *   selectDictLabels  — 多值字典 label 选取
 *   parseTime         — 日期格式化
 *   parseStrEmpty     — undefined/null 转空串
 *   tansParams        — 对象序列化成 query string
 *   handleTree        — 扁平数组 → 树形结构
 *   getNormalPath     — 路径规范化
 *   sprintf           — %s 字符串格式化
 */

import { describe, it, expect } from 'vitest'
import {
  selectDictLabel,
  selectDictLabels,
  parseTime,
  parseStrEmpty,
  tansParams,
  handleTree,
  getNormalPath,
  sprintf,
} from '../plm'

// ─────────────────────────────────────────────────────────────────────────────
// selectDictLabel
// ─────────────────────────────────────────────────────────────────────────────
describe('selectDictLabel', () => {
  const datas = [
    { label: '启用', value: '0' },
    { label: '停用', value: '1' },
  ]

  it('根据 value 返回对应 label', () => {
    expect(selectDictLabel(datas, '0')).toBe('启用')
    expect(selectDictLabel(datas, '1')).toBe('停用')
  })

  it('数字 value 也能匹配字符串字典项（== 比较）', () => {
    expect(selectDictLabel(datas, 0)).toBe('启用')
    expect(selectDictLabel(datas, 1)).toBe('停用')
  })

  it('未命中时原样返回 value', () => {
    // 实现：未匹配则 push(value) 本身
    expect(selectDictLabel(datas, '99')).toBe('99')
  })

  it('value 为 undefined 返回空串', () => {
    expect(selectDictLabel(datas, undefined)).toBe('')
  })

  it('空字典数组时原样返回 value', () => {
    expect(selectDictLabel([], '0')).toBe('0')
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// selectDictLabels (多值)
// ─────────────────────────────────────────────────────────────────────────────
describe('selectDictLabels', () => {
  const datas = [
    { label: '读', value: '1' },
    { label: '写', value: '2' },
    { label: '执行', value: '3' },
  ]

  it('逗号分隔字符串 — 多值命中', () => {
    expect(selectDictLabels(datas, '1,2')).toBe('读,写')
  })

  it('数组形式输入', () => {
    expect(selectDictLabels(datas, ['1', '3'])).toBe('读,执行')
  })

  it('value 为 undefined 返回空串', () => {
    expect(selectDictLabels(datas, undefined)).toBe('')
  })

  it('value 为空字符串返回空串', () => {
    expect(selectDictLabels(datas, '')).toBe('')
  })

  it('自定义 separator', () => {
    expect(selectDictLabels(datas, '1|2', '|')).toBe('读|写')
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// parseTime
// ─────────────────────────────────────────────────────────────────────────────
describe('parseTime', () => {
  it('null 或无参数返回 null', () => {
    expect(parseTime(null)).toBeNull()
    expect(parseTime('')).toBeNull()
  })

  it('Date 对象格式化', () => {
    // 使用固定时间避免时区问题
    const d = new Date(2024, 0, 5, 8, 3, 7) // 2024-01-05 08:03:07
    expect(parseTime(d)).toBe('2024-01-05 08:03:07')
  })

  it('自定义 pattern', () => {
    const d = new Date(2024, 5, 20, 0, 0, 0)
    expect(parseTime(d, '{y}/{m}/{d}')).toBe('2024/06/20')
  })

  it('10 位 Unix 时间戳（秒）转换', () => {
    // 1704067200 = 2024-01-01 00:00:00 UTC
    // 本地时区可能不同，只验证能返回字符串且格式正确
    const result = parseTime(1704067200)
    expect(result).toMatch(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/)
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// parseStrEmpty
// ─────────────────────────────────────────────────────────────────────────────
describe('parseStrEmpty', () => {
  it('正常字符串原样返回', () => {
    expect(parseStrEmpty('hello')).toBe('hello')
  })

  it('null 返回空串', () => {
    expect(parseStrEmpty(null)).toBe('')
  })

  it('undefined 返回空串', () => {
    expect(parseStrEmpty(undefined)).toBe('')
  })

  it('"undefined" 字符串返回空串', () => {
    expect(parseStrEmpty('undefined')).toBe('')
  })

  it('"null" 字符串返回空串', () => {
    expect(parseStrEmpty('null')).toBe('')
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// tansParams
// ─────────────────────────────────────────────────────────────────────────────
describe('tansParams', () => {
  it('简单 key=value 对象序列化', () => {
    const result = tansParams({ name: '张三', age: 18 })
    expect(result).toContain('name=%E5%BC%A0%E4%B8%89')
    expect(result).toContain('age=18')
  })

  it('值为 null / 空串 / undefined 时跳过', () => {
    const result = tansParams({ a: 'ok', b: null, c: '', d: undefined })
    expect(result).toContain('a=ok')
    expect(result).not.toContain('b=')
    expect(result).not.toContain('c=')
    expect(result).not.toContain('d=')
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// handleTree
// ─────────────────────────────────────────────────────────────────────────────
describe('handleTree', () => {
  const flat = [
    { id: 1, parentId: 0, name: 'root' },
    { id: 2, parentId: 1, name: 'child1' },
    { id: 3, parentId: 1, name: 'child2' },
    { id: 4, parentId: 2, name: 'grandchild' },
  ]

  it('生成正确树形结构', () => {
    const tree = handleTree(flat)
    expect(tree).toHaveLength(1)
    expect(tree[0].name).toBe('root')
    expect(tree[0].children).toHaveLength(2)
    expect(tree[0].children[0].children).toHaveLength(1)
  })

  it('空数组返回空数组', () => {
    expect(handleTree([])).toEqual([])
  })

  it('自定义 id / parentId / children 字段名', () => {
    const data = [
      { key: 'a', pid: '', label: 'A' },
      { key: 'b', pid: 'a', label: 'B' },
    ]
    const tree = handleTree(data, 'key', 'pid', 'items')
    expect(tree).toHaveLength(1)
    expect(tree[0].items).toHaveLength(1)
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// getNormalPath
// ─────────────────────────────────────────────────────────────────────────────
describe('getNormalPath', () => {
  it('合并双斜杠', () => {
    expect(getNormalPath('/foo//bar')).toBe('/foo/bar')
  })

  it('去掉末尾斜杠', () => {
    expect(getNormalPath('/foo/bar/')).toBe('/foo/bar')
  })

  it('正常路径原样返回', () => {
    expect(getNormalPath('/foo/bar')).toBe('/foo/bar')
  })

  it('空字符串原样返回', () => {
    expect(getNormalPath('')).toBe('')
  })
})

// ─────────────────────────────────────────────────────────────────────────────
// sprintf — i 从 0 开始（标准实现,已修 RuoYi upstream off-by-one bug)
// ─────────────────────────────────────────────────────────────────────────────
describe('sprintf', () => {
  it('单个 %s + 1 个 arg → 正确替换', () => {
    expect(sprintf('Hello %s!', 'World')).toBe('Hello World!')
  })

  it('多个 %s + 多个 args → 顺序替换', () => {
    expect(sprintf('%s + %s = %s', 1, 2, 3)).toBe('1 + 2 = 3')
  })

  it('无 %s 时原样返回字符串', () => {
    expect(sprintf('no placeholder')).toBe('no placeholder')
  })

  it('占位符数量超过 args 时返回空串 (flag=false 短路)', () => {
    // 2 个 %s,只有 args[0]='a',args[1]=undefined → flag=false → ''
    expect(sprintf('%s %s', 'a')).toBe('')
  })
})
