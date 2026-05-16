/** 数据库设计模块 E2E — PRD §F3.2 数据库设计 + 原型 dbdesign.html */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete } from './helpers/db'
import { RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

test.describe('数据库设计模块 E2E (PRD §F3.2)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`dbdesign-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`dbdesign-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_dbdesign', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-DBDESIGN-F001 创建数据库设计 (MySQL)', async () => {
    const r = await api.post('/business/dbdesign', {
      projectId,
      title: `AgriPLM核心表设计-${RUN_ID}`,
      dbType: 'mysql',
      erContent: '```mermaid\nerDiagram\n  tb_project ||--o{ tb_task : has\n```',
      ddlContent: 'CREATE TABLE tb_project (id BIGINT PRIMARY KEY);',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })

  test('TC-DBDESIGN-F002 创建数据库设计 (PostgreSQL)', async () => {
    const r = await api.post('/business/dbdesign', {
      projectId,
      title: `时序数据库设计-${RUN_ID}`,
      dbType: 'postgresql',
      dictContent: '## 传感器数据表\n| 字段 | 类型 | 说明 |\n|---|---|---|\n| ts | TIMESTAMPTZ | 时间戳 |',
      authorUserId: 1
    })
    expect(r.code).toBe(200)
  })
})
