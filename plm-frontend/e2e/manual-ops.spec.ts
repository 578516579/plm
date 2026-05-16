/** 运维手册模块 E2E — PRD §F5.3 + 原型 opsmanual.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
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

  test('TC-MANUAL-OPS-F001 创建运维手册 (Prometheus + 钉钉+邮件 + 全 IoT)', async () => {
    const r = await api.post('/business/manual-ops', {
      projectId,
      title: `AgriPLM 运维手册-${RUN_ID}`,
      monitoringPlan: 'prometheus_grafana',
      alertChannels: 'dingtalk,email',
      iotDeviceTypes: 'soil_sensor,weather_station,drone,irrigation_controller',
      outputFormats: 'pdf,markdown',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-MANUAL-OPS-F002 创建运维手册 (Zabbix + 飞书)', async () => {
    const r = await api.post('/business/manual-ops', {
      projectId,
      title: `Zabbix 运维手册-${RUN_ID}`,
      monitoringPlan: 'zabbix',
      alertChannels: 'feishu',
      iotDeviceTypes: 'soil_sensor',
      outputFormats: 'pdf',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-MANUAL-OPS-F003 AI 生成运维手册', async () => {
    const createRes = await api.post('/business/manual-ops', {
      projectId,
      title: `AI 运维手册-${RUN_ID}`,
      monitoringPlan: 'aliyun_cms',
      alertChannels: 'wework,email',
      iotDeviceTypes: 'weather_station,drone',
      authorUserId: 1
    })
    expect(createRes.code).toBe(200)

    const list = await api.get('/business/manual-ops/list', { projectId })
    const mo = list.rows.find((r: any) => r.title.includes(`AI 运维手册-${RUN_ID}`))
    expect(mo).toBeDefined()

    const aiRes = await api.post(`/business/manual-ops/ai/generate/${mo.manualopsId}`, {})
    expect(aiRes.code).toBe(200)
    expect(aiRes.data.aiGenerated).toBe('Y')
    expect(aiRes.data.status).toBe('02')
    expect(aiRes.data.content).toContain('监控方案')
    expect(aiRes.data.content).toContain('IoT')
  })
})
