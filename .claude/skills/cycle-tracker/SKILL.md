---
name: cycle-tracker
description: PLM Phase 06 cycle 两段式签字 + 监控替代方案 + cycle 切换决议。当用户说"cycle 启动 / cycle 终态 / day 0 / day 7 / 监控替代方案 / 周期切换 / Phase 06"时调用。输出: Phase06-运营-Gate cycle instance 启动/终态段 + 决议下一步。**ops agent 的子工具** — agent §2.2 触发。
---

# cycle-tracker — Phase 06 cycle 管理 skill v0.1

**ops agent 的子工具**, 主走 §2.2 Phase 06 运营 cycle 两段式签字职责。

per [proposal 0012](../../99-跨阶段/proposals/0012-phase06-two-stage-signoff.md) 两段式签字 + [proposal 0010](../../99-跨阶段/proposals/0010-phase06-substrate-only-metrics.md) substrate-only 监控。

---

## 1. 何时调用

- 用户说 "cycle 启动 / 终态 / day 0 / day 7"
- ops agent §2.2 触发
- Phase 06 周期起止 (7 天 / 30 天 / Q1 ...)
- 监控替代方案需配置 (early 阶段)

---

## 2. 两段式签字 (per proposal 0012)

### 2.1 启动 (day 0)

cycle 开始当日:

```markdown
# Phase 06-运营-Gate cycle{N} kickoff @ YYYY-MM-DD

## §A 进入条件
- [x] Phase 05 已签字通过 (commit hash)
- [x] 上线 30 min 全绿
- [x] 数据看板 / 替代方案表已配 (per §B)

## §C 项目状态
- maturity: early / stable / mature
- 团队规模: solo / small / medium / large
- 关联上线版本: vX.Y.Z

## §I.1 启动签字
| 角色 | 姓名 | 评审 | 日期 |
|---|---|---|---|
| 运营 / 产品 lead | <Wjl [solo-review-3conditions-early-dev]> | ✅ cycle N 已启动 | YYYY-MM-DD |
| 开发 lead | Wjl | ✅ 已部署且可访问 | YYYY-MM-DD |

commit `docs(gate): <module> phase 06 cycle N kickoff`
```

### 2.2 终态 (day N, 通常 7/30/Q1)

cycle 满后:

```markdown
## §B 监控 (per maturity)
- stable+: 5 指标看板 + 告警接收人 + 阈值
- early (substrate-only, per proposal 0010): 观察手段表 (≥ 5 项) + 升级路径

## §C-§G 完成情况
- §C 周报月报齐
- §D 用户反馈 N 条已分类
- §E A/B 测试 (如做) 结论
- §F P0/P1 缺陷 100% 处置
- §G OKR 对照 (per proposal 0011 solo+early 可 N/A)

## §I.2 终态签字
(完整签字阵列, per 团队规模)

## §K 决议下一步 (4 选 1)
- [ ] 继续运营 → 下个 cycle 启动占位
- [ ] 小修小补 → 开 L3 改动
- [ ] 大改进 → 回 Phase 01 立项 (转 product-manager)
- [ ] 下线 → 走退役流程 (per ops §2.6)

commit `docs(gate): <module> phase 06 cycle N closure`
```

---

## 3. 监控替代方案表 (per proposal 0010, early 阶段)

```markdown
## §B.substrate-only — Phase 06 监控替代

| # | 观察手段 | 触发响应条件 |
|---|---|---|
| 1 | 手动 curl healthcheck | 5xx 出现 → 立即查 backend log |
| 2 | journalctl 后端日志 | ERROR 堆栈 → 评估是否回滚 |
| 3 | E2E 套件每日跑 | 全套件 fail → 阻断下一 cycle |
| 4 | 数据库慢查询 EXPLAIN | > 1s 查询 → 列入下个 Sprint 优化 |
| 5 | DB HEX 字符抽样 | 含 EFBFBD → P0 编码事故 |

升级路径: 转 `stable` 时本段失效, 必须补齐正式看板。
```

---

## 4. cycle 切换决议工作流 (§K)

```
[Step 1] 评估当前 cycle 数据
  - §C 完成度
  - §F 缺陷处置率
  - §G OKR 偏差 (或 N/A 状态)

[Step 2] AskUserQuestion 决议
  Q: "Cycle N 结束, 下一步?"
  Options:
    - 继续运营 (再来一轮)
    - 小修小补 (L3, 走 Phase 03 简化)
    - 大改进 (回 Phase 01, 转 product-manager)
    - 下线 (走 ops §2.6 退役)

[Step 3] 执行决议
  - 继续 → cycle-tracker 起新 instance (day 0)
  - 小修小补 → 协调 backend-coder
  - 大改进 → 转 product-manager (新 PRD 流程)
  - 下线 → 走 ops §2.6 6 步退役

[Step 4] commit + 更新模块工作流总览
```

---

## 5. 衔接

| 上游 | cycle-tracker | 下游 |
|---|---|---|
| ops §2.1 Phase 05 完成 | → cycle 1 启动 | → reflect-monthly tracking 终结 |
| signals-collect Phase 06 数据 | → §C-§G 填值 | → product-manager (路线图 / 退役决策) |
| defect-triage P0/P1 计数 | → §F 处置率 | → tester (复测复发) |

---

## 6. 反模式

- ❌ 仅写终态段, 跳过启动 (per proposal 0012 必两段)
- ❌ §I.1 启动签字 写 "✅" 但未注 [solo-review-3conditions-early-dev] (per proposal 0007)
- ❌ §B 监控 standard 路径但实际无 (early 应走 substrate-only)
- ❌ §G OKR 强填 N/A 而 团队 OKR.md 顶部无声明 (per proposal 0011)
- ❌ §K 决议跳过 (cycle 末必决议下一步)
- ❌ P0/P1 缺陷未清就进下个 cycle (违反 ops §1.4)
- ❌ 终态签字 day 7 时间错 (与 day 0 同日 = 假数据)

---

## 7. 历史

| v0.1 | 2026-05-19 | 首版; ops 配套 4 skill 之四 (终); 两段式签字 + substrate-only 监控 + §K 4 选 1 决议 |
