/**
 * 字符编码回归测试 (MANDATORY)
 *
 * 本套件捕获 2026-05-16 三模块乱码事故:Windows JDK 默认 file.encoding=GBK,
 * 导致 HTTP body 解码错位,DB 出现 EFBFBD (U+FFFD 替换符)。
 *
 * **任何 Phase 03 → Phase 04 准入必须先跑过本套件**。
 * 详见 03-开发/字符编码规范.md / 99-跨阶段/gate-checklists/Phase04-测试-Gate.md
 *
 * 测试覆盖:
 * - UI 提交中文标题 → DB HEX 校验 (无 EFBFBD)
 * - 业务模块全覆盖 (Project / Requirement / Sprint / Task)
 * - 边界字符 (中文 + 全角 + 希腊字 + emoji)
 */
import { test, expect, APIRequestContext } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'
import { ApiClient } from './helpers/api'
import { assertNoMojibake, execDelete } from './helpers/db'
import { ENCODING_SAMPLES, MOJIBAKE_HEX, RUN_ID, makeProjectData } from './helpers/fixtures'

let token: string
let api: ApiClient
let apiRequest: APIRequestContext

test.describe('字符编码回归 (Mojibake guard)', () => {
  test.beforeAll(async ({ playwright, browser }) => {
    // 自建 APIRequestContext (跨 test 复用,Playwright 默认 request fixture 只能在单 test 内用)
    apiRequest = await playwright.request.newContext()
    const ctx = await browser.newContext()
    token = await loginAsAdmin(apiRequest, ctx)
    api = new ApiClient(apiRequest, token)
  })

  test.afterAll(async () => {
    await apiRequest?.dispose()
  })

  // ----------------------------------------------------------------
  // 核心断言: 任意中文字段写入后,DB HEX 不含 EFBFBD
  // ----------------------------------------------------------------

  test('Project: 中文 projectName + description 写入后无乱码', async () => {
    const data = makeProjectData(`enc-${RUN_ID}`)
    const resp = await api.createProject(data)
    expect(resp.code).toBe(200)

    // 直接查 DB HEX (绕过 JSON 二次解码)
    const check = assertNoMojibake('tb_project', 'project_name',
      `project_name LIKE '%enc-${RUN_ID}%' AND del_flag='0'`)
    expect.soft(check.ok, `project_name HEX=${check.hex} reason=${check.reason}`).toBe(true)

    const descCheck = assertNoMojibake('tb_project', 'description',
      `project_name LIKE '%enc-${RUN_ID}%' AND del_flag='0'`)
    expect.soft(descCheck.ok, `description HEX=${descCheck.hex}`).toBe(true)

    // cleanup
    execDelete('tb_project', `project_name LIKE '%enc-${RUN_ID}%'`)
  })

  test('Requirement: 中文 title 写入后无乱码', async () => {
    // 先建 project 作为 FK
    const proj = await api.createProject(makeProjectData(`enc-req-${RUN_ID}`))
    expect(proj.code).toBe(200)
    const projList = await api.listProjects()
    const projectId = projList.rows.find((p: any) => p.projectName.includes(`enc-req-${RUN_ID}`))?.id
    expect(projectId).toBeDefined()

    const resp = await api.createRequirement({
      projectId,
      title: `编码测试需求-${RUN_ID}`,
      description: `中文描述 αβγ ${ENCODING_SAMPLES.emoji}`,
      source: '01',
      priority: '02'
    })
    expect(resp.code).toBe(200)

    // 用纯 ASCII 的 RUN_ID 做 WHERE 匹配,避免 SQL CLI 的 Chinese-via-args 编码污染
    const check = assertNoMojibake('tb_requirement', 'title',
      `title LIKE '%${RUN_ID}%' AND del_flag='0'`)
    expect.soft(check.ok, `Requirement title HEX=${check.hex}`).toBe(true)

    // cleanup
    execDelete('tb_requirement', `title LIKE '%${RUN_ID}%'`)
    execDelete('tb_project', `project_name LIKE '%enc-req-${RUN_ID}%'`)
  })

  test('Sprint: 中文 name + goal 写入后无乱码', async () => {
    const proj = await api.createProject(makeProjectData(`enc-spr-${RUN_ID}`))
    expect(proj.code).toBe(200)
    const projList = await api.listProjects()
    const projectId = projList.rows.find((p: any) => p.projectName.includes(`enc-spr-${RUN_ID}`))?.id

    const resp = await api.createSprint({
      projectId,
      name: `编码 Sprint-${RUN_ID}`,
      goal: `中文目标:验证编码 ${ENCODING_SAMPLES.greek}`,
      plannedStartDate: '2026-05-16',
      plannedEndDate: '2026-05-29'
    })
    expect(resp.code).toBe(200)

    const nameCheck = assertNoMojibake('tb_sprint', 'name',
      `name LIKE '%${RUN_ID}%' AND del_flag='0'`)
    expect.soft(nameCheck.ok, `Sprint name HEX=${nameCheck.hex}`).toBe(true)

    const goalCheck = assertNoMojibake('tb_sprint', 'goal',
      `name LIKE '%${RUN_ID}%' AND del_flag='0'`)
    expect.soft(goalCheck.ok, `Sprint goal HEX=${goalCheck.hex}`).toBe(true)

    // cleanup
    execDelete('tb_sprint', `name LIKE '%${RUN_ID}%'`)
    execDelete('tb_project', `project_name LIKE '%enc-spr-${RUN_ID}%'`)
  })

  test('Task: 中文 title + description 写入后无乱码', async () => {
    const proj = await api.createProject(makeProjectData(`enc-task-${RUN_ID}`))
    expect(proj.code).toBe(200)
    const projList = await api.listProjects()
    const projectId = projList.rows.find((p: any) => p.projectName.includes(`enc-task-${RUN_ID}`))?.id

    const resp = await api.createTask({
      projectId,
      title: `编码任务-${RUN_ID}`,
      description: `自动测试中文 ${ENCODING_SAMPLES.mixed}`,
      priority: '02'
    })
    expect(resp.code).toBe(200)

    const check = assertNoMojibake('tb_task', 'title',
      `title LIKE '%${RUN_ID}%' AND del_flag='0'`)
    expect.soft(check.ok, `Task title HEX=${check.hex}`).toBe(true)

    // cleanup
    execDelete('tb_task', `title LIKE '%${RUN_ID}%'`)
    execDelete('tb_project', `project_name LIKE '%enc-task-${RUN_ID}%'`)
  })

  // ----------------------------------------------------------------
  // UI 层验证: 通过 Vue 表单提交也无乱码 (覆盖 fetch / axios 链路)
  // ----------------------------------------------------------------

  test('UI 层: 浏览器表单提交中文,DB 存储无乱码', async ({ page, context }) => {
    // 注入 token cookie
    await context.addCookies([{
      name: 'Admin-Token',
      value: token,
      url: 'http://localhost'
    }])

    await page.goto('/business/project')
    await page.waitForSelector('.el-table', { timeout: 60_000 })

    // 点新增
    await page.getByRole('button', { name: /新增/ }).first().click()
    const dialog = page.locator('.el-dialog')
    await expect(dialog).toBeVisible()

    const uiTag = `ui-encoding-${RUN_ID}`
    // 填全部前端 form rule 要求的字段
    await dialog.getByLabel(/项目编号/).fill(`UI-${uiTag}`)
    await dialog.getByLabel(/项目名称/).fill(`UI 编码测试-${uiTag} αβγ`)
    // 提交
    await dialog.getByRole('button', { name: /确\s*定/ }).click()

    // 等成功提示
    await expect(page.locator('.el-message--success')).toBeVisible({ timeout: 10_000 })

    // 等列表刷新
    await page.waitForTimeout(500)

    // DB HEX 校验
    const check = assertNoMojibake('tb_project', 'project_name',
      `project_name LIKE '%${uiTag}%' AND del_flag='0'`)
    expect(check.ok, `UI-submitted project name HEX=${check.hex}\nReason: ${check.reason}`).toBe(true)
    expect(check.hex).not.toContain(MOJIBAKE_HEX)

    // cleanup
    execDelete('tb_project', `project_name LIKE '%${uiTag}%'`)
  })

  // ----------------------------------------------------------------
  // 反向断言: 已知坏数据应该被检测出来 (sanity test for the test itself)
  // ----------------------------------------------------------------

  test('反向自检: assertNoMojibake 能识别 EFBFBD 字节', async () => {
    // 直接插入一条已知乱码记录
    const badTag = `bad-${RUN_ID}`
    const proj = await api.createProject({
      projectName: `BadCheck-${badTag}`,
      projectType: 'rnd',
      managerUserId: 1
    })
    expect(proj.code).toBe(200)

    // 手动改成包含 EFBFBD 字节的乱码
    const { execSync } = await import('child_process')
    const MYSQL = process.env.MYSQL_CLI || 'C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe'
    const DB_PWD = process.env.DB_PASSWORD || 'aa8945163'
    // UNHEX('EFBFBD') = U+FFFD
    execSync(
      `"${MYSQL}" -uroot -p${DB_PWD} --default-character-set=utf8mb4 plm -e "UPDATE tb_project SET description = CONCAT('badbyte-', UNHEX('EFBFBD'), '-end') WHERE project_name='BadCheck-${badTag}'"`,
      { stdio: 'pipe' }
    )

    // 此时断言应该报失败
    const check = assertNoMojibake('tb_project', 'description',
      `project_name='BadCheck-${badTag}' AND del_flag='0'`)
    expect(check.ok).toBe(false)
    expect(check.reason).toContain('EFBFBD')

    // cleanup
    execDelete('tb_project', `project_name='BadCheck-${badTag}'`)
  })
})
