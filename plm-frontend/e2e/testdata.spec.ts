/**
 * TestData 模块 E2E — PRD §F4.3 测试数据工厂 + 原型 testdata.html
 *
 * 覆盖 (补 P0 双缺口之 E2E 层,1 占位 → 11 真实 case):
 *   - CRUD + TD-YYYY-NNNN 编号 + 默认值 (json/1000/规则 YYYN/aiGenerated=N/status=00)
 *   - 编码守门员: 中文 title DB HEX 无 EFBFBD
 *   - ENUM 白名单: targetTable / outputFormat → 604
 *   - FK projectId 不存在 → 702
 *   - 必填: title / targetTable → 602
 *   - 新建非草稿 → 601
 *   - 3 状态机无反向边: 00→01→02 合法 / 00→02 跨级 / 01→00 反向 / 02→01 终态 → 601
 *   - generate (AI mock): status=01 + aiGenerated=Y + generatedContent + 中国坐标语义
 *   - UI 菜单可达
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { execDelete, assertNoMojibake } from './helpers/db'
import { RUN_ID, makeProjectData, ERROR_CODES } from './helpers/fixtures'

let token: string, api: ApiClient, apiRequest: APIRequestContext, projectId: number

/** 创建一个测试数据集 (默认土壤传感器),返回响应 + title */
async function createDataset(suffix: string, overrides: Record<string, any> = {}) {
  const title = `TD-${suffix}-${RUN_ID}`
  const r = await api.post('/business/testdata', {
    projectId,
    title,
    targetTable: 'soil_sensor',
    authorUserId: 1,
    ...overrides
  })
  return { r, title }
}

/** 按 title 在列表里反查 (add 端点不回 id) */
async function findByTitle(title: string): Promise<any> {
  const list = await api.get('/business/testdata/list', { pageSize: 100 })
  return list.rows.find((x: any) => x.title === title)
}

test.describe('TestData 模块 E2E (PRD §F4.3)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
    await api.createProject(makeProjectData(`td-suite-${RUN_ID}`))
    const pl = await api.listProjects()
    projectId = pl.rows.find((p: any) => p.projectName.includes(`td-suite-${RUN_ID}`))?.id
  })

  test.afterAll(async () => {
    if (projectId) {
      execDelete('tb_testdata', `project_id=${projectId}`)
      execDelete('tb_project', `id=${projectId}`)
    }
    await apiRequest?.dispose()
  })

  test('TC-TD-F001 创建数据集 + TD-YYYY-NNNN 编号 + 默认值填充', async () => {
    const { r, title } = await createDataset('crud', {
      generateCount: 5000,
      outputFormat: 'json',
      ruleChinaCoord: 'Y',
      ruleTimeContinuity: 'Y',
      ruleSensorRange: 'Y',
      ruleIncludeOutliers: 'N'
    })
    expect(r.code).toBe(200)
    const t = await findByTitle(title)
    expect(t.testdataNo, '编号格式 TD-YYYY-NNNN').toMatch(/^TD-\d{4}-\d{4}$/)
    expect(t.status, '新建默认草稿').toBe('00')
    expect(t.outputFormat).toBe('json')
    expect(t.generateCount).toBe(5000)
    expect(t.ruleChinaCoord).toBe('Y')
    expect(t.ruleIncludeOutliers).toBe('N')
    expect(t.aiGenerated, '未生成前 aiGenerated=N').toBe('N')
  })

  test('TC-TD-F002 编码守门员: 中文 title DB HEX 无 EFBFBD', async () => {
    const title = `编码自检-测试数据-${RUN_ID}`
    const r = await api.post('/business/testdata', {
      projectId, title, targetTable: 'weather', authorUserId: 1,
      remark: '需求标题：测试，结束。αβγ 🎯'
    })
    expect(r.code).toBe(200)
    const t = await findByTitle(title)
    const enc = assertNoMojibake('tb_testdata', 'title', `testdata_no='${t.testdataNo}'`)
    expect(enc.ok, enc.reason).toBe(true)
    expect(enc.hex, 'title 应以「编码」UTF-8 字节开头').toContain('E7BC96E7A081')
  })

  test('TC-TD-F003 targetTable 非白名单 → 604', async () => {
    const { r } = await createDataset('bad-table', { targetTable: 't_unknown' })
    expect(r.code).toBe(ERROR_CODES.FIELD_FORMAT) // 604
    expect(r.msg).toContain('目标表')
  })

  test('TC-TD-F004 outputFormat 非白名单 → 604', async () => {
    const { r } = await createDataset('bad-format', { outputFormat: 'xml' })
    expect(r.code).toBe(ERROR_CODES.FIELD_FORMAT) // 604
    expect(r.msg).toContain('输出格式')
  })

  test('TC-TD-F005 FK projectId 不存在 → 702', async () => {
    const r = await api.post('/business/testdata', {
      projectId: 99999999, title: `fk-${RUN_ID}`, targetTable: 'crop', authorUserId: 1
    })
    expect(r.code).toBe(ERROR_CODES.FK_NOT_EXISTS) // 702
    expect(r.msg).toContain('关联项目不存在')
  })

  test('TC-TD-F006 必填校验 (title / targetTable) → 602', async () => {
    let r = await api.post('/business/testdata', {
      projectId, targetTable: 'soil_sensor', authorUserId: 1
    })
    expect(r.code).toBe(ERROR_CODES.REQUIRED_FIELD) // 602
    r = await api.post('/business/testdata', {
      projectId, title: `no-table-${RUN_ID}`, authorUserId: 1
    })
    expect(r.code).toBe(ERROR_CODES.REQUIRED_FIELD) // 602
  })

  test('TC-TD-F007 新建状态非 00 → 601', async () => {
    const { r } = await createDataset('non-draft', { status: '01' })
    expect(r.code).toBe(ERROR_CODES.STATUS_VIOLATION) // 601
    expect(r.msg).toContain('草稿')
  })

  test('TC-TD-F008 状态机合法链 00 → 01 → 02', async () => {
    const { title } = await createDataset('sm-legal')
    const t = await findByTitle(title)
    const id = t.testdataId
    expect((await api.put('/business/testdata', { testdataId: id, status: '01' })).code, '00→01').toBe(200)
    expect((await api.put('/business/testdata', { testdataId: id, status: '02' })).code, '01→02').toBe(200)
  })

  test('TC-TD-F009 状态机非法 (跨级/反向/终态) 全 601', async () => {
    // 00 → 02 跨级
    const a = await createDataset('sm-jump')
    const idA = (await findByTitle(a.title)).testdataId
    expect.soft((await api.put('/business/testdata', { testdataId: idA, status: '02' })).code,
      '00→02 跨级应拒').toBe(ERROR_CODES.STATUS_VIOLATION)

    // 01 → 00 反向 (本状态机无反向边)
    const b = await createDataset('sm-reverse')
    const idB = (await findByTitle(b.title)).testdataId
    await api.put('/business/testdata', { testdataId: idB, status: '01' })
    expect.soft((await api.put('/business/testdata', { testdataId: idB, status: '00' })).code,
      '01→00 反向应拒').toBe(ERROR_CODES.STATUS_VIOLATION)

    // 02 → 01 终态保护
    const c = await createDataset('sm-terminal')
    const idC = (await findByTitle(c.title)).testdataId
    await api.put('/business/testdata', { testdataId: idC, status: '01' })
    await api.put('/business/testdata', { testdataId: idC, status: '02' })
    expect.soft((await api.put('/business/testdata', { testdataId: idC, status: '01' })).code,
      '02→01 终态应拒').toBe(ERROR_CODES.STATUS_VIOLATION)
  })

  test('TC-TD-F010 generate (AI mock) → status=01 + aiGenerated=Y + 中国坐标语义', async () => {
    const { title } = await createDataset('gen')
    const t = await findByTitle(title)
    const r = await api.post(`/business/testdata/generate/${t.testdataId}`, {})
    expect(r.code).toBe(200)
    expect(r.data.status, 'generate 后 → 已生成').toBe('01')
    expect(r.data.aiGenerated).toBe('Y')
    expect(r.data.generatedAt).toBeTruthy()
    expect(r.data.fieldSemantics, '字段语义含中国坐标范围').toContain('中国范围')
    expect(r.data.generatedContent, '样本含 soil_moisture').toContain('soil_moisture')
  })

  test('TC-TD-UI 测试数据管理菜单可访问', async ({ page, context, request }) => {
    // 动态路由场景: 给 fresh context 重新 login 注入菜单,否则 /business/testdata 404
    await loginAsAdmin(request, context)
    await page.goto('/business/testdata')
    await expect(page.locator('.el-table')).toBeVisible({ timeout: 10_000 })
  })
})
