# Git & GitHub 工作流手册

完整的"从需求到合并"流程规范。机器强制层：`.githooks/`；GitHub 平台层：`.github/`；人机共识层：本文档。

> 配套文件：
> - Commit hook → [../.githooks/commit-msg](../.githooks/commit-msg)
> - Push hook → [../.githooks/pre-push](../.githooks/pre-push)
> - PR 模板 → [../.github/PULL_REQUEST_TEMPLATE.md](../.github/PULL_REQUEST_TEMPLATE.md)
> - CI → [../.github/workflows/ci.yml](../.github/workflows/ci.yml)
> - 开发规范 §4 → [开发规范.md §4](开发规范.md)

---

## 0. 一次性环境配置

### 0.1 Git hooks 激活（clone 后必做）

```bash
git config core.hooksPath .githooks
# 验证
git config --get core.hooksPath   # → .githooks
```

### 0.2 安装 GitHub CLI（`gh`）

```bash
# Windows（winget，推荐）
winget install --id GitHub.cli

# Windows（chocolatey）
choco install gh

# macOS
brew install gh

# Linux
sudo apt install gh   # 或按 https://cli.github.com/manual/installation
```

验证：

```bash
gh --version   # → gh version 2.x.x
```

### 0.3 GitHub CLI 认证

```bash
gh auth login
# 按提示选择：
#   → GitHub.com
#   → HTTPS
#   → Login with a web browser  ← 推荐
# 浏览器打开后复制 one-time code → 授权完成
```

验证：

```bash
gh auth status
# 期望输出：Logged in to github.com account 578516579 (...)
```

### 0.4 配置默认仓库（可选，加速命令）

```bash
cd <repo-root>
gh repo set-default 578516579/plm
```

---

## 1. 日常提交流程

### 1.1 最小正确流程

```bash
# ① 确认在正确分支
git status
git branch

# ② 暂存要提交的文件（精确暂存，不要 git add -A）
git add plm-backend/src/...
git add plm-frontend/src/...

# ③ 提交（hook 自动校验格式）
git commit -m "feat(project): support batch close for projects"
# ↑ 如果格式错误，hook 会拒绝并打印示例

# ④ 推送（hook 自动校验分支名 + 保护分支）
git push -u origin <branch-name>
```

### 1.2 Commit message 规范（由 hook 强制）

```
<type>(<scope>): <subject>

[可选 body — 说明 why，不是 what]

[可选 footer — BREAKING CHANGE / Closes #123]
```

| type | 用途 |
|---|---|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `docs` | 纯文档变更 |
| `refactor` | 重构（无功能/bug 变化） |
| `test` | 加测试 / 改测试 |
| `chore` | 构建、工具链、依赖、配置 |
| `perf` | 性能优化 |
| `build` | 构建系统（Maven pom / vite.config 等） |
| `ci` | CI/CD 配置 |
| `revert` | 回滚某次 commit |
| `style` | 代码格式（不影响逻辑） |

**scope** 示例：`project` / `auth` / `task` / `db` / `ci` / `deps` / `mcp`

**好的 subject**：动词起头，≤ 70 字符，不以句号结尾
- `feat(task): add assignee field per PRD §3.3`
- `fix(auth): JWT expiry timezone issue causing midnight relogin`
- `chore(deps): bump fastjson2 2.0.60 → 2.0.61`

**Breaking change** 写在 footer：
```
BREAKING CHANGE: tb_project 删除 manager_id 列，改为 manager_user_id
```

---

## 2. 分支管理

### 2.1 长期分支

| 分支 | 保护 | 用途 |
|---|---|---|
| `main` | ✅ 只 PR 合入，CI 必须绿 | 稳定主干 |
| `release/x.y` | ✅ 只 cherry-pick fix | 发版冻结 |

**禁止直接 push `main` 或 `release/*`**（pre-push hook 强制拦截）。

### 2.2 工作分支命名（由 pre-push hook 校验）

```
<type>/<desc>
```

| type | 场景 |
|---|---|
| `feature` | 新功能 / 新模块 |
| `fix` | Bug 修复 |
| `hotfix` | 线上紧急修复 |
| `chore` | 非业务变更（deps / CI / 脚手架） |
| `docs` | 纯文档 |
| `refactor` | 重构 |
| `release` | 发版分支（`release/1.2`） |
| `test` | 测试专项 |
| `perf` | 性能优化 |

`<desc>` 小写、短横线、可带 issue 编号：

```bash
# 推荐带 issue 编号（便于 gh 自动关联）
feature/123-add-project-batch-close
fix/456-jwt-timezone-issue
hotfix/urgent-login-500
chore/deps-fastjson2-upgrade
```

### 2.3 创建工作分支（含 `gh` 快捷用法）

```bash
# 从 main 最新创建
git switch main && git pull
git switch -c feature/123-add-project-batch-close

# 或：从 GitHub Issue 自动创建（gh 自动命名并关联）
gh issue develop 123 --checkout
# ↑ 创建 issue-123 分支并切换
```

---

## 3. Pull Request 流程

### 3.1 创建 PR（`gh pr create`）

```bash
# 先推分支
git push -u origin feature/123-add-project-batch-close

# 交互式创建 PR（推荐，自动填模板）
gh pr create

# 快速创建（含标题和 body 模板路径）
gh pr create \
  --title "feat(project): support batch close for projects" \
  --body-file .github/PULL_REQUEST_TEMPLATE.md \
  --base main \
  --assignee @me
```

### 3.2 PR 必填内容

见 [PR 模板](../.github/PULL_REQUEST_TEMPLATE.md)，关键项：

- 关联 Issue（`#123`）或 PRD 章节
- 自测结果（命令输出 / 截图）
- 数据库变更说明
- PRD 追溯确认（业务模块）

### 3.3 查看 / 管理 PR

```bash
# 查看本仓库所有开放 PR
gh pr list

# 查看当前分支关联的 PR
gh pr status

# 在 terminal 里阅读 PR 详情
gh pr view 42

# 打开浏览器查看
gh pr view 42 --web

# 合并（需 approve + CI 绿）
gh pr merge 42 --squash --delete-branch
```

### 3.4 Code Review 要求

| 场景 | 最少 Approve |
|---|---|
| 普通功能 / Bug fix | 1 |
| 架构级改动 / 新模块 | 2 |
| DB 结构性变更（加列/改索引） | 2 + DBA 确认 |
| 安全相关（auth/jwt/权限串） | 2 |
| `main` merge freeze 期间 | 负责人 1 + owner 1 |

**Reviewer 重点检查**：
1. Commit message 格式 ✓
2. 无硬编码密码/token ✓
3. Controller `@PreAuthorize` 齐全 ✓
4. Service 写操作有 `@Transactional` ✓
5. 新业务字段在 `PRD-MAPPING.md` 已登记 ✓
6. E2E 测试有对应 spec（业务模块） ✓

---

## 4. Issue 管理

### 4.1 创建 Issue

```bash
# 交互式（推荐，自动选模板）
gh issue create

# 快速指定
gh issue create \
  --title "fix: project list returns 500 when filter by date range" \
  --label "bug,P1" \
  --assignee @me
```

### 4.2 常用 Issue 操作

```bash
# 查看所有开放 Issue
gh issue list

# 按标签过滤
gh issue list --label "bug"
gh issue list --label "P0"

# 查看单个
gh issue view 123

# 关闭（合 PR 时 commit 写 "Closes #123" 自动关闭）
gh issue close 123 --comment "已通过 PR #42 修复"

# 给 Issue 打标签
gh issue edit 123 --add-label "P1,enhancement"
```

### 4.3 Issue → Branch → PR 完整链路

```bash
# 1. 有 issue #123
gh issue view 123

# 2. 从 issue 创建分支（gh 自动命名 + 关联）
gh issue develop 123 --checkout

# 3. 开发、commit ...

# 4. 推送并创建 PR（--closes 自动关联 issue）
git push -u origin HEAD
gh pr create --title "feat(project): ..." --body "Closes #123"

# 5. PR 合并后 issue 自动关闭
```

---

## 5. Release 流程

### 5.1 创建 Release 分支

```bash
git switch main && git pull
git switch -c release/1.2
git push -u origin release/1.2
```

### 5.2 打 Tag 和 GitHub Release

```bash
# 打 tag（Conventional Commits 版本号）
git tag v1.2.0 -m "release: AgriPLM v1.2.0"
git push origin v1.2.0

# 自动生成 Release notes（基于 commits）
gh release create v1.2.0 \
  --title "AgriPLM v1.2.0" \
  --generate-notes \
  --latest
```

### 5.3 Hotfix 流程

```bash
# 从 main（或对应 release 分支）拉 hotfix
git switch main && git pull
git switch -c hotfix/urgent-login-500

# 修复 → commit → push
git commit -m "fix(auth): handle null JWT on expired session"
git push -u origin hotfix/urgent-login-500

# 创建 PR → 紧急通道合入（需 owner approve）
gh pr create --title "fix(auth): ..." --label "hotfix,P0"
```

---

## 6. 仓库管理（gh repo 系列）

```bash
# 查看仓库信息
gh repo view 578516579/plm

# 查看 CI 运行情况
gh run list --limit 10
gh run view <run-id>
gh run watch <run-id>   # 实时监控

# 查看某次失败的日志
gh run view <run-id> --log-failed

# 设置分支保护（owner 执行）
gh api repos/578516579/plm/branches/main/protection \
  -X PUT \
  -F required_status_checks='{"strict":true,"contexts":["CI Gate"]}' \
  -F enforce_admins=false \
  -F required_pull_request_reviews='{"required_approving_review_count":1}' \
  -F restrictions=null
```

---

## 7. 常见问题 / FAQ

### Q: pre-push 校验把我的分支拦住了

```bash
# 查看当前分支名
git branch --show-current
# → my-feature  ← 不符合规范

# 重命名分支
git branch -m my-feature feature/42-my-feature
git push -u origin feature/42-my-feature
```

### Q: commit-msg hook 报错

```bash
# 检查格式：type(scope): subject
git commit -m "feat(project): add batch close endpoint"
# type 必须是: feat|fix|docs|refactor|test|chore|perf|build|ci|revert|style
```

### Q: CI 后端失败（DB connection refused）

CI 使用 MySQL service container，通过 `127.0.0.1:3306` 连接。检查 `application-druid.yml` 是否正确读取 `${DB_PASSWORD}` 环境变量。

### Q: gh 命令报 "not logged in"

```bash
gh auth login   # 重新认证
gh auth status  # 确认状态
```

### Q: 如何在 commit 里关闭多个 issue

```
feat(task): implement bulk assign endpoint

Closes #123
Closes #124
```
