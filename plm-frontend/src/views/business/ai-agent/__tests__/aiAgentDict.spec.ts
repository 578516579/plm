import { describe, it, expect } from 'vitest'
import {
  AGENT_TYPE, AI_PROVIDER, AGENT_STATUS,
  agentIcon, agentTypeLabel, agentTypeTag,
  providerLabel, providerTag,
  agentStatusLabel, agentStatusTag,
  type TagType
} from '../aiAgentDict'

describe('aiAgentDict — Agent 类型 biz_aiagent_type (6 类,对齐 business-ai-agent.sql)', () => {
  const cases: Array<[string, string, TagType, string]> = [
    ['requirement', '需求分析', 'primary', '📋'],
    ['prd',         'PRD 生成', 'success', '📝'],
    ['code',        '代码审查', 'warning', '🔍'],
    ['test',        '测试生成', 'info',    '🧪'],
    ['release',     '发布评审', 'danger',  '🚀'],
    ['ops',         '运维巡检', 'primary', '🛠️']
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」/ icon「%s」', (code, label, tag, icon) => {
    expect(AGENT_TYPE[code]).toEqual({ label, tag, icon })
    expect(agentTypeLabel(code)).toBe(label)
    expect(agentTypeTag(code)).toBe(tag)
    expect(agentIcon(code)).toBe(icon)
  })
  it('共 6 类,与 SQL value 完整对齐', () => {
    expect(Object.keys(AGENT_TYPE)).toEqual(['requirement', 'prd', 'code', 'test', 'release', 'ops'])
  })
  it('未知/空 type → 裸值 + info tag + 🤖 icon (fallback)', () => {
    expect(agentTypeLabel('unknown')).toBe('unknown')
    expect(agentTypeLabel(undefined)).toBe('-')
    expect(agentTypeLabel('')).toBe('-')
    expect(agentTypeTag('unknown')).toBe('info')
    expect(agentTypeTag(undefined)).toBe('info')
    expect(agentIcon('unknown')).toBe('🤖')
    expect(agentIcon(undefined)).toBe('🤖')
  })
})

describe('aiAgentDict — AI Provider biz_ai_provider (4 种)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['mock',      'Mock',         'info'],     // ⚠ SQL label「Mock 占位」, 见末段 drift
    ['dify',      'Dify',         'primary'],  // ⚠ SQL label「Dify 编排」, 见末段 drift
    ['openai',    'OpenAI 兼容',  'success'],
    ['anthropic', 'Anthropic',    'warning']
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(AI_PROVIDER[code]).toEqual({ label, tag })
    expect(providerLabel(code)).toBe(label)
    expect(providerTag(code)).toBe(tag)
  })
  it('共 4 种,与 SQL value 完整对齐', () => {
    expect(Object.keys(AI_PROVIDER)).toEqual(['mock', 'dify', 'openai', 'anthropic'])
  })
  it('providerLabel 历史宽签名: number 输入返回裸值 (现实中始终是 string)', () => {
    expect(providerLabel(123 as any)).toBe('123')
  })
  it('未知/空 provider → 裸值 + info fallback', () => {
    expect(providerLabel('xxx')).toBe('xxx')
    expect(providerLabel(undefined)).toBe('-')
    expect(providerLabel('')).toBe('-')
    expect(providerTag('xxx')).toBe('info')
    expect(providerTag(undefined)).toBe('info')
  })
})

describe('aiAgentDict — Agent 状态 biz_aiagent_status (3 态,对齐 SQL value+label+tag 完整)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['00', '运行中', 'success'],
    ['01', '已停止', 'info'],
    ['02', '错误',   'danger']
  ]
  it.each(cases)('状态 %s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(AGENT_STATUS[code]).toEqual({ label, tag })
    expect(agentStatusLabel(code)).toBe(label)
    expect(agentStatusTag(code)).toBe(tag)
  })
  it('共 3 码,与 SQL 完整对齐(无多无漏)', () => {
    expect(Object.keys(AGENT_STATUS)).toEqual(['00', '01', '02'])
  })
  it('未知/空 状态 → 裸值 + info fallback', () => {
    expect(agentStatusLabel('99')).toBe('99')
    expect(agentStatusLabel(undefined)).toBe('-')
    expect(agentStatusTag('99')).toBe('info')
    expect(agentStatusTag(undefined)).toBe('info')
  })
})

describe('⚠ 已知显示层小漂移(provider label 简化,锁定当前前端约定,UED 评审走 spawn 任务卡)', () => {
  it('provider mock: 前端 label「Mock」, SQL biz_ai_provider「Mock 占位」(简化)', () => {
    expect(AI_PROVIDER.mock.label).toBe('Mock')
    // SQL 真值: dict_label='Mock 占位' — 见 plm-backend/sql/business-ai-agent.sql:51
    // 若 UED 评审决定对齐 SQL,改本断言为 'Mock 占位' 并复查所有 providerLabel 调用位
  })
  it('provider dify: 前端 label「Dify」, SQL biz_ai_provider「Dify 编排」(简化)', () => {
    expect(AI_PROVIDER.dify.label).toBe('Dify')
    // SQL 真值: dict_label='Dify 编排' — 见 plm-backend/sql/business-ai-agent.sql:52
  })
})
