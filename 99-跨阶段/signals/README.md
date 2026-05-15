# Signals — 流程健康度信号采集

自进化机制的**输入**。每月一份，记录 7 类客观信号，作为下一轮反思 ([../reflect/](../reflect/)) 与提案 ([../proposals/](../proposals/)) 的数据依据。

> **核心信念**：规范有效性不靠"感觉"判断，靠"数据"说话。

---

## 文件命名

`YYYY-MM.md`，例如 `2026-05.md`。每月初由 `/reflect-monthly` 自动汇总上月数据生成。

人工补充信号 → 用 `YYYY-MM-supplementary.md`，避免覆盖自动生成的主文件。

---

## 7 类信号

每个月报必含以下字段（数值类，空数据填 `0`，未采集填 `N/A`）：

### 1. Commit 规范信号

| 字段 | 含义 | 来源 |
|---|---|---|
| `commit_total` | 当月 commit 总数 | `git log --since "$start" --until "$end" --oneline \| wc -l` |
| `commit_violation_count` | 不符 Conventional Commits 数（hook 拒收 + 绕过 hook） | `.githooks/commit-msg` 退出码 ≠ 0 + grep `--no-verify` |
| `commit_bypass_count` | 使用 `--no-verify` 绕过的次数 | shell history / reflog grep |

### 2. Gate Checklist 信号

| 字段 | 含义 |
|---|---|
| `gate_instances_added` | 当月 `gate-checklists/instances/**` 新增文件数 |
| `gate_skip_evidence` | 模块工作流总览显示 Phase 推进 但 instances/ 没对应实例（疑似跳过 Gate） |
| `gate_exception_filled_rate` | 实例中 "E. 异常 / 例外" 段被填写的比例（越高 → 规范越难达成） |

### 3. Phase 耗时信号

| 字段 | 含义 |
|---|---|
| `phase_avg_duration` | 每个 Phase 实际耗时（实例日期 - 上个 Phase 实例日期） |
| `phase_bottleneck` | 最长 Phase 名称 |

### 4. Bug / 缺陷复发信号

| 字段 | 含义 |
|---|---|
| `bug_total` | 当月 `fix:` commit 总数 |
| `bug_recurring` | 同一模块 / 同一类 fix 出现 ≥2 次 |
| `bug_categories` | top 3 bug 类别（按 commit message 关键词聚类） |

### 5. Claude 拒绝 / Override 信号

| 字段 | 含义 |
|---|---|
| `claude_block_count` | Claude 主动拒绝高危操作的次数 |
| `claude_override_count` | 用户强制 Claude 执行被拒操作的次数 |
| `claude_override_reasons` | 主要 override 理由分布 |

### 6. 风险信号

| 字段 | 含义 |
|---|---|
| `risks_new` | [风险登记册](../风险登记册.md) 当月新增 |
| `risks_closed` | 当月关闭 |
| `risks_open_p0_p1` | 月末开放的 P0/P1 风险数 |

### 7. OKR 进度信号

| 字段 | 含义 |
|---|---|
| `kr_on_track_pct` | 当前 KR 中"进度 ≥ 时间百分比"的占比 |
| `kr_at_risk` | 进度落后 > 20% 的 KR 列表 |

---

## 采集方式

### 自动（推荐）

由 `/reflect-monthly` skill 跑（待 Phase B 实现）。脚本读取：
- `git log` + `.githooks/commit-msg` 日志
- `gate-checklists/instances/**`
- `风险登记册.md`
- `团队 OKR.md`
- 本会话 Claude 行为日志（如果有）

### 手动（兜底）

[`月度模板`](2099-12.template.md)：本目录的模板，可复制后用人工填表。

---

## 信号 → 提案 转化路径

```
signals/2026-05.md  ── reflect/2026-W22.md  ──┐
                                                ↓
                                       proposals/<编号>-<标题>.md
                                                ↓
                                            评审 / merge
                                                ↓
                                  规范文档变更 PR（Claude 写）
```

详见 [../reflect/README.md](../reflect/README.md) 和 [../proposals/README.md](../proposals/README.md)。

---

## 数据存留

- 月度信号文件**入 git，永久保留**（作为流程演进的"化石层"）。
- 半年以前的数据用 mermaid `xychart` 在 README 顶部画一张趋势图（每季度更新一次）。

---

## 反模式

- ❌ 月报靠口算填写、没有数据来源链接
- ❌ 把 `gate_skip_evidence` 这类负面数据隐藏（造成"流程很顺"的假象）
- ❌ 数据采集了但 reflect 不消费（信号烂在档案里）
