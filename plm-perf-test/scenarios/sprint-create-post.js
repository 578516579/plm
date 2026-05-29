// PLM Sprint 新增并发 — 压力场景(stress)
// 100 VU × 2min,P95 < 800ms,错误率 < 1%
// 跑法: k6 run --env-file env/local.env scenarios/sprint-create-post.js
//
// 依赖:压测前需先跑 seed-project.sql(项目 id 100-149,Sprint 关联到此)

import { sleep } from 'k6'
import { Counter } from 'k6/metrics'
import { login } from '../lib/auth.js'
import { plmPost } from '../lib/http.js'
import { fakeSprint, randomInt } from '../lib/data.js'

const createSuccessCounter = new Counter('sprint_create_success')

export const options = {
  scenarios: {
    sprint_stress_create: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 100 },  // 爬到 100 并发
        { duration: '90s', target: 100 },  // 稳态 1.5min
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    'http_req_duration{name:sprint-create}': ['p(95)<800', 'p(99)<2000'],
    'http_req_failed': ['rate<0.01'],   // 错误率 < 1%
    'checks': ['rate>0.99'],
  },
}

export default function () {
  const token = login()

  // 随机挑一个 seed project id (100-149) 关联
  const projectId = randomInt(100, 149)
  const payload = fakeSprint(projectId)
  const res = plmPost(token, '/business/sprint', payload, 'sprint-create')

  // 业务 code:200=成功
  const code = res.json('code')
  if (code === 200) {
    createSuccessCounter.add(1)
  }

  sleep(Number(__ENV.THINK_TIME || 0.5))
}

// 清理:压测会插入大量数据,跑完后清
// DELETE FROM tb_sprint WHERE create_by='admin' AND sprint_name LIKE 'Sprint-压测-%';
