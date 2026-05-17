# Proposal 0200: 编码自检脚本接入 PreToolUse hook + git pre-commit 钩子

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0200（首个工具链类提案）|
| 标题 | 把 check-encoding.sh / check-encoding-runtime.sh 从手工运行升级为自动化 hook，杜绝乱码事故复发 |
| 状态 | **merged → tracking** (check-encoding.sh staged 优化延后 BL-2026-009) |
| 类型 | 工具链 |
| 提出人 | Wjl + Claude（reflect/2026-W21 批量升格）|
| 提出日期 | 2026-05-17 |
| 来源 | signals 候选 **0030**（派生自 0028 的运维提升）|
| 评审截止 | 2026-05-21（工具链 2 工作日内）|
| Tracking 截止 | merged 后 2 周 |

---

## 1. 背景

[Proposal 0028](0028-encoding-runtime-hardrules.md) 落地了静态规则 + 2 个检测脚本。但脚本"手工运行"等于"靠记得跑"——周六事故后 W21+ 谁记得跑？

[Signals 候选 0030] 提议把 check-encoding 系列纳入 PreToolUse hook 自动化。本提案细化为 2 层防御：

| 层 | 脚本 | 触发时机 | 失败行为 |
|---|---|---|---|
| **L1 静态** | `plm-backend/scripts/check-encoding.sh` | 1) git pre-commit hook 拒收 2) Claude PreToolUse `Bash` matcher 检测 `git commit` 时提示 | 拒绝 commit |
| **L2 运行期** | `plm-backend/scripts/check-encoding-runtime.sh` | 1) Phase 05 §F 上线后验证必填项 2) 后端启动 health check 加 hex 探针端点（可选）| Phase 05 Gate §I 写"未通过原因" → 拒绝准出 |

---

## 2. 证据

- 关联 signals: [2026-05.md](../signals/2026-05.md) 候选 0030（派生自 0028）
- 关联 reflect: [reflect/2026-W20.md](../reflect/2026-W20.md) B2 W21 批量
- 关联 proposal: [0028](0028-encoding-runtime-hardrules.md) Tracking §10 "[ ] 把 check-encoding-runtime.sh 纳入 PreToolUse hook — 留独立 proposal 0030"
- 实际事故: 2026-05-16 周六 EFBFBD 入库事故（已修复）

---

## 3. 提案

### 3.1 加 `.githooks/pre-commit` 钩子（新文件）

```bash
#!/usr/bin/env bash
# 静态编码自检 — 拒绝 commit 时引入非 UTF-8 / BOM 文件
set -e
exec bash "$(git rev-parse --show-toplevel)/plm-backend/scripts/check-encoding.sh"
```

CLAUDE.md 已说"First-time setup per clone: `git config core.hooksPath .githooks`" → 用户已经走过这一步，新钩子自动生效。

### 3.2 改 `.claude/settings.json` PreToolUse 段

```diff
       {
         "matcher": "Bash",
         "hooks": [
           {
             "type": "command",
-            "command": "command=\"$CLAUDE_TOOL_INPUT_command\"; case \"$command\" in *'DROP DATABASE'*|*'DROP TABLE'*|...
+            "command": "command=\"$CLAUDE_TOOL_INPUT_command\"; case \"$command\" in *'git commit'*) bash plm-backend/scripts/check-encoding.sh >&2 || { echo '❌ check-encoding.sh failed — fix encoding issues before commit' >&2; exit 1; } ;; *'DROP DATABASE'*|*'DROP TABLE'*|...
           }
         ]
       },
```

（具体实现：将 `git commit` 分支放在 case 第一个，独立逻辑；其它高危命令保留原行为）

### 3.3 改 `99-跨阶段/gate-checklists/Phase05-上线-Gate.md` §F

```diff
 ## F. 上线后验证（强制，观察期长度按 项目成熟度 — proposal 0006）

 - [ ] 关键路径 5 个用例已手动 / 自动跑通
+- [ ] **编码运行期自检（proposal 0200）**：`bash plm-backend/scripts/check-encoding-runtime.sh <DB_PWD>` 输出含 `[DB] HEX=...` 且不含 `EFBFBD` → 截图/日志贴本节
 - [ ] 监控看板 / 日志全绿，观察期按 项目成熟度：
```

### 3.4 文档化

- 加 `03-开发/字符编码规范.md` "自动化层级" 段（如已有则更新）
- CLAUDE.md "Gotchas" 表保留 1 行，链到本 proposal

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| 开发者 commit 流程 | pre-commit 增几秒（check-encoding.sh 平均 1-2s）；遇 BOM / 非 UTF-8 必修后才能 commit |
| Claude | PreToolUse hook 在 Bash `git commit` 前跑检测；用户 force-bypass 时回到 `--no-verify` 路径（仍记入 signals）|
| Phase 05 上线 | §F 多 1 项必填，可在 dev 环境绕过 per 0009（dev 不强制凭据 → runtime 检测 N/A）|
| CI | 未启用 CI 暂不涉及；将来加 GitHub Actions 时把同一脚本作为 job |

---

## 5. 风险

- **风险 1**: pre-commit 慢 / 偶尔 false positive 让开发者抓狂 → `--no-verify`。**缓解**: 脚本只扫 staged files（用 `git diff --cached --name-only`），不扫全仓库；signals 自动监控 `--no-verify` 次数。
- **风险 2**: check-encoding-runtime.sh 失败方式不友好（直接 exit 1）。**缓解**: 脚本完善 error message 指向 03-开发/字符编码规范.md 复盘段。
- **风险 3**: Phase 05 §F 加项让 Phase 05 模板更重。**缓解**: dev 环境（per 0009）可豁免，stable+ 才强制。

---

## 6. 备选方案

- A: 不加自动化，靠 reflect 监控复发率 — 不选，事后发现成本高
- B: 用 Claude UserPromptSubmit hook 在用户说 "commit" 时提醒 — 不选，太被动
- C（选定）: pre-commit 强制 + Phase 05 §F 强制 + Claude PreToolUse 提示

---

## 7. 实施计划

```
[x] Step 1: 写 proposal
[x] Step 2: 评审 — 2026-05-17 [solo-review] (DevOps 角色 = Wjl)
[ ] Step 3: 优化 check-encoding.sh 只扫 staged files → BL-2026-009 (W22, P2, 性能优化)
[x] Step 4: 加 .githooks/pre-commit — 2026-05-17 (调全仓库扫描版本)
[x] Step 5: 改 .claude/settings.json PreToolUse Bash matcher 加 git commit 提示 — 2026-05-17
[x] Step 6: 改 Phase05-上线-Gate.md §F 加"编码运行期自检"必填 — 2026-05-17
[ ] Step 7: tracking 期看 commit_bypass_count + 编码事故复发
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| 编码事故复发次数（含 EFBFBD / BOM）| 1（W20 周六事故 → 0 复发） | 0 |
| `commit_bypass_count` (`--no-verify`) | 0 | ≤ 1 / 月（pre-commit 加严后允许偶尔豁免）|
| Phase 05 §F.编码运行期检测 通过率 | N/A | 100%（每次 Phase 05 必通过） |
| pre-commit 平均耗时 | N/A | < 3s |

Tracking 期: merged 后 2 周。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl `[solo-review]` | ✅ 通过 | 2026-05-17 | 工具链类首落地; 高敏感 (改 .claude/settings.json 影响每次会话); 已有 0028 事故根治为先例, 本提案是事故防御自动化 |
| Claude | ✅ 实施 | 2026-05-17 | 按 0040 §3.1 先 Read settings.json/pre-commit/Phase05 §F 当前文本; pre-commit 走全仓库扫描 (慢但可靠), staged 优化派生 BL-2026-009 |

> Solo 单签理由：0028 事故已修, 0200 是把"事故防御从手工 commit-msg hook 扩展到 pre-commit + Claude 提示 + Phase 05 §F 必填"——三层防御对应已发生事故的根因, 不存在评审能找出新发现的可能。pre-commit 全仓库扫描的性能问题靠 BL-2026-009 W22 优化, 不阻塞规范本身。

---

## 10. 实施后跟踪（已 merged）

### 实际合入
- 合入 commit: 待 commit 后回填
- 实际 merged 日期：2026-05-17

### 实际改动
- `.githooks/pre-commit` (新建, +24 行 bash)
- `.claude/settings.json` PreToolUse Bash matcher 加 `git commit` case (提示式, 不阻断)
- `99-跨阶段/gate-checklists/Phase05-上线-Gate.md` §F 加"编码运行期自检"必填项 + early/dev 挂账路径

### 派生迁移项

| BL ID | 任务 | 优先级 | 估期 |
|---|---|---|---|
| ~~BL-2026-009~~ | ✅ **已完成 2026-05-17**：`check-encoding.sh` 加 `--staged` 模式 + pre-commit hook 切到 --staged + 同次修 `-A2→-A5` 检测漏洞 | ~~P2~~ | ~~S~~ | Wjl+Claude |

### Tracking 数据

| 信号 | 基线 | 目标 | W21 (本次) | W22 | W23 |
|---|---|---|---|---|---|
| 编码事故复发次数 (EFBFBD/BOM) | 1 (W20 周六事故 → 0 复发) | 0 | 已建多层防御 | 待填 | 待填 |
| `commit_bypass_count` (`--no-verify`) | 0 (W19-W21) | ≤ 1 / 月 | 0 | 待填 | 待填 |
| Phase 05 §F.编码运行期检测 通过率 | N/A | 100% (新 instance) | 0 (尚无新 Phase 05 实例) | 待填 | 待填 |
| pre-commit 平均耗时 | N/A | < 3s (BL-2026-009 优化后) | 当前未优化, 估全仓库扫 5-10s | 待填 | 待填 |

Tracking 期: 2026-05-17 ~ 2026-05-31。

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首版从 signals 候选 0030 升格；首个 0200-0299 工具链类 proposal |
| 2026-05-17 | Wjl `[solo-review]` + Claude | 同日 solo-review accept + 落地 per 0040 §3.5: 新建 .githooks/pre-commit + 改 .claude/settings.json PreToolUse 加 git commit 提示 + Phase05-上线-Gate.md §F 加编码运行期自检必填 (early+dev 可挂账, 同 §C 凭据路径)。check-encoding.sh staged 优化 → BL-2026-009 (W22)。状态 proposed → merged → tracking |
