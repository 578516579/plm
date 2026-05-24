## 背景 / Background

> 解决什么问题？链接对应的 Issue / PRD 章节 / Gate Checklist 实例。
> What problem does this solve? Link the Issue / PRD section / Gate Checklist instance.

- Issue: #
- PRD § (如有 / if applicable):
- Gate Checklist 实例 (如有 / if applicable): `99-跨阶段/gate-checklists/instances/<module>/`

---

## 改动点 / Changes

> 列要点，不要罗列每个文件。

- 

---

## 改动类型 / Type of Change

- [ ] `feat` — 新功能（New feature）
- [ ] `fix` — Bug 修复（Bug fix）
- [ ] `refactor` — 重构（Refactoring, no feature/fix）
- [ ] `docs` — 文档（Documentation only）
- [ ] `test` — 测试（Adding/fixing tests）
- [ ] `chore` — 构建/依赖/配置（Build, deps, config）
- [ ] `perf` — 性能优化（Performance improvement）
- [ ] `ci` — CI/CD 变更

---

## 自测情况 / Self-Test

> 本地跑过的命令 / 截图证据。

- [ ] 后端编译通过：`mvn clean install -DskipTests`
- [ ] 前端编译通过：`npm run build`
- [ ] E2E 通过（业务模块必填）：`npm run test:e2e` — **41 passed**
- [ ] 手动验证关键路径（截图/curl 输出附下方）

<details>
<summary>验证输出 / Verification output</summary>

```
# 粘贴命令输出 / Paste command output here
```

</details>

---

## 数据库变更 / DB Changes

- [ ] 无数据库变更
- [ ] 有 DDL 变更（新增 SQL 文件：`plm-backend/sql/business-<entity>.sql`）
- [ ] 有数据迁移（已在 PR 描述中说明回滚方式）

---

## 安全 / Security

- [ ] 无敏感值硬编码（密码/token/key 全走 `${VAR}` 占位）
- [ ] 新增 API 端点已加 `@PreAuthorize` 权限注解
- [ ] 无 SQL 注入风险（用户输入走 `#{param}` 参数化）

---

## PRD 追溯 / PRD Traceability（业务模块必填）

- [ ] 字段/状态/错误码 已在 `PRD-MAPPING.md` 登记
- [ ] 无 PRD 未提及的字段被添加（或已走 Proposal 流程修改 PRD-MAPPING）

---

## 关联 / Related

- PR: #
- ADR (架构决策): `03-开发/ADR/`
- 测试用例: `04-测试/`

---

## 合并清单 / Merge Checklist（Reviewer 确认）

- [ ] CI 全绿
- [ ] 至少 1 个 Approve（架构级至少 2 个）
- [ ] 分支名符合规范（`feature|fix|hotfix|chore|release/<desc>`）
- [ ] Commit message 符合 Conventional Commits
- [ ] 不允许 `--no-verify` 绕过 hook（无法规避时在 PR 描述中说明原因）
