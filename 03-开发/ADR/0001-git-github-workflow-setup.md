# ADR-0001: Git & GitHub 工作流基础设施建立

| 字段 | 值 |
|---|---|
| 状态 | Accepted |
| 日期 | 2026-05-18 |
| 决策人 | @578516579 |
| 影响范围 | 全仓库 DevOps 基础设施 |

## 背景

项目从若依脚手架改名（commit `2679a61`）后，一直在 Claude Code worktree 分支（`claude/*`）上开发，没有规范的主干分支、PR 流程和 CI。随着模块数量增加（13 个 PRD-aligned 模块），需要建立完整的 Git & GitHub 工作流防止质量滑坡。

## 决策

建立四层工作流基础设施：

1. **机器强制层**（git hooks）
   - `commit-msg`：Conventional Commits 格式校验
   - `pre-push`：分支命名校验 + 禁止直推 `main`/`release/*`

2. **平台层**（GitHub）
   - `main` 作为唯一保护主干（require CI Gate + 1 Approve）
   - PR 模板（Gate 对齐清单）
   - Issue 模板（bug / feature，含 PRD 追溯）
   - GitHub Actions CI（backend mvn + frontend build）

3. **规范文档层**
   - `03-开发/git-github-workflow.md`：完整 `gh` 命令操作手册
   - `.claude/rules.md §N`：Claude GitHub 行为约束

4. **工具层**
   - `~/.claude/skills/git-workflow/`：Claude skill（7 场景）

## 分支模型

```
main          ← 保护，只 PR 合入，CI Gate 必须绿
  ↑
feature/<id>-<desc>   ← 日常开发
fix/<id>-<desc>       ← bug 修复
hotfix/<desc>         ← 线上紧急
release/x.y           ← 发版冻结
```

## 后果

- **正面**：强制 PR 审查，CI 自动验证编译，分支命名标准化，GitHub Issue 与 PRD 对齐
- **负面**：solo 开发阶段需要自己给自己 approve（可临时关闭 enforce_admins 或用 `gh pr merge --admin`）
- **缓解**：`enforce_admins: false`，不强制管理员也走 PR（紧急时可直接合并）

## 参考

- [03-开发/git-github-workflow.md](../git-github-workflow.md)
- [.github/workflows/ci.yml](../../.github/workflows/ci.yml)
- [.claude/rules.md §N](../../.claude/rules.md)
