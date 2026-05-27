/** 运维手册模块 E2E — PRD §F5.3 + 原型 opsmanual.html (11 case) */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, getFieldHex } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('运维手册模块 E2E (PRD §F5.3)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`manual-ops-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`manual-ops-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_manual_ops', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  const create = (suffix: string, extra: any = {}) =>
    api.post('/business/manual-ops', { projectId, title: `${suffix}-${RUN_ID}`, authorUserId: 1, ...extra })
  const idByTitle = async (suffix: string) => {
    const list = await api.get('/business/manual-ops/list', { projectId })
    return list.rows.find((r: any) => r.title.includes(`${suffix}-${RUN_ID}`))?.manualopsId
  }

  test('TC-MANUAL-OPS-F001 创建 (Prometheus + 钉钉/邮件 + 土壤传感器)', async () => {
    const r = await create('AgriPLM 运维手册', {
      monitoringPlan: 'prometheus_grafana', alertChannels: 'dingtalk,email',
      iotDeviceTypes: 'soil_sensor,weather_station', outputFormats: 'pdf'
    })
    expect(r.code).toBe(200)
  })

  test('TC-MANUAL-OPS-F002 创建 (Zabbix + 飞书多渠道 + 无人机)', async () => {
    const r = await create('多渠道运维手册', {
      monitoringPlan: 'zabbix', alertChannels: 'feishu,wework,email', iotDeviceTypes: 'drone,irrigation_controller'
    })
    expect(r.code).toBe(200)
  })

  test('TC-MANUAL-OPS-F003 title 必填 → 602', async () => {
    const r = await api.post('/business/manual-ops', { projectId, authorUserId: 1 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/标题/)
  })

  test('TC-MANUAL-OPS-F004 关联项目不存在 → 702', async () => {
    const r = await api.post('/business/manual-ops', { projectId: 99999999, title: `非法项目-${RUN_ID}`, authorUserId: 1 })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/关联项目不存在/)
  })

  test('TC-MANUAL-OPS-F005 alertChannels CSV 含非白名单项 → 604', async () => {
    const r = await create('非法告警渠道', { alertChannels: 'dingtalk,sms_unsupported' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/告警渠道/)
  })

  test('TC-MANUAL-OPS-F006 状态机正向 00→01→02 自动填 generatedAt', async () => {
    expect((await create('状态机正向')).code).toBe(200)
    const id = await idByTitle('状态机正向')
    expect((await api.put('/business/manual-ops', { manualopsId: id, status: '01' })).code).toBe(200)
    expect((await api.put('/business/manual-ops', { manualopsId: id, status: '02' })).code).toBe(200)
    const got = await api.get(`/business/manual-ops/${id}`)
    expect(got.data.status).toBe('02')
    expect(got.data.generatedAt).toBeTruthy()
  })

  test('TC-MANUAL-OPS-F007 状态机跳级 00→02 非法 → 601', async () => {
    expect((await create('跳级非法')).code).toBe(200)
    const id = await idByTitle('跳级非法')
    const r = await api.put('/business/manual-ops', { manualopsId: id, status: '02' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-MANUAL-OPS-F008 终态保护 03→00 非法 → 601', async () => {
    expect((await create('终态保护')).code).toBe(200)
    const id = await idByTitle('终态保护')
    await api.put('/business/manual-ops', { manualopsId: id, status: '01' })
    await api.put('/business/manual-ops', { manualopsId: id, status: '02' })
    await api.put('/business/manual-ops', { manualopsId: id, status: '03' })
    const r = await api.put('/business/manual-ops', { manualopsId: id, status: '00' })
    expect(r.code).not.toBe(200)
    expect(JSON.stringify(r)).toMatch(/不能直接转/)
  })

  test('TC-MANUAL-OPS-F009 AI 生成 → content + status=02 + aiGenerated=Y', async () => {
    expect((await create('AI 运维手册', { monitoringPlan: 'prometheus_grafana' })).code).toBe(200)
    const id = await idByTitle('AI 运维手册')
    const r = await api.post(`/business/manual-ops/ai/generate/${id}`, {})
    expect(r.code).toBe(200)
    expect(r.data.aiGenerated).toBe('Y')
    expect(r.data.status).toBe('02')
    expect(r.data.content).toContain('监控方案')
  })

  test('TC-MANUAL-OPS-F010 编号格式 OM-YYYY-NNNN', async () => {
    expect((await create('编号格式')).code).toBe(200)
    const list = await api.get('/business/manual-ops/list', { projectId })
    const row = list.rows.find((r: any) => r.title.includes(`编号格式-${RUN_ID}`))
    const year = new Date().getFullYear()
    expect(row.manualopsNo).toMatch(new RegExp(`^OM-${year}-\\d{4}$`))
  })

  test('TC-MANUAL-OPS-ENC001 编码 HEX — 中文 title 不含 EFBFBD', async () => {
    const cn = '农情运维手册-中文检测'
    expect((await create(cn)).code).toBe(200)
    const hex = getFieldHex('tb_manual_ops', 'title', `title like '${cn}-${RUN_ID}%'`)
    expect(hex.toUpperCase()).not.toContain('EFBFBD')
  })
})
