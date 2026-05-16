/** AI Agent 编排模块 E2E — PRD §F3.5 + 原型 aiagents.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext

test.describe('AI Agent 模块 E2E (PRD §F3.5)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
  })

  test.afterAll(async () => {
    execDelete('tb_ai_agent', `agent_name like '%${RUN_ID}%'`)
    await apiRequest?.dispose()
  })

  test('TC-AIAGENT-F001 创建 PRD Agent', async () => {
    const r = await api.post('/business/ai-agent', {
      agentName: `PRD-Agent-${RUN_ID}`,
      agentType: 'prd',
      description: 'AI 一键生成 PRD',
      promptTemplate: '你是 AgriPLM 产品经理...',
      difyWorkflowId: 'wf-prd-001',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-AIAGENT-F002 创建运维巡检 Agent + invoke 累计', async () => {
    const r = await api.post('/business/ai-agent', {
      agentName: `Ops-Agent-${RUN_ID}`,
      agentType: 'ops',
      description: 'IoT 设备巡检',
      authorUserId: 1
    })
    expect(r.code).toBe(200)

    const list = await api.get('/business/ai-agent/list', { agentName: `Ops-Agent-${RUN_ID}` })
    const ag = list.rows[0]
    const inv1 = await api.post(`/business/ai-agent/invoke/${ag.agentId}`, {})
    expect(inv1.code).toBe(200)
    expect(inv1.data.totalCalls).toBe(1)

    const inv2 = await api.post(`/business/ai-agent/invoke/${ag.agentId}`, {})
    expect(inv2.data.totalCalls).toBe(2)
    expect(Number(inv2.data.successRate)).toBeGreaterThan(0)
  })

  test('TC-AIAGENT-F003 无效 type 触发 604', async () => {
    const r = await api.post('/business/ai-agent', {
      agentName: `Bad-${RUN_ID}`,
      agentType: 'invalid_type',
      authorUserId: 1
    })
    expect(r.code).toBe(604)
  })
})
