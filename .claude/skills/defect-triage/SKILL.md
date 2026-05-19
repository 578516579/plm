---
name: defect-triage
description: PLM 缺陷生命周期管理 — 复现 / 分类 / 分配 / 验证 / 关闭 6 步走, 与 plm-defect 模块 5×5 状态机协作。当用户说"报缺陷 / 复现 bug / 缺陷分类 / triage / defect lifecycle / P0/P1 缺陷 / 缺陷复发"时调用。输出: plm-defect record (DEF-YYYY-NNNN) + 04-测试/缺陷复盘/<DEF-NN>.md (如适用)。**tester agent 的子工具** — agent §2.5 触发。
---

# defect-triage — 缺陷分类与生命周期 skill v0.1

**tester agent 的子工具**, 主走 §2.5 缺陷生命周期管理职责。

与 plm-defect 模块 5×5 状态机协作 (per ADR-0005), 走完 6 步: 创建 → 分类 → 复现 → 分配 → 验证 → 关闭。

---

## 1. 何时调用

- 用户说 "报缺陷 / 复现 bug / 这是 P0 吗 / 缺陷怎么分类"
- tester agent §2.5 触发
- Phase 04 测试发现 fail / Phase 06 用户反馈
- 反思发现复发缺陷 → 走流程反思

---

## 2. 6 步流程 (与 plm-defect 5×5 状态机协作)

### Step 1: 创建 (state=draft)

POST `/business/defect` body:

```json
{
  "title": "<1 句话 — 用户看得懂>",
  "projectId": <FK>,
  "discoveredBy": "<userId>",
  "severity": "P0/P1/P2/P3",
  "type": "功能/性能/UI/兼容/编码/安全",
  "status": "draft"
}
```

### Step 2: 分类 (severity × type 二维)

**Severity** (业务影响):
- **P0** 阻断 release / 数据损坏 / 安全漏洞 — 立即 hotfix
- **P1** 核心功能不可用 — 当前 Sprint 修
- **P2** 边缘场景 / UI 错位 — 下个 Sprint
- **P3** 微小瑕疵 / 文案 — 列 backlog 有空再做

**Type** (根因领域):
- 功能 (业务逻辑错)
- 性能 (响应慢 / OOM)
- UI (布局错位 / 文案错)
- 兼容 (浏览器 / 移动端)
- 编码 (EFBFBD / 乱码, per rules.md §D)
- 安全 (权限绕过 / SQL 注入 / URL 钓鱼)

Severity 决定 SLA, type 决定分配域。

### Step 3: 复现 (state=reviewing)

记录复现步骤 (必须可重现):

```markdown
**前置**: <环境 / 数据 / 角色>
**步骤**:
  1. <用户动作>
  2. <用户动作>
  3. ...
**期望**: <PRD 验收标准>
**实际**: <错误现象 + 截图 / 日志>
**复现率**: 100% / 偶发 (3 次中 1 次)
```

不可复现的 P2/P3 → 关闭标 "无法复现 + 加监控"。

### Step 4: 分配 (state=assigned, FK → assigneeUserId)

按 type 转 agent:
- 功能 / 性能 → backend-coder 或 frontend-coder
- 编码 → backend-coder (含 0028 三层防御自检)
- 安全 → security-reviewer + backend-coder
- UI → frontend-coder + design:design-critique

PUT `/business/defect/<id>` body `{ status: "assigned", assigneeUserId: <id> }`

### Step 5: 验证 (state=resolved → 测试复测)

开发修复后 → tester 复测:
- 测试用例 (TC-NNN) 重跑
- 5×5 状态机 → resolved
- 若复测失败 → 反向边 03→01 (per ADR-0005 PRD-MAPPING.md §3), 进入态必填 resolution (705 错误码)

### Step 6: 关闭 (state=closed)

最终关闭前:
- [ ] 测试用例添加到用例库 (TC-NNN 记入 04-测试/测试用例库/)
- [ ] 若 severity = P0/P1 → 写"P0/P1 缺陷根因分析" (per Phase 06 §F MUST)
- [ ] 若复发 (同 type 同模块 ≥ 2 次) → 触发流程反思 (per signals/README.md §4 → reflect 升 proposal)

---

## 3. P0/P1 根因分析模板 (Step 6 必填)

`04-测试/缺陷复盘/<DEF-NNNN>.md`:

```markdown
# DEF-YYYY-NNNN 根因分析

| 字段 | 值 |
|---|---|
| 严重度 | P0 / P1 |
| 发现时机 | Phase 04 测试 / Phase 06 cycle / 用户反馈 |
| 影响范围 | <用户数 / 模块> |
| 持续时间 | <从引入到修复> |

## 5 Whys 挖根因
- Q1: 为什么 X 发生? A1: ...
- Q2: 为什么 A1? A2: ...
- ...
- **根因 (1 句话)**

## 行动
- 修复: <commit hash>
- 防复发: 测试用例 TC-NNN + 编码规范 §X
- 升级流程: <如有, 转 proposal>
```

---

## 4. 缺陷复发触发反思

若 signals/README.md §4 `bug_recurring` > 0 → **必触发流程反思**:
- 同 type 同模块 ≥ 2 次 → 测试用例覆盖不够 → 加用例
- 同 type 跨模块 ≥ 3 次 → 系统性缺陷 → 转 proposal 升规范

---

## 5. 衔接

| 上游 | defect-triage | 下游 |
|---|---|---|
| tester quality-gate-audit 发现 fail | → 创建 defect | → backend-coder 修 |
| 用户报反馈 (Phase 06) | → 复现 + 分类 | → frontend-coder 修 UI |
| 复测失败 | → 反向边 resolved → assigned | → security-reviewer (安全类) |
| 复发 ≥ 2 次 | → 流程反思 | → reflect-monthly / proposal skill |

---

## 6. 反模式

- ❌ 不复现就 close (无法验证修复)
- ❌ severity 拍脑袋无依据 (P0 滥用 / P3 漏报)
- ❌ 关闭未记入测试用例库 (复发风险)
- ❌ P0/P1 不写根因分析 (违反 Phase 06 §F MUST)
- ❌ 复发 ≥ 2 次不触发反思 (违反 signals §4 信号)
- ❌ 直接 close 状态机反向边 (绕过 705 必填 resolution)

---

## 7. 历史

| v0.1 | 2026-05-19 | 首版; tester 配套 4 skill 之四 (终); 与 plm-defect 5×5 状态机 / ADR-0005 协作 |
