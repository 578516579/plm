import { describe, it, expect } from 'vitest'
import {
  CI_TOOL, TRIGGER_TYPE, LAST_RUN_STATUS,
  ciToolLabel, triggerLabel, lastRunLabel, lastRunTag,
  type TagType
} from '../pipelineDict'

describe('pipelineDict — CI 工具(前端键名,⚠ 与 SQL biz_pipeline_tool 3/4 错位,见末段 drift)', () => {
  const cases: Array<[string, string]> = [
    ['jenkins',        'Jenkins'],
    ['gitlab_ci',      'GitLab'],
    ['github_actions', 'GHA'],
    ['drone',          'Drone']
  ]
  it.each(cases)('%s → label「%s」', (code, label) => {
    expect(CI_TOOL[code]).toEqual({ label })
    expect(ciToolLabel(code)).toBe(label)
  })
  it('共 4 工具(锁定前端键名集合)', () => {
    expect(Object.keys(CI_TOOL)).toEqual(['jenkins', 'gitlab_ci', 'github_actions', 'drone'])
  })
  it('未知/空 → 裸值 / "-"', () => {
    expect(ciToolLabel('travis')).toBe('travis')
    expect(ciToolLabel(undefined)).toBe('-')
  })
})

describe('pipelineDict — 触发方式(前端 5 项 vs SQL 4 项,见末段 drift §3)', () => {
  const cases: Array<[string, string]> = [
    ['push',   'Push'],
    ['pr',     'PR'],
    ['tag',    'Tag'],
    ['cron',   '定时'],
    ['manual', '手动']
  ]
  it.each(cases)('%s → label「%s」', (code, label) => {
    expect(TRIGGER_TYPE[code]).toEqual({ label })
    expect(triggerLabel(code)).toBe(label)
  })
  it('共 5 项(锁定前端约定;SQL 仅 4 项无 pr)', () => {
    expect(Object.keys(TRIGGER_TYPE)).toEqual(['push', 'pr', 'tag', 'cron', 'manual'])
  })
  it('未知/空 → 裸值 / "-"', () => {
    expect(triggerLabel('webhook')).toBe('webhook')
    expect(triggerLabel(undefined)).toBe('-')
  })
})

describe('pipelineDict — 上次执行结果(前端约定,⚠ 与 SQL biz_pipeline_result 不一致,见末段 drift §4+§5)', () => {
  const cases: Array<[string, string, TagType]> = [
    ['success', '成功',   'success'],
    ['running', '运行中', 'primary'],   // ⚠ SQL list_class='warning'
    ['failed',  '失败',   'danger'],
    ['never',   '未执行', 'info']       // ⚠ SQL 无 'never'
  ]
  it.each(cases)('%s → label「%s」/ tag「%s」', (code, label, tag) => {
    expect(LAST_RUN_STATUS[code]).toEqual({ label, tag })
    expect(lastRunLabel(code)).toBe(label)
    expect(lastRunTag(code)).toBe(tag)
  })
  it('共 4 项(前端约定;SQL 4 项但 skipped↔never 不同)', () => {
    expect(Object.keys(LAST_RUN_STATUS)).toEqual(['success', 'running', 'failed', 'never'])
  })
  it('回归锁: lastRunLabel 未知/空 → "-" (相比旧 v||\'-\' 改为统一 "-")', () => {
    expect(lastRunLabel('unknown')).toBe('-')
    expect(lastRunLabel(undefined)).toBe('-')
    expect(lastRunTag('unknown')).toBe('info')
  })
})

describe('⚠ 已知契约漂移(锁定当前前端约定,api-contract/UED 评审走 spawn 任务卡)', () => {
  it('drift §1 ciTool values 错位 3/4:前端 gitlab_ci/github_actions/drone, SQL gitlab/github/gitea', () => {
    expect('gitlab_ci' in CI_TOOL).toBe(true)
    expect('github_actions' in CI_TOOL).toBe(true)
    expect('drone' in CI_TOOL).toBe(true)
    expect('gitlab' in CI_TOOL).toBe(false)
    expect('github' in CI_TOOL).toBe(false)
    expect('gitea' in CI_TOOL).toBe(false)
    // SQL 真值: 见 plm-backend/sql/business-pipeline.sql:46-49
  })
  it('drift §3 triggerType: 前端含 pr (5 项), SQL 仅 4 项无 pr', () => {
    expect('pr' in TRIGGER_TYPE).toBe(true)
    expect(Object.keys(TRIGGER_TYPE).length).toBe(5)
    // SQL 真值: dict_value=manual/push/cron/tag — 见 plm-backend/sql/business-pipeline.sql:51-54
  })
  it('drift §4 lastRunStatus: 前端含 never SQL 含 skipped (互斥)', () => {
    expect('never' in LAST_RUN_STATUS).toBe(true)
    expect('skipped' in LAST_RUN_STATUS).toBe(false)
    // SQL 真值: dict_value=success/failed/running/skipped — 见 plm-backend/sql/business-pipeline.sql:56-59
  })
  it('drift §5 lastRunTag running: 前端 primary, SQL list_class warning', () => {
    expect(LAST_RUN_STATUS.running.tag).toBe('primary')
    // SQL 真值: list_class='warning' — 见 plm-backend/sql/business-pipeline.sql:58
  })
  it('drift §6 biz_pipeline_status (启用/停用) SQL 定义但前端 index.vue 未渲染 (本字典亦未承载)', () => {
    // 仅锚点:本字典不承载 biz_pipeline_status;若未来需展示「启用/停用」补 STATUS 节
    expect(true).toBe(true)
  })
})
