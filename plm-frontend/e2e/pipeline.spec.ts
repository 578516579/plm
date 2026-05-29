/** CI/CD 流水线模块 E2E — DevOps 扩展 + 原型 pipeline.html (10 case) */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, getFieldHex } from './helpers/db'
import { RUN_ID } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext

test.describe('Pipeline 模块 E2E (DevOps)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
  })

  test.afterAll(async () => {
    execDelete('tb_pipeline', `pipeline_name like '%${RUN_ID}%'`)
    await apiRequest?.dispose()
  })

  const create = (suffix: string, extra: any = {}) =>
    api.post('/business/pipeline', { pipelineName: `${suffix}-${RUN_ID}`, repoName: 'agriplm/backend', cicdTool: 'jenkins', triggerType: 'push', authorUserId: 1, ...extra })
  const idByName = async (suffix: string) => {
    const list = await api.get('/business/pipeline/list', { pipelineName: `${suffix}-${RUN_ID}` })
    return list.rows[0]?.pipelineId
  }

  test('TC-PIPELINE-F001 创建 Jenkins push 触发流水线', async () => {
    const r = await create('AgriPLM-Build', { repoBranch: 'main' })
    expect(r.code).toBe(200)
  })

  test('TC-PIPELINE-F002 Cron 触发必须带 cronExpr → 602', async () => {
    const r = await create('Cron-Bad', { triggerType: 'cron' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/Cron/)
  })

  test('TC-PIPELINE-F003 触发流水线累计 successRate', async () => {
    expect((await create('Trigger-Test', { cicdTool: 'gitea', triggerType: 'manual' })).code).toBe(200)
    const id = await idByName('Trigger-Test')
    const t1 = await api.post(`/business/pipeline/trigger/${id}`, {})
    expect(t1.code).toBe(200)
    expect(t1.data.totalRuns).toBe(1)
    expect(['success', 'failed']).toContain(t1.data.lastRunStatus)
  })

  test('TC-PIPELINE-F004 pipelineName 必填 → 602', async () => {
    const r = await api.post('/business/pipeline', { repoName: 'agriplm/x', authorUserId: 1 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/流水线名称/)
  })

  test('TC-PIPELINE-F005 repoName 必填 → 602', async () => {
    const r = await api.post('/business/pipeline', { pipelineName: `缺仓库-${RUN_ID}`, authorUserId: 1 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/代码仓库/)
  })

  test('TC-PIPELINE-F006 cicdTool 白名单非法 → 604', async () => {
    const r = await create('非法工具', { cicdTool: 'travis' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/CICD 工具/)
  })

  test('TC-PIPELINE-F007 状态机 00→01→00 (启用↔停用)', async () => {
    expect((await create('状态机')).code).toBe(200)
    const id = await idByName('状态机')
    expect((await api.put('/business/pipeline', { pipelineId: id, status: '01' })).code).toBe(200)
    expect((await api.put('/business/pipeline', { pipelineId: id, status: '00' })).code).toBe(200)
  })

  test('TC-PIPELINE-F008 已停用触发 → 601', async () => {
    expect((await create('停用触发')).code).toBe(200)
    const id = await idByName('停用触发')
    await api.put('/business/pipeline', { pipelineId: id, status: '01' })
    const r = await api.post(`/business/pipeline/trigger/${id}`, {})
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/已停用/)
  })

  test('TC-PIPELINE-F009 编号格式 PIPE-YYYY-NNNN', async () => {
    expect((await create('编号格式')).code).toBe(200)
    const list = await api.get('/business/pipeline/list', { pipelineName: `编号格式-${RUN_ID}` })
    const year = new Date().getFullYear()
    expect(list.rows[0].pipelineNo).toMatch(new RegExp(`^PIPE-${year}-\\d{4}$`))
  })

  test('TC-PIPELINE-ENC001 编码 HEX — 中文 pipeline_name 不含 EFBFBD', async () => {
    const cn = '农情流水线-中文检测'
    expect((await create(cn)).code).toBe(200)
    const hex = getFieldHex('tb_pipeline', 'pipeline_name', `pipeline_name like '${cn}-${RUN_ID}%'`)
    expect(hex.toUpperCase()).not.toContain('EFBFBD')
  })

  test('TC-PIPELINE-F-DELETE 软删: create → list 存在 → delete → list 不存在', async () => {
    const createResp = await create('删除测试')
    expect(createResp.code, '创建应成功').toBe(200)

    const id = await idByName('删除测试')
    expect(id, '新建 pipeline 应能在列表里查到').toBeDefined()
    expect(typeof id, 'pipelineId 应是 number').toBe('number')

    const delResp = await api.delete(`/business/pipeline/${id}`)
    expect(delResp.code, '删除应成功 (code=200)').toBe(200)

    const after = await api.get('/business/pipeline/list', { pipelineName: `删除测试-${RUN_ID}` })
    const stillThere = after.rows.find((r: any) => r.pipelineId === id)
    expect(stillThere, `pipelineId=${id} 删除后不该出现在 list`).toBeUndefined()
  })
})
