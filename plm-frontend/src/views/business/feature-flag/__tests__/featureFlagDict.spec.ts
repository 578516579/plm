import { describe, it, expect } from 'vitest'
import {
  FF_ENV, FF_MODE,
  envLabel, envTag, modeLabel, modeTag,
  type TagType
} from '../featureFlagDict'

describe('featureFlagDict — 环境 (⚠ 前端 dev/SQL test,见末段 drift §1-§3)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['dev',     '开发', 'info'],     // ⚠ SQL value='test', label='TEST', tag='primary'
    ['staging', '预发', 'warning'],  // ⚠ SQL label='STAGING'
    ['prod',    '生产', 'danger']    // ⚠ SQL label='PROD'
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(FF_ENV[code]).toEqual({ label, tag })
    expect(envLabel(code)).toBe(label)
    expect(envTag(code)).toBe(tag)
  })
  it('共 3 项(锁前端 dev/staging/prod,⚠ SQL 用 test/staging/prod)', () => {
    expect(Object.keys(FF_ENV)).toEqual(['dev', 'staging', 'prod'])
  })
  it('未知/空 → 裸值 + info', () => {
    expect(envLabel('uat')).toBe('uat')
    expect(envLabel(undefined)).toBe('-')
    expect(envTag('uat')).toBe('info')
  })
})

describe('featureFlagDict — 灰度模式 (⚠ tag/label 部分漂移,见末段 drift §4-§5)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['all_on',  '全量', 'success'],   // SQL label='全量开启 (100%)' (前端简化)
    ['all_off', '关闭', 'danger'],    // ⚠ SQL list_class='info'
    ['canary',  '灰度', 'warning']    // SQL label='灰度 (1-99%)' (前端简化)
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(FF_MODE[code]).toEqual({ label, tag })
    expect(modeLabel(code)).toBe(label)
    expect(modeTag(code)).toBe(tag)
  })
  it('共 3 项(values 与 SQL biz_ff_strategy 一致)', () => {
    expect(Object.keys(FF_MODE)).toEqual(['all_on', 'all_off', 'canary'])
  })
  it('未知/空 → 裸值 + info', () => {
    expect(modeLabel('experiment')).toBe('experiment')
    expect(modeLabel(undefined)).toBe('-')
    expect(modeTag('experiment')).toBe('info')
  })
})

describe('⚠ 已知契约漂移(api-contract/UED 评审走 spawn 任务卡)', () => {
  it('drift §1 env value: 前端 dev vs SQL test', () => {
    expect('dev' in FF_ENV).toBe(true)
    expect('test' in FF_ENV).toBe(false)
    // SQL value: 'test'/'staging'/'prod' — 见 plm-backend/sql/business-feature-flag.sql:40-42
  })
  it('drift §2 env label 风格:前端中文 vs SQL 英文大写', () => {
    expect(FF_ENV.dev.label).toBe('开发')       // SQL 'TEST' (英文大写)
    expect(FF_ENV.staging.label).toBe('预发')   // SQL 'STAGING'
    expect(FF_ENV.prod.label).toBe('生产')      // SQL 'PROD'
  })
  it('drift §3 env tag dev/test:前端 info, SQL primary', () => {
    expect(FF_ENV.dev.tag).toBe('info')         // SQL list_class='primary'
  })
  it('drift §4 mode all_off tag:前端 danger (强调危险),SQL info', () => {
    expect(FF_MODE.all_off.tag).toBe('danger')  // SQL list_class='info'
  })
  it('drift §5 mode label 简化:前端「全量/关闭/灰度」,SQL 含百分比注释', () => {
    expect(FF_MODE.all_on.label).toBe('全量')    // SQL '全量开启 (100%)'
    expect(FF_MODE.all_off.label).toBe('关闭')   // SQL '关闭 (0%)'
    expect(FF_MODE.canary.label).toBe('灰度')    // SQL '灰度 (1-99%)'
  })
  it('drift §6 biz_ff_status (00 开启/01 关闭) SQL 定义但本字典未承载 (index.vue 用 rolloutMode 渲染)', () => {
    // 仅锚点:本字典只覆盖 env+mode,如未来 index.vue 需展示 biz_ff_status 补 FF_STATUS
    expect(true).toBe(true)
  })
})
