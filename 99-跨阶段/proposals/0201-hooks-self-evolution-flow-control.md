# Proposal 0201: Hooks 自进化流程控制扩展 — 规范文件 PreToolUse warning + skill 触发 hint

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0201 |
| 标题 | 给 PreToolUse Edit\|Write + UserPromptSubmit 加自进化流程控制 case |
| 状态 | **merged → tracking** (solo same-day per [0040](0040-self-evolution-v2-meta-rules.md) §3.5) |
| 类型 | 工具链 (0200-0299) |
| 提出人 | Wjl + Claude (用户指令 "继续，增加 hook，控制流程") |
| 提出日期 | 2026-05-19 |
| Bundle | 2 个 hook 扩展 (同目标文件 `.claude/settings.json`, 同评审人) per [0040 §3.3](0040-self-evolution-v2-meta-rules.md) bundle 判据 |
| 评审日期 | 2026-05-19 `[solo-review]` |
| Tracking 截止 | 2026-06-02 |

---

## 1. 背景

当前 hooks (5 个) 覆盖:
- Stop: 会话末沉淀提示
- PreToolUse Bash: 高危命令 + git commit hint
- PreToolUse Edit|Write: gate-checklists/instances/ 已签字提醒
- UserPromptSubmit: 业务关键字 → 通用 skill 提示
- .githooks/commit-msg + pre-commit (Conventional Commits + encoding 自检)

**自进化流程层未覆盖**:
- 直接 Edit `.claude/rules.md` / `03-开发/开发规范.md` / `Phase*.md` 模板 → 应走 proposal 但无 hook 提示
- 用户说 "升格 / 反思 / 采集信号" 等关键字 → skill 可自动加载, 但用户不一定知道有 skill, hook 可提示

本 proposal 加 2 处 hook case, **不引入新 hook 类型** (轻量扩展现有 hook 的 case 分支)。

---

## 2. 证据

- 用户指令 (2026-05-19): "继续, 增加 hook, 控制流程"
- 关联 reflect: [2026-W20-tracking-audit-mid](../reflect/2026-W20-tracking-audit-mid.md) §3.1 主诊断 "merged 太宽松" — hook 提示能让"应走 proposal 但直接改"在源头被拦
- 关联 §L.2 反模式: silent merge (W20 retroactive 0027/0028/0029 都是事后才补 proposal, hook 提醒可以防发生)
- 关联 5 skill 上线 (reflect-weekly/monthly/quarterly + proposal + signals-collect), hook 应该把它们暴露给用户

---

## 3. 提案 (2 段 diff)

### 3.1 PreToolUse Edit|Write 加规范文件 case

```diff
 {
   "matcher": "Edit|Write",
   "hooks": [
     {
       "type": "command",
-      "command": "path=\"$CLAUDE_TOOL_INPUT_file_path\"; case \"$path\" in *'gate-checklists/instances/'*) echo '⚠️  EDITING SIGNED GATE CHECKLIST INSTANCE' >&2; echo '   Path: '\"$path\" >&2; echo '   These files are immutable once signed.' >&2; echo '   Add a 「修订记录」 entry instead of overwriting content.' >&2 ;; esac; exit 0"
+      "command": "path=\"$CLAUDE_TOOL_INPUT_file_path\"; case \"$path\" in *'gate-checklists/instances/'*) echo '⚠️  EDITING SIGNED GATE CHECKLIST INSTANCE' >&2; echo '   Path: '\"$path\" >&2; echo '   These files are immutable once signed.' >&2; echo '   Add a 「修订记录」 entry instead of overwriting content.' >&2 ;; *'.claude/rules.md'*|*'03-开发/开发规范.md'*|*'gate-checklists/Phase'*'-Gate.md'*|*'proposals/README.md'*|*'proposals/0000-template.md'*|*'.claude/settings.json'*) echo '⚠️  EDITING CANONICAL SPEC FILE (per rules.md §L.2)' >&2; echo '   Path: '\"$path\" >&2; echo '   Should this go through a proposal?' >&2; echo '   • If yes → create 99-跨阶段/proposals/NNNN-*.md first (or invoke /proposal Mode A)' >&2; echo '   • If solo + same-day → make sure commit will include proposal file + diff (0040 §3.5)' >&2; echo '   • If User-requested-bypass → must retroactively write proposal in same session (rules.md §L.2 例外)' >&2 ;; esac; exit 0"
     }
   ]
 },
```

### 3.2 UserPromptSubmit 加自进化 skill 触发 case

```diff
 {
   "matcher": "",
   "hooks": [
     {
       "type": "command",
-      "command": "prompt=\"$CLAUDE_USER_PROMPT\"; case \"$prompt\" in *'新功能'*|*'加功能'*|*'新模块'*|*'新业务'*|*'new feature'*|*'add module'*) echo '💡 detected intent: new business feature' >&2; echo '   Consider triggering the ruoyi-bootstrap skill Phase 7, and create the Gate Checklist instance in 99-跨阶段/gate-checklists/instances/' >&2 ;; *'上线'*|*'发布'*|*'release'*|*'deploy'*) echo '💡 detected intent: release' >&2; echo '   Phase 05 Gate must be filled: 99-跨阶段/gate-checklists/Phase05-上线-Gate.md' >&2 ;; *'回滚'*|*'rollback'*|*'revert'*) echo '💡 detected intent: rollback' >&2; echo '   Check 05-上线/Runbook.md and follow the documented rollback steps' >&2 ;; esac; exit 0"
+      "command": "prompt=\"$CLAUDE_USER_PROMPT\"; case \"$prompt\" in *'新功能'*|*'加功能'*|*'新模块'*|*'新业务'*|*'new feature'*|*'add module'*) echo '💡 detected intent: new business feature' >&2; echo '   Consider triggering the ruoyi-bootstrap skill Phase 7, and create the Gate Checklist instance in 99-跨阶段/gate-checklists/instances/' >&2 ;; *'上线'*|*'发布'*|*'release'*|*'deploy'*) echo '💡 detected intent: release' >&2; echo '   Phase 05 Gate must be filled: 99-跨阶段/gate-checklists/Phase05-上线-Gate.md' >&2 ;; *'回滚'*|*'rollback'*|*'revert'*) echo '💡 detected intent: rollback' >&2; echo '   Check 05-上线/Runbook.md and follow the documented rollback steps' >&2 ;; *'反思'*|*'反思机制'*|*'周报'*|*'/reflect-weekly'*|*'/reflect-monthly'*|*'/reflect-quarterly'*) echo '💡 detected intent: 自进化反思' >&2; echo '   skill 可自动加载: reflect-weekly / reflect-monthly / reflect-quarterly' >&2; echo '   前门文档: 99-跨阶段/self-evolution.md' >&2 ;; *'升格'*|*'lift'*|*'apply 0'*|*'/proposal'*|*'转 proposal'*|*'创建提案'*) echo '💡 detected intent: proposal lifecycle' >&2; echo '   skill 可自动加载: proposal (Mode A lift / Mode B apply / Mode C status)' >&2; echo '   决策树: .claude/skills/proposal/references/decision-tree.md' >&2 ;; *'采集信号'*|*'collect signals'*|*'signals 数据'*|*'/signals-collect'*) echo '💡 detected intent: signals 采集' >&2; echo '   skill 可自动加载: signals-collect (Phase D 输入基础设施)' >&2; echo '   输出: 99-跨阶段/signals/YYYY-MM-supplementary.md (不覆盖主文件)' >&2 ;; *'审计'*|*'audit'*|*'tracking'*) echo '💡 detected intent: tracking / audit' >&2; echo '   月底 reflect-monthly 含 tracking 终结 7 步; 中段可走 ad-hoc audit reflect' >&2 ;; esac; exit 0"
     }
   ]
 }
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| Claude (会话起点) | 加载 settings.json → 5 hook + 11 case 全生效 |
| 用户 | Edit canonical 规范文件时看到提示, 减少 silent merge 反模式 |
| 自进化机制 | hook 暴露 5 个 skill 给用户, 提高 skill 发现率 |
| 已 merged 的 0200 (encoding hook) | 不冲突, 本提案只加 case 不改原有 |

---

## 5. 风险

- 风险 1: hook 提示过多 → 用户麻木。**缓解**: case 用具体关键字, 不撒网 (只匹"反思 / 升格 / 采集" 等具体 skill 触发短语)。
- 风险 2: PreToolUse 提示阻断编辑流。**缓解**: 只 `echo >&2` + `exit 0`, 不阻断 (与现有 hooks 一致)。
- 风险 3: settings.json JSON 解析失败 → Claude 会话异常。**缓解**: apply 后 dry-run `jq -c < settings.json` 验证语法。

---

## 6. 备选方案

- **A**: 用独立 .sh 文件 (e.g. `.claude/hooks/pre-edit.sh`) 替代行内长 command — 不选, settings.json 当前模式是 inline command, 一致性优先
- **B**: 加新 Stop hook 在会话末扫"本次有 Edit canonical 但无 proposal" — 不选, 复杂; 优先 PreToolUse 在源头拦
- **C** (选定): 扩展现有 case statements, 不引入新 hook 类型

---

## 7. 实施计划

```
[x] Step 1: 写 proposal (本文件)
[x] Step 2: 评审 — 2026-05-19 [solo-review]
[x] Step 3: 落地 (per 0040 §3.1 先 Read settings.json):
    - .claude/settings.json PreToolUse Edit|Write case 扩 (加 5 个 spec 路径)
    - .claude/settings.json UserPromptSubmit case 扩 (加 4 类自进化 skill 关键字)
[x] Step 4: jq dry-run 验证 JSON 合法
[ ] Step 5: tracking 期看 silent merge 复发 + skill 发现率
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| silent merge 次数 (W19-W21 = 3 次: 0027/0028/0029) | 3 / 3 周 | 0 / 月 (W22+ ) |
| 用户主动调 skill 比例 | 0% (W21 全部手工触发) | ≥ 30% (W22+, 看到 hint 后) |
| Edit canonical 规范文件无 proposal 次数 | N/A | 0 (W22+) |
| Hook 误报率 (干扰正常工作) | 0 (本提案部署后) | ≤ 5% |

Tracking 期: 2026-05-19 ~ 2026-06-02。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl `[solo-review]` | ✅ 通过 | 2026-05-19 | 工具链类; 与已 merged 的 0200 兼容; 扩展现有 hook 不引入新类型, 风险低; 用户明确指令 "增加 hook, 控制流程" |
| Claude | ✅ 实施 | 2026-05-19 | 按 0040 §3.1 先 Read settings.json 当前文本 (293 行 → 53 行结构), 按 0041 §3.1 第 4 checkbox `.claude/settings.json` 是配置不是代码, 不适用 grep 现存代码; 但需 jq 验证 JSON 合法 |

> Solo 单签理由: hook 扩展是机械加 case statement, 不改 hook 类型; 风险已枚举且缓解; 用户明确请求, 不存在评审能找出新发现的可能。

---

## 10. 实施后跟踪 (已 merged)

### 实际合入
- 合入 commit: 待回填
- 实际 merged 日期: 2026-05-19

### Tracking 数据

| 信号 | 基线 | 目标 | W22 | W23 |
|---|---|---|---|---|
| silent merge 次数 | 3 (W19-W21) | 0 / 月 | 待填 | 待填 |
| skill 通过 hint 触发率 | 0% | ≥ 30% | 待填 | 待填 |
| Edit canonical 无 proposal 次数 | N/A | 0 | 待填 | 待填 |
| Hook 提示误报次数 | 0 | ≤ 5% | 待填 | 待填 |

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-19 | Wjl + Claude | 首版 + 同日 solo-review accept + apply (per 0040 §3.5): settings.json PreToolUse Edit\|Write + UserPromptSubmit 2 段 case 扩展; jq dry-run 通过 |
