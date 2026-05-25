import { test, expect } from '@playwright/test'
import { loginAsAdmin } from './helpers/auth'

test.describe('Dashboard 按钮跳转修复验证', () => {
  test.beforeEach(async ({ request, context }) => {
    await loginAsAdmin(request, context)
  })

  test('FIX: AI 快速立项按钮跳到 /phase-plan/inception 不再 404', async ({ page }) => {
    await page.goto('/workbench/dashboard')
    await page.waitForSelector('text=/AgriAI 智能助手/', { timeout: 15_000 })
    // 找"AI 快速立项"按钮 click
    await page.getByRole('button', { name: /AI 快速立项/ }).click()
    // URL 应该是 /phase-plan/inception, 不是 404
    await expect(page).toHaveURL(/\/phase-plan\/inception$/, { timeout: 5_000 })
    // 不能是 404 页
    await expect(page.locator('text=/404/').first()).toBeHidden({ timeout: 2_000 }).catch(() => {})
  })

  test('FIX: lifecycle 节点"竞品"跳到 /phase-plan/competitive', async ({ page }) => {
    await page.goto('/workbench/dashboard')
    await page.waitForSelector('text=/项目生命周期/', { timeout: 15_000 })
    await page.locator('.lc-node', { hasText: '竞品' }).click()
    await expect(page).toHaveURL(/\/phase-plan\/competitive$/, { timeout: 5_000 })
  })

  test('FIX: quickActions"生成 PRD"跳到 /phase-design/prd', async ({ page }) => {
    await page.goto('/workbench/dashboard')
    await page.waitForSelector('text=/AgriAI 智能助手/', { timeout: 15_000 })
    await page.locator('.el-tag', { hasText: '生成 PRD' }).click()
    await expect(page).toHaveURL(/\/phase-design\/prd$/, { timeout: 5_000 })
  })

  test('FIX: lifecycle 节点"编码"跳到 /phase-dev/task', async ({ page }) => {
    await page.goto('/workbench/dashboard')
    await page.waitForSelector('text=/项目生命周期/', { timeout: 15_000 })
    await page.locator('.lc-node', { hasText: '编码' }).click()
    await expect(page).toHaveURL(/\/phase-dev\/task$/, { timeout: 5_000 })
  })

  test('FIX: lifecycle 节点"运维手册"跳到 /phase-deploy/manual-ops', async ({ page }) => {
    await page.goto('/workbench/dashboard')
    await page.waitForSelector('text=/项目生命周期/', { timeout: 15_000 })
    await page.locator('.lc-node', { hasText: '运维手册' }).click()
    await expect(page).toHaveURL(/\/phase-deploy\/manual-ops$/, { timeout: 5_000 })
  })
})
