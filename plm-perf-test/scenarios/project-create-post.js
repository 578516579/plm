// PLM Project 新增并发 — 压力场景(stress),专测 ADR-0001 编号撞号重试
// 100 VU × 2min,P95 < 800ms,撞号重试率应低(< 5%)
// 跑法: k6 run --env-file env/local.env scenarios/project-create-post.js
//
// 关键基线:
//   - ADR-0001:同年内 project_no 自动生成 PRJ-YYYY-NNNN,UNIQUE 索引兜底
//   - 高并发下 selectMaxSeqOfYear 读取的 maxSeq 可能与其他 VU 重叠 → DuplicateKeyException → 1 次重试
//   - 撞号重试由业务代码处理,客户端看到的应仍是 200(除非重试也撞号)

import { sleep } from 'k6'
import { Counter, Rate } from 'k6/metrics'
import { login } from '../lib/auth.js'
import { plmPost } from '../lib/http.js'
import { fakeProject } from '../lib/data.js'

const createSuccessCounter = new Counter('project_create_success')
const duplicateKeyRate = new Rate('project_create_duplicate_key_rate')

export const options = {
  scenarios: {
    stress_create: {
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
    'http_req_duration{name:project-create}': ['p(95)<800', 'p(99)<2000'],
    'http_req_failed': ['rate<0.01'],   // 含重试后失败仍 < 1%
    'project_create_duplicate_key_rate': ['rate<0.05'],  // 撞号 < 5%
    'checks': ['rate>0.99'],
  },
}

export default function () {
  const token = login()

  const payload = fakeProject()
  const res = plmPost(token, '/business/project', payload, 'project-create')

  // 业务 code:200=成功;601=校验失败;701=状态非法
  const code = res.json('code')
  if (code === 200) {
    createSuccessCounter.add(1)
  }

  // 检测撞号(若 ServiceImpl 重试也失败,会返非 200 + 含 DuplicateKey 的 msg)
  const msg = res.json('msg') || ''
  duplicateKeyRate.add(msg.includes('Duplicate') || msg.includes('重号') ? 1 : 0)

  sleep(Number(__ENV.THINK_TIME || 0.5))
}

// 清理:压测会插入大量数据,跑完后清
// 方法 A: 启动后端时设 --spring.profiles.active=perf-test,业务码 600+ 不入 DB
// 方法 B: 跑完手动执行: DELETE FROM tb_project WHERE create_by='admin' AND remark LIKE '性能测试%'
// 方法 C: 用 seed-cleanup.sql 全清并重 seed
