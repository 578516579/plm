# 03-开发 / CLAUDE.md — 开发阶段入口

> 这是给**人类开发者**看的"开发阶段门户"，与项目根目录的 [CLAUDE.md](../CLAUDE.md) 区分开：
> - **根 [CLAUDE.md](../CLAUDE.md)**：Claude Code 自动加载的运行时上下文（仓库布局、命令、坑、架构）。
> - **本文件**：人类开发者首次接触本目录时的导航页。

## 一分钟上手

1. 看 [README.md](README.md) 了解本目录的文档结构。
2. 启用 git hook（仅首次）：在仓库根 `git config core.hooksPath .githooks`，提交时自动校验 Conventional Commits。
3. 本地能跑通：照根目录 [CLAUDE.md](../CLAUDE.md) 的 "Running locally" 5 步。
4. 写代码前过一遍 [开发规范.md](开发规范.md) §0 命名总纲 + §1/§2 后端/前端规则。
5. 改业务模块前看 [../99-跨阶段/模块工作流.md](../99-跨阶段/模块工作流.md) 当前阶段的"准入/准出"。
6. 做架构决策前先翻 [ADR/](ADR/)，确认要不要新增一条。
7. 当前 Sprint 任务看 [Sprint 计划与回顾/](Sprint%20计划与回顾/)。

## 工作流（PR 流程示意）

```
issue / 需求拆解
  → 分支 feature/<jira-id>-<short-desc>
  → 开发 + 本地通过验证 (mvn install / npm run dev)
  → PR (含描述 / 自测清单 / 关联 issue)
  → CR (至少 1 个 approve)
  → 合 main，CI 触发部署到 staging
  → 验收通过后 tag → 进 [05-上线](../05-上线/)
```

## 关键链接

| 用途 | 位置 |
|---|---|
| 后端代码 | [../plm-backend/](../plm-backend/) |
| 前端代码 | [../plm-frontend/](../plm-frontend/) |
| API 契约 | [../02-设计/API 设计.md](../02-设计/API%20设计.md) |
| 数据库脚本 | [../plm-backend/sql/](../plm-backend/sql/) |
| 环境变量 | [../plm-backend/.env.example](../plm-backend/.env.example) |
| 规则文档（人）| [开发规范.md](开发规范.md) |
| 规则文档（Claude）| [../.claude/rules.md](../.claude/rules.md) |
| 模块工作流（全角色） | [../99-跨阶段/模块工作流.md](../99-跨阶段/模块工作流.md) |
| Editor 配置 | [../.editorconfig](../.editorconfig) |
| Commit hook | [../.githooks/commit-msg](../.githooks/commit-msg) |

## 谁是 owner

| 模块 | Owner | 备份 |
|---|---|---|
| 后端架构 | TBD | |
| 前端架构 | TBD | |
| DBA | TBD | |
| DevOps | TBD | |
