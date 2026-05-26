import { defineConfig, devices } from '@playwright/test'
import { existsSync } from 'node:fs'
import { resolve } from 'node:path'

// 加载本地敏感值(plm-frontend/.env.local,已 gitignore)— 例如 DB_PASSWORD
// shell 已 export 的值优先,文件值只填空缺(loadEnvFile 默认不覆盖现有 env)
const envLocal = resolve(process.cwd(), '.env.local')
if (existsSync(envLocal)) {
  process.loadEnvFile(envLocal)
}

/**
 * Playwright 配置 — Project 模块 E2E 测试
 *
 * 假设服务都已启动：
 *   - 后端：http://localhost:8081（PID 见 netstat）
 *   - 前端：http://localhost:80（Vite dev server）
 *   - MySQL：localhost:3306/plm
 *   - Redis：127.0.0.1:6379
 *
 * 运行：
 *   cd plm-frontend
 *   npx playwright test                    # 全部跑（headless）
 *   npx playwright test --headed           # 看着跑
 *   npx playwright test -g "状态机"        # 跑某个测试
 *   npx playwright test --debug            # 单步调试
 */
export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  fullyParallel: false, // 测试间共享数据库状态，串行更稳
  forbidOnly: !!process.env.CI,
  retries: 0,
  workers: 1, // 单 worker 避免 captcha 竞争
  reporter: [['list']],

  use: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:80',
    actionTimeout: 10_000,
    navigationTimeout: 15_000,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure'
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ]
})
