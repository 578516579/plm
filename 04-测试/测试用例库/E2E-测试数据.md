# E2E 测试数据规约

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| 关联代码 | `plm-frontend/e2e/helpers/fixtures.ts` |
| 状态 | active |

---

## 1. 数据隔离机制

### 1.1 RUN_ID

每次跑测试时生成全局唯一 `RUN_ID`,格式:`YYYYMMDD-HHMMSS-rand4`

```ts
// fixtures.ts
export const RUN_ID = '20260516-141023-7a3b'  // 示例
```

**用途**:所有测试生成的数据都带这个后缀,保证:
- 并发跑互不影响
- 单次跑测试间隔离
- 失败后清理只删本次产生的

### 1.2 测试 teardown 三层防护

```ts
test.afterAll(async () => {
  // 1. 业务层逻辑删 (走 service 接口,触发完整业务逻辑)
  // 2. DB 层物理删 (兜底,保证 DB 干净)
  execDelete('tb_task', `project_id=${projectId}`)
  // 3. 释放资源 (APIRequestContext)
  await apiRequest?.dispose()
})
```

---

## 2. 编码测试样本（fixtures.ts §ENCODING_SAMPLES）

| Key | 内容 | UTF-8 HEX 前缀 | 用途 |
|---|---|---|---|
| `cn` | `编码自检测试` | `E7BC96E7A081` | 标准中文 |
| `cnPunct` | `需求标题:测试,结束。` | `E99C80E6B182` | 含全角标点 |
| `greek` | `αβγδε` | `CEB1CEB2CEB3` | 希腊字母 |
| `emoji` | `🚀✨🎯` | `F09F9A80` | emoji (4-byte UTF-8) |
| `ascii` | `Hello World` | `48656C6C` | 基线 ASCII |
| `mixed` | `中文-αβγ-Hello-🎯` | `E4B8AD` | 混合 |
| `edge` | `Test"O'Brien & <tag>` | — | SQL 安全字符 |

### 2.1 乱码标记字节

```
EFBFBD = U+FFFD (Unicode replacement character "�")
```

**任何字段 HEX 含 `EFBFBD` 必须视为编码污染,测试失败**。这是 `helpers/db.ts:assertNoMojibake()` 的核心断言。

---

## 3. 业务模块测试数据生成器

### 3.1 Project

```ts
makeProjectData(suffix?: string)
→ {
    projectName: `E2E 测试项目-${tag}`,    // 含中文,触发编码链路
    projectType: 'rnd',
    managerUserId: 1,
    startDate: '2026-05-16',
    endDate: '2026-12-31',
    budget: 100.5,
    description: `E2E 自动化测试-${tag} αβγ`
  }
```

### 3.2 Requirement

```ts
makeRequirementData(projectId, suffix?)
→ {
    projectId,
    title: `E2E 需求-${tag}`,
    description: `自动测试需求描述 ${tag}`,
    source: '01',          // 客户反馈
    priority: '01'         // P1 重要
  }
```

### 3.3 Sprint

```ts
makeSprintData(projectId, suffix?)
→ {
    projectId,
    name: `E2E Sprint-${tag}`,
    goal: `自动化测试目标 ${tag}`,
    plannedStartDate: '2026-05-16',
    plannedEndDate: '2026-05-29'   // +14 天默认
  }
```

### 3.4 Task

```ts
makeTaskData(projectId, sprintId?, requirementId?, suffix?)
→ {
    projectId,
    sprintId,
    requirementId,
    title: `E2E 任务-${tag}`,
    description: `自动测试 ${tag}`,
    priority: '02',
    assigneeUserId: 1,
    estimatedHours: 2.0
  }
```

---

## 4. 状态机转换矩阵

### 4.1 Project (5×5)

```
PROJECT_STATUS_TRANSITIONS.legal   = [0→1, 1→2, 2→1, 1→3, 1→4]
PROJECT_STATUS_TRANSITIONS.illegal = [0→3, 3→1, 4→0]
```

### 4.2 Requirement (4×4)

```
REQUIREMENT_STATUS_TRANSITIONS.legal:
  00→01 (待评审→开发中)
  01→00 (反向打回!)
  01→02 (完成)
  00→03 (取消)

REQUIREMENT_STATUS_TRANSITIONS.illegal:
  00→02 (跨级)
  02→01 (终态保护)
```

### 4.3 Sprint (4×4)

```
SPRINT_STATUS_TRANSITIONS.legal:
  00→01, 01→02, 01→03

SPRINT_STATUS_TRANSITIONS.illegal:
  00→02, 02→01
```

### 4.4 Task (6×6 含反向边)

```
TASK_STATUS_TRANSITIONS.legal:
  00→01 (待开发→开发中)
  01→02 (开发中→代码评审)
  02→01 (反向:评审打回)
  02→03 (评审→测试)
  03→02 (反向:测试打回)
  03→04 (测试→完成)

TASK_STATUS_TRANSITIONS.illegal:
  00→02 / 00→03 (跨级)
  04→01 (终态保护)
```

---

## 5. 错误码对照表

```ts
ERROR_CODES = {
  ENCODING_OK: 200,
  STATUS_VIOLATION: 601,        // 状态机非法
  REQUIRED_FIELD: 602,          // 必填字段空
  FIELD_FORMAT: 604,            // 格式不合法 (MR URL / dict value)
  NO_UNIQUE: 701,               // 唯一约束冲突
  FK_NOT_EXISTS: 702,           // FK 不存在
  SPRINT_SINGLE_ACTIVE: 703,    // 业务硬规则: 项目级单一活跃迭代
  SPRINT_HAS_TASKS: 704         // Sprint 下有 task 不可删
}
```

---

## 6. 数据生命周期示例

### 6.1 Sprint 模块测试的 Suite 级数据

```
beforeAll
  ↓
1. 建 Project (id=auto, name="E2E 测试项目-spr-suite-{RUN_ID}")
   ↓
2. 测试 1-6 各自:
   - 建自己的 Sprint(s)
   - 验证业务逻辑
   - (部分测试 cleanup 自己的 sprint)
   ↓
afterAll
  ↓
3. execDelete tb_sprint WHERE project_id={projectId}
4. execDelete tb_project WHERE id={projectId}
5. apiRequest.dispose()
```

### 6.2 cross-test 共享 vs 隔离

| 资源 | 隔离粒度 | 原因 |
|---|---|---|
| Project (suite-level FK) | 整个 spec 文件共享 | 减少 setup 开销;非主测对象 |
| Sprint/Task/Requirement | 每测试独立 | 状态机操作互不干扰 |
| RUN_ID 后缀 | 全局唯一 | 并发安全 |

---

## 7. 客户端编码坑速查

### 7.1 ❌ 错误做法

```bash
# Windows MSYS bash 内联 -d, 会被 GBK 转换污染 args
curl -d '{"title":"测试"}' http://...
```

### 7.2 ✅ 正确做法

```bash
# 写文件后 --data-binary @file
cat > /tmp/body.json <<'EOF'
{"title":"测试"}
EOF
curl --data-binary @/tmp/body.json http://...
```

```ts
// Playwright APIRequestContext 不受 shell 编码影响 — UTF-8 字符串直接传
await api.post('/business/project', { projectName: '测试' })
```

---

## 8. 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-16 | 首次创建,与 fixtures.ts v1.0 对齐 |
