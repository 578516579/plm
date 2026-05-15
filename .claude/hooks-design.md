# Hooks 设计文档 — `.claude/settings.json` 配套说明

定义本仓库 Claude Code hooks 的**意图、行为、回滚方式**。配合 [settings.json](settings.json) 食用。

> Claude Code hooks 是把"靠 Claude 自觉"变"系统强制"的关键基础设施。但 hook 写错会让会话变笨/卡，所以必须文档化每个 hook 的"为什么"。

---

## 已配置的 hooks 一览

| 触发 | 类型 | 作用 | 失败影响 |
|---|---|---|---|
| `Stop` | command | 会话回合结束时，提示是否需要沉淀（gotcha / ADR / proposal / Gate update） | 仅多一段 stderr 输出；不阻塞会话 |
| `PreToolUse(Bash)` | command | Bash 命令前扫描高危关键词（DROP/TRUNCATE/force-push/reset-hard），命中时打印警告 | 命中时仅 stderr 提示；不拦截执行（拦截策略写在 .claude/rules.md §G.3） |
| `PreToolUse(Edit\|Write)` | command | 写文件前若路径在 `gate-checklists/instances/`，提示是否要"修订记录"追加而非覆盖 | 命中时仅 stderr 提示；不阻塞写 |
| `UserPromptSubmit` | command | 检测用户意图关键词（新功能/上线/回滚），提示对应 skill / Gate Checklist 入口 | 仅 stderr 输出；不影响 prompt 处理 |

> 全部 hooks **不主动阻塞** Claude 行为，只输出 stderr 提示。这是 Phase A 的设计原则——先建提醒机制，等用熟了再考虑强制拦截。

---

## 设计原则

### 1. Fail open（失败不挡路）

hook 命令哪怕 syntax 错、超时、平台不兼容，都不应该让 Claude 卡死。所有 hook 命令末尾 `exit 0`，stderr 仅提示。

### 2. 提示不替代规则

stderr 输出是**给用户和 Claude 看的提示**，真正的规则约束写在 [.claude/rules.md](rules.md)。hook 是"提醒"，rules.md 是"宪法"。改规则时改 rules.md，不要把规则塞进 hook 命令里。

### 3. Windows + Git Bash 友好

- 用 `>&2` 重定向 stderr（避免污染 stdout 被 Claude 当作工具输出）
- 用 POSIX shell 语法（`case...esac` 不用 `[[ ]]`）
- 不用中文做关键词匹配（GBK / UTF-8 编码差异问题）
- 输出用 UTF-8 中文是 OK 的（Git Bash 默认 UTF-8 输出）

### 4. 环境变量约定

Claude Code 在执行 hook 时注入一组 `CLAUDE_*` 环境变量。本仓库 hook 用到：

| 变量 | 含义 |
|---|---|
| `CLAUDE_TOOL_INPUT_command` | Bash 工具被调用时的命令字符串 |
| `CLAUDE_TOOL_INPUT_file_path` | Edit/Write 工具的目标文件路径 |
| `CLAUDE_USER_PROMPT` | UserPromptSubmit 时的用户输入文本 |

> ⚠️ 这些变量名是基于通用约定推测；若你的 Claude Code 版本 schema 不一致，hook 的 case 匹配会"永远不命中"（也就是没效果，但不会出错）。
> 验证方法见下文"测试"。

---

## 测试方法

### Stop hook

启动新会话，问 Claude 任意问题，回答完后看终端有没有 `─── reflect on this turn ───` 那段输出。
- ✅ 看到 → 工作正常
- ❌ 没看到 → 见"排错"

### PreToolUse(Bash) hook

请 Claude 跑一条故意触发的命令，例如：

```bash
echo "DROP TABLE foo"   # 不会真执行 DROP，但命令字符串含关键词
```

应该看到 `⚠️ HIGH-RISK COMMAND DETECTED` 输出。

### PreToolUse(Edit) hook

让 Claude 试着改 `gate-checklists/instances/foo/bar.md`（哪怕文件不存在），应看到 `⚠️ EDITING SIGNED GATE CHECKLIST INSTANCE` 提示。

### UserPromptSubmit hook

发一条消息含"新功能"，应看到 `💡 detected intent: new business feature`。

---

## 排错

### 完全没看到任何 hook 输出？

可能原因：
1. **Claude Code 版本不支持本 schema**：查 `claude --version` 与 [Claude Code 官方 hooks 文档](https://docs.claude.com/claude-code) 对照。我们的 schema 假定 v1.x 通用格式。
2. **hooks 字段在 settings.local.json 被覆盖**：项目级 `settings.json` < 个人 `settings.local.json`。若你 local 有 `"hooks": {}` 会清掉 hooks。
3. **hook 命令本身报错**：把 settings.json 中某条 hook 的 command 改成简单的 `echo HOOK_FIRED >&2`，验证 hook 系统能不能跑通；再逐步加复杂度。

### 看到了提示但内容不对（变量名拼错？）

环境变量名可能不是 `CLAUDE_TOOL_INPUT_command`。尝试改成 `$1` 或在 hook 命令开头加 `env | grep CLAUDE_` 把所有 CLAUDE_* 变量打出来诊断。

### Stop hook 触发太频繁吵？

把 `command` 改成 `[ $((RANDOM % 5)) -eq 0 ] && echo '...' >&2`（1/5 概率触发）或仅在特定时间触发。Phase B 之后可以把 reminder 改成"周一/周五才提醒"。

---

## 后续 Phase B 升级方向

| 升级 | 用途 |
|---|---|
| Stop hook 改成 `additionalContext` 类型 | 不输出到终端，而是把"是否沉淀"信息直接注入 Claude 下一轮上下文，由 Claude 主动判断 |
| PreToolUse 改成 `block` | 命中时真拦截执行（需先确保规则成熟，避免误伤） |
| 加 `SubagentStop` hook | 子任务结束时把信号写入 `99-跨阶段/signals/<YYYY-MM>.md` |
| 加 `SessionEnd` hook | 整次会话结束时跑 `/reflect-session` 自动出会话总结 |

---

## 回滚

hooks 让 Claude 行为异常时：

```bash
# 临时关闭项目 hooks（不删文件，注释掉）
mv .claude/settings.json .claude/settings.json.disabled
```

下次会话不再加载 hooks，Claude 行为回到 baseline。问题排查清楚再 `mv` 回来。

---

## 修订记录

| 日期 | 修改人 | 改了什么 |
|---|---|---|
| 2026-05-15 | Claude+Wjl | 初版：Stop / PreToolUse(Bash) / PreToolUse(Edit\|Write) / UserPromptSubmit 四个 hook，全部 fail-open |
