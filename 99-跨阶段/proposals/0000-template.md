# Proposal NNNN: <一句话标题>

> 复制本文件为 `NNNN-<kebab-标题>.md` 开新提案。
> 编号查 [README.md](README.md) 的状态索引表，递增即可。

---

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | NNNN |
| 标题 | |
| 状态 | draft / proposed / accepted / implementing / merged / tracking / done / rejected / superseded |
| 类型 | 流程 / 编码规范 / 工具链 / 架构 / 实验 |
| 提出人 | _(人名 / Claude /reflect)_ |
| 提出日期 | YYYY-MM-DD |
| 评审人 | _(职责矩阵见 README §评审节奏)_ |
| 评审日期 | YYYY-MM-DD |
| Tracking 截止 | YYYY-MM-DD（merged 后 2-4 周）|

---

## 1. 背景（What's the problem?）

> 我们为什么要做这件事？描述现状的痛点。一段话，避免长篇大论。

---

## 2. 证据（Evidence）

> 数据 / 事故 / gotcha / 用户请求 任选 ≥1 个，**必填**。

- 关联 [signals/YYYY-MM.md](../signals/YYYY-MM.md)：__（具体哪行数据）
- 关联 [reflect/YYYY-WW.md](../reflect/YYYY-WW.md)：__（具体哪条建议）
- 关联 事故 / 故障复盘：__
- 关联 [gotchas](../../../.claude/skills/ruoyi-bootstrap/references/gotchas.md)：__
- 用户请求：用户在 YYYY-MM-DD 会话中明确表达 _(粘贴原话)_

---

## 3. 提案（What's the change?）

> **具体到 文件 + 行号 + diff**。不接受"我建议改进 X" 这种粒度的描述。

### 改动文件清单

| 文件 | 改动类型 |
|---|---|
| `03-开发/开发规范.md` §X | 修改 |
| `.claude/rules.md` §A.1 | 新增 |

### 写 proposal 前必填校验（proposal 0040 引入）

- [ ] 已 `Read` 上表每个目标文件的**当前完整内容**
- [ ] §3 写的"段号 / 字段名 / sub-section 编号"**逐字与目标文件当前版本一致**（不依赖记忆 / 不依赖 outdated 版本）
- [ ] Diff 草案在目标文件实际段位置**精确可应用**（没有"先重排段再改"的隐含前提）

> 若 apply 时发现 scope 错位 → 本 proposal 在 §修订记录 写"scope 修正"，并把修正过程文档化（不许悄悄改 §3 抹掉痕迹）。

### Diff 草案

```diff
--- a/03-开发/开发规范.md
+++ b/03-开发/开发规范.md
@@ -123,4 +123,5 @@
- 旧规则
+ 新规则
```

---

## 4. 影响范围（Impact）

| 受众 | 影响 |
|---|---|
| 开发者 | _(需要改习惯？需要培训？)_ |
| Claude | _(rules.md 会变，下次会话起生效)_ |
| 测试 / 运维 | |
| 已有代码 / 文档 | _(是否需要 migration？数量级？)_ |

---

## 5. 风险（Risks）

> 这条提案可能带来什么副作用？被滥用怎么办？

- 风险 1：__；缓解：__
- 风险 2：__；缓解：__

---

## 6. 备选方案（Alternatives Considered）

> 我们还想过什么方案？为什么没选？

- 方案 A：__；不选原因：__
- 方案 B：__；不选原因：__

---

## 7. 实施计划（Implementation Plan）

```
[ ] Step 1: __（who / when）
[ ] Step 2: __
[ ] Step 3: 合规范变更 PR
[ ] Step 4: 通知团队（飞书 / 周报）
[ ] Step 5: 进入 tracking 期
```

---

## 8. 衡量指标（How will we know it worked?）

> Tracking 期内观察哪些 signals 数据？怎样算"成功"？

- 信号 1：`<信号名>` 从 _基线值_ 改善为 _目标值_
- 信号 2：

跟踪期：`YYYY-MM-DD` ~ `YYYY-MM-DD`（merged 后 N 周）。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| | 通过 / 有条件通过 / 不通过 | | |

---

## 10. 实施后跟踪（merged 后填）

### 实际 PR / commit
- PR: __
- 合入 commit: __
- 实际 merged 日期：YYYY-MM-DD

### Tracking 数据

| 信号 | 基线 | 目标 | 实际（周 1）| 实际（周 2）| 实际（周 N）|
|---|---|---|---|---|---|
| | | | | | |

### 最终判定
- [ ] done（达成目标，本提案归档）
- [ ] reverted（未达成 → 走回滚 PR，并在此段写"为什么失败"）

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| | | |
