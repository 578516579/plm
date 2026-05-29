// PLM Project 状态机并发测试 — 测 PRD §3.3 5×5 状态机在并发下的冲突
// 50 VU 共享 10 个 project,每个 VU 随机选 project 做状态变更
// 跑法: k6 run --env-file env/local.env scenarios/project-state-transition.js
//
// 关键基线:
//   - 5 个状态: 0(未启动) / 1(进行中) / 2(暂停) / 3(已完成) / 4(已取消)
//   - 终态: 3, 4 不可再变 → 客户端期望 701
//   - 合法转换并发 → 期望最终一致(乐观锁/version 列没有,所以是 last-write-wins)

import { sleep } from 'k6'
import { Counter, Rate } from 'k6/metrics'
import { login } from '../lib/auth.js'
import { plmGet, plmPut } from '../lib/http.js'
import { randomInt, pickOne } from '../lib/data.js'

// 测试前需 seed 10 个项目:见 plm-backend/sql/seed/seed-project.sql
// 取 seed 出的 id 区间(假设 100-109,可由环境变量覆盖)
const SEED_PROJECT_IDS = (__ENV.SEED_PROJECT_IDS || '100,101,102,103,104,105,106,107,108,109')
  .split(',').map(s => parseInt(s, 10))

const stateConflict701Rate = new Rate('state_conflict_701_rate')
const successCount = new Counter('state_transition_success')

export const options = {
  scenarios: {
    state_machine_concurrency: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '15s', target: 50 },
        { duration: '90s', target: 50 },
        { duration: '15s', target: 0 },
      ],
    },
  },
  thresholds: {
    'http_req_duration{name:project-edit}': ['p(95)<500'],
    'http_req_failed': ['rate<0.01'],
    // 70% 概率随机命中终态,所以 701 错误率应 < 70%
    'state_conflict_701_rate': ['rate<0.7'],
  },
}

const STATUSES = ['0', '1', '2', '3', '4']

export default function () {
  const token = login()

  const projectId = pickOne(SEED_PROJECT_IDS)

  // 先 GET 看当前 status
  const getRes = plmGet(token, `/business/project/${projectId}`, {}, 'project-edit')
  const current = getRes.json('data') || {}
  const currentStatus = current.status

  // 选一个新 status(随机,可能合法可能非法)
  const newStatus = pickOne(STATUSES.filter(s => s !== currentStatus))

  const editRes = plmPut(
    token,
    '/business/project',
    { id: projectId, status: newStatus },
    'project-edit'
  )
  const code = editRes.json('code')

  if (code === 200) {
    successCount.add(1)
    stateConflict701Rate.add(0)
  } else if (code === 701) {
    stateConflict701Rate.add(1)
  }

  sleep(Number(__ENV.THINK_TIME || 0.3))
}
