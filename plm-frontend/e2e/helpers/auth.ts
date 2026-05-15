import { APIRequestContext, BrowserContext } from '@playwright/test'
import { execSync } from 'child_process'

/**
 * Programmatic 登录辅助。
 *
 * 思路：
 *   1. GET /captchaImage 拿 uuid（绕过浏览器中的图形 captcha）
 *   2. 调本机 redis-cli GET captcha_codes:<uuid> 拿 code
 *   3. POST /login 拿 JWT
 *   4. 把 JWT 写入 cookie `Admin-Token`（前端从 cookie 取，见 src/utils/auth.ts）
 *
 * 这是工业 E2E 测试的常见做法：先 programmatic login，再 UI 验证。
 * 避免 captcha 图像识别难题，且测试每轮都拿新 token，避免 token 过期问题。
 *
 * 依赖 redis-cli 在 PATH 或下面常见路径之一。可设环境变量 REDIS_CLI 覆盖。
 */
export async function loginAsAdmin(
  request: APIRequestContext,
  context: BrowserContext
): Promise<string> {
  const backendURL = process.env.E2E_BACKEND_URL || 'http://localhost:8081'

  // 1. 拿 captcha uuid
  const cap = await request.get(`${backendURL}/captchaImage`)
  const capJson = await cap.json()
  const uuid: string = capJson.uuid
  if (!uuid) throw new Error(`captcha 失败: ${JSON.stringify(capJson)}`)

  // 2. 从 redis 拿对应 code
  const redisCli = findRedisCli()
  const redisHost = process.env.REDIS_HOST || '127.0.0.1'
  const redisPort = process.env.REDIS_PORT || '6379'
  const codeRaw = execSync(
    `"${redisCli}" -h ${redisHost} -p ${redisPort} GET "captcha_codes:${uuid}"`,
    { encoding: 'utf8' }
  )
  const code = codeRaw.trim().replace(/^"|"$/g, '')
  if (!code) throw new Error(`redis 拿不到 captcha code (uuid=${uuid})`)

  // 3. 登录
  const loginResp = await request.post(`${backendURL}/login`, {
    data: {
      username: 'admin',
      password: 'admin123',
      code,
      uuid
    }
  })
  const loginJson = await loginResp.json()
  if (loginJson.code !== 200 || !loginJson.token) {
    throw new Error(`登录失败: ${JSON.stringify(loginJson)}`)
  }
  const token: string = loginJson.token

  // 4. 注入 Admin-Token cookie（domain 与前端 BASE_URL 一致）
  const baseURL = process.env.PLAYWRIGHT_BASE_URL || 'http://localhost'
  const url = new URL(baseURL)
  await context.addCookies([
    {
      name: 'Admin-Token',
      value: token,
      domain: url.hostname,
      path: '/',
      httpOnly: false,
      secure: false
    }
  ])

  return token
}

function findRedisCli(): string {
  if (process.env.REDIS_CLI) return process.env.REDIS_CLI
  const candidates = [
    'redis-cli',
    'D:\\Program Files\\Redis\\redis-cli.exe',
    'C:\\Program Files\\Redis\\redis-cli.exe',
    '/usr/local/bin/redis-cli',
    '/opt/homebrew/bin/redis-cli'
  ]
  for (const c of candidates) {
    try {
      execSync(`"${c}" --version`, { stdio: 'pipe' })
      return c
    } catch {}
  }
  throw new Error(
    'redis-cli 找不到。请在 PATH 中加入 redis-cli，或设环境变量 REDIS_CLI=完整路径'
  )
}
