/** AI Agent 编排模块 E2E — PRD §F3.5 + 原型 aiagents.html (10 case) */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, getFieldHex } from './helpers/db'
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

  const create = (suffix: string, extra: any = {}) =>
    api.post('/business/ai-agent', { agentName: `${suffix}-${RUN_ID}`, agentType: 'requirement', provider: 'mock', authorUserId: 1, ...extra })
  const idByName = async (suffix: string) => {
    const list = await api.get('/business/ai-agent/list', { agentName: `${suffix}-${RUN_ID}` })
    return list.rows[0]?.agentId
  }

  test('TC-AIAGENT-F001 创建 PRD Agent', async () => {
    const r = await create('PRD-Agent', {
      agentType: 'prd', description: 'AI 一键生成 PRD', promptTemplate: '你是 AgriPLM 产品经理...', difyWorkflowId: 'wf-prd-001'
    })
    expect(r.code).toBe(200)
  })

  test('TC-AIAGENT-F002 invoke 累计成功率 (mock provider)', async () => {
    expect((await create('Ops-Agent', { agentType: 'ops', description: 'IoT 设备巡检' })).code).toBe(200)
    const id = await idByName('Ops-Agent')
    const inv1 = await api.post(`/business/ai-agent/invoke/${id}`, {})
    expect(inv1.code).toBe(200)
    expect(inv1.data.totalCalls).toBe(1)
    const inv2 = await api.post(`/business/ai-agent/invoke/${id}`, {})
    expect(inv2.data.totalCalls).toBe(2)
    expect(Number(inv2.data.successRate)).toBeGreaterThan(0)
  })

  test('TC-AIAGENT-F003 agentType 白名单非法 → 604', async () => {
    const r = await create('Bad-Type', { agentType: 'invalid_type' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/无效的 Agent 类型/)
  })

  test('TC-AIAGENT-F004 agentName 必填 → 602', async () => {
    const r = await api.post('/business/ai-agent', { agentType: 'requirement', authorUserId: 1 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/Agent 名称/)
  })

  test('TC-AIAGENT-F005 provider 白名单非法 → 604', async () => {
    const r = await create('Bad-Provider', { provider: 'gemini' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/无效的 provider/)
  })

  test('TC-AIAGENT-F006 状态机 00→01 (运行中→已停止) + 00→02 (→错误)', async () => {
    expect((await create('状态机-停止')).code).toBe(200)
    const id1 = await idByName('状态机-停止')
    expect((await api.put('/business/ai-agent', { agentId: id1, status: '01' })).code).toBe(200)

    expect((await create('状态机-错误')).code).toBe(200)
    const id2 = await idByName('状态机-错误')
    expect((await api.put('/business/ai-agent', { agentId: id2, status: '02' })).code).toBe(200)
  })

  test('TC-AIAGENT-F007 状态机 01→02 非法 (已停止不可直接到错误) → 601', async () => {
    expect((await create('状态机-非法')).code).toBe(200)
    const id = await idByName('状态机-非法')
    await api.put('/business/ai-agent', { agentId: id, status: '01' })
    const r = await api.put('/business/ai-agent', { agentId: id, status: '02' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/转到/)
  })

  test('TC-AIAGENT-F008 invoke 非运行中 (status=01) → 601', async () => {
    expect((await create('停用-invoke')).code).toBe(200)
    const id = await idByName('停用-invoke')
    await api.put('/business/ai-agent', { agentId: id, status: '01' })
    const r = await api.post(`/business/ai-agent/invoke/${id}`, {})
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不可调用/)
  })

  test('TC-AIAGENT-F009 编号格式 AGT-YYYY-NNNN', async () => {
    expect((await create('编号格式')).code).toBe(200)
    const list = await api.get('/business/ai-agent/list', { agentName: `编号格式-${RUN_ID}` })
    const year = new Date().getFullYear()
    expect(list.rows[0].agentNo).toMatch(new RegExp(`^AGT-${year}-\\d{4}$`))
  })

  test('TC-AIAGENT-ENC001 编码 HEX — 中文 agent_name 不含 EFBFBD', async () => {
    const cn = '农情智能体-中文检测'
    expect((await create(cn)).code).toBe(200)
    const hex = getFieldHex('tb_ai_agent', 'agent_name', `agent_name like '${cn}-${RUN_ID}%'`)
    expect(hex.toUpperCase()).not.toContain('EFBFBD')
  })

  test('TC-AIAGENT-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const createResp = await create('删除测试')
    expect(createResp.code, '创建应成功').toBe(200)

    const id = await idByName('删除测试')
    expect(id, '新建 ai-agent 应能在列表里查到').toBeDefined()
    expect(typeof id, 'agentId 应是 number').toBe('number')

    const delResp = await api.delete(`/business/ai-agent/${id}`)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.get('/business/ai-agent/list', { agentName: `删除测试-${RUN_ID}` })
    const stillThere = after.rows.find((r: any) => r.agentId === id)
    expect(stillThere, `agentId=${id} 删除后不该出现在 list`).toBeUndefined()
  })
})
