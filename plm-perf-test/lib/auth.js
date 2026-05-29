// PLM 登录工具 — captcha + login → 拿 Authorization Bearer token
// 用法:
//   import { login, authHeaders } from '../lib/auth.js'
//   const token = login()
//   const headers = authHeaders(token)

import http from 'k6/http'
import { check, fail } from 'k6'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081'
const USERNAME = __ENV.PLM_USERNAME || 'admin'
const PASSWORD = __ENV.PLM_PASSWORD || 'admin123'

// 全局 token 缓存(每 VU 一份;k6 每个 VU 是独立 JS runtime)
let cachedToken = null

/**
 * 登录拿 JWT token
 * @returns {string} token
 */
export function login() {
  if (cachedToken) return cachedToken

  // Step 1: 拿 captcha (uuid + code)
  // 性能测试默认假设 captchaEnabled=false 或测试账号免验证码
  // 若启用,在 application.yml 设 plm.captchaEnabled=false 后跑性能测试
  const captchaRes = http.get(`${BASE_URL}/captchaImage`, {
    tags: { name: 'captchaImage' },
  })
  check(captchaRes, { 'captcha 200': (r) => r.status === 200 })
  const captchaBody = captchaRes.json()
  const captchaEnabled = captchaBody.captchaEnabled

  // Step 2: 登录
  const loginPayload = {
    username: USERNAME,
    password: PASSWORD,
  }
  if (captchaEnabled) {
    loginPayload.uuid = captchaBody.uuid
    loginPayload.code = '1234' // 性能测试约定:测试环境不校验,此处占位
  }

  const loginRes = http.post(`${BASE_URL}/login`, JSON.stringify(loginPayload), {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'login' },
  })

  const ok = check(loginRes, {
    'login 200': (r) => r.status === 200,
    'login 返回 token': (r) => r.json('token') !== undefined,
  })

  if (!ok) {
    fail(`登录失败 status=${loginRes.status} body=${loginRes.body}`)
  }

  cachedToken = loginRes.json('token')
  return cachedToken
}

/**
 * 构造带 Bearer token 的 headers
 * @param {string} token
 * @returns {Object}
 */
export function authHeaders(token) {
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  }
}

/**
 * 清缓存 — 用于强制重新登录(token 过期场景)
 */
export function clearTokenCache() {
  cachedToken = null
}
