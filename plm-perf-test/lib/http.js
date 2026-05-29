// PLM HTTP 工具 — 包装 k6 http,统一断言 code=200 + msg=操作成功
// 用法: import { plmGet, plmPost, plmPut, plmDelete } from '../lib/http.js'

import http from 'k6/http'
import { check } from 'k6'
import { authHeaders } from './auth.js'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081'

/**
 * 通用断言:HTTP 200 + 业务 code=200
 */
function assertOk(res, name) {
  return check(res, {
    [`${name} HTTP 200`]: (r) => r.status === 200,
    [`${name} 业务 code=200`]: (r) => {
      try {
        return r.json('code') === 200
      } catch {
        return false
      }
    },
  })
}

export function plmGet(token, path, params = {}, name = 'GET') {
  const query = Object.keys(params).length
    ? '?' + new URLSearchParams(params).toString()
    : ''
  const res = http.get(`${BASE_URL}${path}${query}`, {
    headers: authHeaders(token),
    tags: { name },
  })
  assertOk(res, name)
  return res
}

export function plmPost(token, path, body, name = 'POST') {
  const res = http.post(`${BASE_URL}${path}`, JSON.stringify(body), {
    headers: authHeaders(token),
    tags: { name },
  })
  assertOk(res, name)
  return res
}

export function plmPut(token, path, body, name = 'PUT') {
  const res = http.put(`${BASE_URL}${path}`, JSON.stringify(body), {
    headers: authHeaders(token),
    tags: { name },
  })
  assertOk(res, name)
  return res
}

export function plmDelete(token, path, name = 'DELETE') {
  const res = http.del(`${BASE_URL}${path}`, null, {
    headers: authHeaders(token),
    tags: { name },
  })
  assertOk(res, name)
  return res
}
