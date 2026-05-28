import { describe, it, expect } from 'vitest'
import {
  METRIC_TYPE, LEVEL,
  metricLabel, rowLevelLabel, rowLevelTag,
  type TagType
} from '../doraDict'

describe('doraDict — DORA 4 指标 (前端键名,⚠ 与 SQL biz_dora_type 部分错位,见末段 drift)', () => {
  const cases: Array<[string, string]> = [
    ['deploy_frequency',    '📈 部署频率'],
    ['lead_time',           '⏱️ 前置时间'],
    ['mttr',                '🚨 MTTR'],
    ['change_failure_rate', '❌ 变更失败率']
  ]
  it.each(cases)('%s → label「%s」', (code, label) => {
    expect(METRIC_TYPE[code]).toEqual({ label })
    expect(metricLabel(code)).toBe(label)
  })
  it('共 4 指标(锁定当前前端键名集合)', () => {
    expect(Object.keys(METRIC_TYPE)).toEqual(['deploy_frequency', 'lead_time', 'mttr', 'change_failure_rate'])
  })
  it('未知/空 metric → 裸值 + "-" fallback', () => {
    expect(metricLabel('unknown')).toBe('unknown')
    expect(metricLabel(undefined)).toBe('-')
    expect(metricLabel('')).toBe('-')
  })
})

describe('doraDict — DORA 等级 (前端独有,⚠ SQL 无 biz_dora_level 字典,见末段 drift)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['elite',  '🥇 Elite',  'success'],
    ['high',   '🥈 High',   'primary'],
    ['medium', '🥉 Medium', 'warning'],
    ['low',    '⚠️ Low',    'danger']
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(LEVEL[code]).toEqual({ label, tag })
    expect(rowLevelLabel(code)).toBe(label)
    expect(rowLevelTag(code)).toBe(tag)
  })
  it('共 4 等级 (Elite/High/Medium/Low,DORA 行业标准 4 档)', () => {
    expect(Object.keys(LEVEL)).toEqual(['elite', 'high', 'medium', 'low'])
  })
  it('未知/空 level → "-" + info fallback', () => {
    expect(rowLevelLabel('unknown')).toBe('-')
    expect(rowLevelLabel(undefined)).toBe('-')
    expect(rowLevelTag('unknown')).toBe('info')
    expect(rowLevelTag(undefined)).toBe('info')
  })
})

describe('⚠ 已知契约漂移(锁定当前前端约定,api-contract/UED 评审走 spawn 任务卡)', () => {
  it('drift §1 metric_type 码错位 (2/4):前端「deploy_frequency / change_failure_rate」,SQL「deploy_freq / change_fail_rate」', () => {
    expect('deploy_frequency' in METRIC_TYPE).toBe(true)
    expect('change_failure_rate' in METRIC_TYPE).toBe(true)
    expect('deploy_freq' in METRIC_TYPE).toBe(false)      // SQL 真值不在前端
    expect('change_fail_rate' in METRIC_TYPE).toBe(false) // SQL 真值不在前端
    // SQL 真值: 见 plm-backend/sql/business-dora.sql:46,49
    // 修复决策:改前端键 → 同步 api/business/dora.ts DoraMetric.metricType + 后端
    //          Domain/Mapper + E2E + 存量数据迁移
  })
  it('drift §2 metric_type label 含 emoji 前缀,SQL 为纯文本', () => {
    expect(METRIC_TYPE.deploy_frequency.label).toMatch(/^📈 /)
    expect(METRIC_TYPE.lead_time.label).toMatch(/^⏱️ /)
    expect(METRIC_TYPE.mttr.label).toBe('🚨 MTTR')  // 前端缩写 + emoji;SQL「平均恢复时间」
    expect(METRIC_TYPE.change_failure_rate.label).toMatch(/^❌ /)
    // SQL 真值: dict_label='部署频率'/'前置时间'/'平均恢复时间'/'变更失败率' (无 emoji)
  })
  it('drift §3 LEVEL 等级字典属前端独有 — SQL 无 biz_dora_level,tb_dora_metric 也无 level 列', () => {
    expect(Object.keys(LEVEL).length).toBe(4)
    // 提醒:level 字段在 frontend form.level 但 SQL DDL 无 level 列;
    // 可能存于 Domain 拓展属性 / 计算字段 / 前端态。需 api-contract 厘清。
  })
  it('drift §4 + §5: SQL biz_dora_status (3) / biz_dora_period (2) 当前 doraDict 未承载 (index.vue 也未渲染)', () => {
    // 仅作锚点:本字典不覆盖 status/period,如未来 index.vue 需展示,本文件补 STATUS/PERIOD 时同步对齐 SQL
    expect(true).toBe(true)
  })
})
