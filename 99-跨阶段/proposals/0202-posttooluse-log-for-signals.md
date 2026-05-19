# Proposal 0202: PostToolUse 日志接入 — Phase D v0.3 signals Type 5 Claude 行为基础

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0202 |
| 标题 | 加 PostToolUse hook 写 `.claude/logs/tools/YYYY-MM-DD.log`, signals-collect Type 5 grep 统计 |
| 状态 | **merged → tracking** (solo same-day per [0040](0040-self-evolution-v2-meta-rules.md) §3.5) |
| 类型 | 工具链 (0200-0299) |
| 提出人 | Wjl + Claude (用户 auto-mode 继续, Phase D 路线推进) |
| 提出日期 | 2026-05-19 |
| 评审日期 | 2026-05-19 `[solo-review-3conditions-early-dev]` (per [0007](0007-phase05-maturity-parametrization.md) §B 表) |
| Tracking 截止 | 2026-06-02 |

---

## 1. 背景

[signals-collect v0.2](../../.claude/skills/signals-collect/SKILL.md) 完成 7 类信号采集中的 6 类, 留 **Type 5 Claude 行为** 一直 N/A:

```
| claude_block_count | N/A (待 Phase D v0.3 PostToolUse hook 接入) |
| claude_override_count | N/A (同上) |
```

per [signals/README.md §Type 5](../signals/README.md), Phase D 终态 (auto-suggest MUST↔SHOULD 升降) 需要"Claude 在规则约束下的行为分布"作为输入。当前完全缺数据。

**v0.3 目标**: 把 Claude 的 tool 调用机械化日志, signals-collect 从中提取衍生指标 (而非真"block / override" — 那需要语义判断, 留 v0.4)。

---

## 2. 证据

- [self-evolution.md §5 Phase D 进度表](../self-evolution.md) 2/4 → 3/4
- [signals/README.md §Type 5](../signals/README.md) 字段定义已存在但无数据源
- 用户 auto-mode 继续推进 Phase D 路线 (2026-05-19, commit 609fd0e 完成 4×4 agent skill 矩阵后)
- [proposal 0040 §3.1](0040-self-evolution-v2-meta-rules.md) "写前 Read": 已 Read .claude/settings.json (52 行, 5 hook 完整)
- [proposal 0041 §3.1](0041-meta-rule-grep-existing-code.md) 第 4 checkbox `.claude/settings.json` 是 JSON 配置不是代码, 不适用 grep 现存字段; 但需 python JSON 验证语法

---

## 3. 提案

### 3.1 .claude/settings.json 加 PostToolUse hook

```diff
     "UserPromptSubmit": [
       { ... 不变 ... }
-    ]
+    ],
+
+    "PostToolUse": [
+      {
+        "_comment": "Phase D v0.3 (proposal 0202): tool-use 日志, 供 signals-collect Type 5 Claude 行为分析. 日志 .claude/logs/tools/YYYY-MM-DD.log gitignored. 仅记录 tool name + 截断 80 字符的 excerpt + 时间戳; 不写 CLAUDE_TOOL_INPUT_content (避免敏感数据).",
+        "matcher": "",
+        "hooks": [
+          {
+            "type": "command",
+            "command": "LOG_DIR=.claude/logs/tools; mkdir -p \"$LOG_DIR\"; LOG_FILE=\"$LOG_DIR/$(date +%Y-%m-%d).log\"; TS=$(date +%Y-%m-%dT%H:%M:%S); TOOL=\"$CLAUDE_TOOL_NAME\"; case \"$TOOL\" in Bash) EXCERPT=$(echo \"$CLAUDE_TOOL_INPUT_command\" | head -c 80 | tr '\\n' ' ' | tr '\\t' ' ') ;; Edit|Write|Read|NotebookEdit) EXCERPT=$(echo \"$CLAUDE_TOOL_INPUT_file_path\" | head -c 80) ;; Agent) EXCERPT=\"subagent=$CLAUDE_TOOL_INPUT_subagent_type\" ;; Skill) EXCERPT=\"skill=$CLAUDE_TOOL_INPUT_skill\" ;; Grep) EXCERPT=$(echo \"$CLAUDE_TOOL_INPUT_pattern\" | head -c 80 | tr '\\n' ' ') ;; *) EXCERPT=\"\" ;; esac; printf '%s\\t%s\\t%s\\n' \"$TS\" \"$TOOL\" \"$EXCERPT\" >> \"$LOG_FILE\" 2>/dev/null; exit 0"
+          }
+        ]
+      }
+    ]
```

### 3.2 .gitignore 加 logs 排除

```diff
 # Claude Code session data — ignore everything under .claude/ EXCEPT shared rule/playbook files
 .claude/*
 !.claude/*.md
 !.claude/settings.json
+# Phase D v0.3 起 .claude/logs/ 是本地 tool-use 日志, 不入 git (signals-collect 月底读后归档)
+.claude/logs/
```

### 3.3 signals-collect Type 5 query 升级

`references/queries.md` Type 5 段从 N/A 升级为:

```bash
LOG_DIR=".claude/logs/tools"
# 5.1 当窗内 tool 总调用
TOOL_TOTAL=$(find "$LOG_DIR" -name "*.log" -newermt "$WINDOW_START" ! -newermt "$WINDOW_END_PLUS_1" 2>/dev/null \
    | xargs cat 2>/dev/null | wc -l)
# 5.2 tool 类型分布
TOOLS_BY_TYPE=$(find "$LOG_DIR" -name "*.log" -newermt "$WINDOW_START" 2>/dev/null \
    | xargs cat 2>/dev/null | awk -F'\t' '{print $2}' | sort | uniq -c | sort -rn)
# 5.3 --no-verify Bash 调用 (Real-world "override" 信号)
NO_VERIFY=$(find "$LOG_DIR" -name "*.log" -newermt "$WINDOW_START" 2>/dev/null \
    | xargs cat 2>/dev/null | awk -F'\t' '$2=="Bash" {print $3}' | grep -c "no-verify" )
# 5.4 Edit/Write 到 canonical 规范文件 (Per PreToolUse hint 提示后仍编辑 = override)
EDIT_CANONICAL=$(find "$LOG_DIR" -name "*.log" -newermt "$WINDOW_START" 2>/dev/null \
    | xargs cat 2>/dev/null | awk -F'\t' '$2=="Edit" || $2=="Write" {print $3}' \
    | grep -cE "rules\.md|开发规范\.md|Phase[0-9]{2}-.*-Gate\.md|proposals/0000-template\.md|settings\.json")
# 5.5 Subagent 使用分布
AGENT_USAGE=$(find "$LOG_DIR" -name "*.log" -newermt "$WINDOW_START" 2>/dev/null \
    | xargs cat 2>/dev/null | awk -F'\t' '$2=="Agent" {print $3}' | sort | uniq -c | sort -rn)
```

---

## 4. 影响面

| 受众 | 影响 |
|---|---|
| Claude (会话起点) | 每个 tool call 后 ~5ms hook 延迟; 日志写盘 |
| 用户 | 不感知; 日志 gitignored, 不污染仓库 |
| 自进化机制 | Type 5 字段从 N/A → 有数; signals-collect v0.3 输入闭环 |
| 已 merged 的 0200/0201 (hooks) | 不冲突, 本提案只加 PostToolUse 不改 PreToolUse/UserPromptSubmit |
| 磁盘 | 估每 hook 50 字节 × 1000 tool/day = 50KB/day; 月 1.5 MB; 季度 ~5 MB (可接受) |

---

## 5. 风险

- 风险 1: 敏感数据进 log (e.g. `git config api_key=xxx` 的 commit 命令)。
  **缓解**: excerpt 截断 80 字符 + 仅记 file_path / command 头, 不记 content / output。Bash command 含 secret 的概率低 (因为 secret 通常通过 env var 或 .env, 而非命令行)。
- 风险 2: 高频 tool 调用拖慢 hook (每 PostToolUse 启 bash 子进程)。
  **缓解**: 命令使用 `printf` + `>> $LOG_FILE 2>/dev/null` 写盘极快 (< 5ms); 不调用网络 / 不阻塞。
- 风险 3: 日志增长无限 / 占用磁盘。
  **缓解**: 按日分文件 (YYYY-MM-DD.log) + signals-collect 月底读后可归档; 季度初手动 `rm .claude/logs/tools/2025-*.log`。
- 风险 4: 错误的 hook 命令导致 Claude 会话异常。
  **缓解**: `2>/dev/null; exit 0` 兜底; hook 任何失败都不阻断 Claude; settings.json JSON 已 python json.load 验证。

---

## 6. 备选方案

- **A**: 独立 .sh 文件 (e.g. `.claude/hooks/post-tool.sh`) 而非 inline command — 不选, 与 0200/0201 现状 inline 模式一致优先
- **B**: JSON Lines 格式而非 TSV — 不选, 简化 grep / awk 解析, TSV 足够
- **C**: 加入 duration / status 字段 — 不选 v0.3, PostToolUse 不天然有 duration; 留 v0.4 如需扩展
- **D** (选定): TSV 3 列 (timestamp, tool, excerpt), 截断 80 字符, gitignored, 按日分文件

---

## 7. 实施计划

```
[x] Step 1: 写 proposal (本文件)
[x] Step 2: 评审 — 2026-05-19 [solo-review-3conditions-early-dev]
[x] Step 3: 落地 (per 0040 §3.1 先 Read settings.json):
    - .claude/settings.json 加 PostToolUse hook block
    - .gitignore 加 .claude/logs/
[x] Step 4: python json.load 验证 settings.json 合法
[ ] Step 5: signals-collect references/queries.md Type 5 段升级 (本 commit 内)
[ ] Step 6: dogfood — 当前 session 后续 tool calls 应写入 today 日志
[ ] Step 7: tracking 期看 (W22+) — 信号采集是否真起到判断层输入作用
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| Type 5 字段 N/A → 实数据 | 100% N/A | < 10% N/A (Bash + Edit + Write + Agent 全有数) |
| 日志写盘成功率 (per session) | N/A | ≥ 99% (容错 2>/dev/null) |
| 月度日志大小 | N/A | < 5 MB / 月 (8 GB/年 上限, 远低于) |
| 敏感数据泄露 / 误报 | 0 | 0 (excerpt 80 字符 + 不记 content) |

Tracking 期: 2026-05-19 ~ 2026-06-02。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl `[solo-review-3conditions-early-dev]` | ✅ 通过 | 2026-05-19 | 工具链类; 与已 merged 的 0200/0201 兼容; 加 PostToolUse 不引入新 hook 设计; 风险已枚举且缓解; 推进 Phase D 路线 |
| Claude | ✅ 实施 | 2026-05-19 | 按 0040 §3.1 已 Read .claude/settings.json (52 行); 按 0041 §3.1 第 4 checkbox JSON 配置不适用 grep; 已 python json.load 验证 valid |

> Solo 单签理由 (per [0007 §B](0007-phase05-maturity-parametrization.md) early × solo 3 条件):
> 1. ✅ 用户明确请求 / auto-mode continue (Phase D 路线推进)
> 2. ✅ 改动单文件 + 配套 .gitignore + queries.md (机械, 不涉业务逻辑)
> 3. ✅ 风险可逆 (删 PostToolUse block 即恢复, 无数据迁移)

---

## 10. 实施后跟踪 (已 merged)

### 实际合入
- 合入 commit: 待回填
- 实际 merged 日期: 2026-05-19

### Tracking 数据

| 信号 | 基线 | 目标 | W22 | W23 |
|---|---|---|---|---|
| Type 5 N/A 比例 | 100% | < 10% | 待填 | 待填 |
| 日志写盘成功率 | N/A | ≥ 99% | 待填 | 待填 |
| 月度日志大小 (MB) | 0 | < 5 | 待填 | 待填 |
| 敏感数据泄露次数 | 0 | 0 | 待填 | 待填 |
| signals 月报 Type 5 段实际填充字段数 | 0 / 5 | ≥ 4 / 5 (5月底首测) | — | — |

### 跟踪结果 (待 W23 末填)

待填: 跟踪期完后, 复盘是否目标达成 / 是否需要 v0.3.1 调整 (e.g. log 字段加 duration)。

---

## 11. 与其他 proposal 关系

- 上游: [0040 v2 元规则](0040-self-evolution-v2-meta-rules.md) §3.1 写前 Read — 本提案落地前已 Read settings.json
- 上游: [0041 grep 现存代码](0041-meta-rule-grep-existing-code.md) §3.1 第 4 checkbox — JSON 配置文件不适用 grep, 但已 python json.load 验证
- 上游: [0201 hooks 流程控制](0201-hooks-self-evolution-flow-control.md) — 增 PostToolUse, 不改 PreToolUse/UserPromptSubmit
- 下游: [signals-collect v0.3 SKILL.md](../../.claude/skills/signals-collect/SKILL.md) Type 5 query 升级
- 后续: v0.4 (判断层) 会基于本 hook 写入的日志推断 "block / override" 模式

---

## 12. 历史

| 日期 | 版本 | 改了什么 |
|---|---|---|
| 2026-05-19 | v0.1 | 首版; PostToolUse hook 接入 + signals Type 5 query 升级; Phase D v0.3 落地; solo + same-day merged |
