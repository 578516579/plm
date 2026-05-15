# Phase 03 — 开发 Gate Checklist

> 复制本模板到 `instances/<模块>/Phase03-开发-Gate-<YYYY-MM-DD>.md`，每项打勾 / 填值后归档。
> 模板说明见 [README.md](README.md)。**L3 小型改动仅需填 F 段 PR/CR 子集**。

---

## 头部信息（必填）

| 字段 | 值 |
|---|---|
| 模块名 | |
| 分级 | L1 / L2 / L3 |
| **分级理由** | _引用 [README §维度 1](../README.md) 的具体判定列_ |
| **项目类型** | `external-product` / `internal-tool` / `framework-upgrade` |
| **团队规模** | `solo` / `small` / `medium` / `large` |
| Owner（开发 lead） | |
| 起始日期 | YYYY-MM-DD |
| 目标完成日期 | YYYY-MM-DD |
| 实际完成日期 | YYYY-MM-DD |
| 关联 PRD | 链接 + 版本号 |
| 关联设计 | 链接（Phase 02 Gate） |
| Sprint | Sprint NN |

---

## A. 准入条件（L1/L2 必须）

- [ ] [Phase 02 Gate](Phase02-设计-Gate.md) 已签字通过
- [ ] API 契约 + DB 设计已冻结
- [ ] 已分配开发者（后端 / 前端 / DBA 角色明确）
- [ ] 任务已拆解进 Sprint（[Sprint 计划与回顾](../../03-开发/Sprint%20计划与回顾/)）
- [ ] 本地能跑通（参考根 [CLAUDE.md](../../CLAUDE.md) Running locally）

---

## B. 必产出物 — 代码

### B.1 后端

- [ ] 业务代码落位符合 [开发规范.md §1.1](../../03-开发/开发规范.md)：
      `plm-system/.../system/business/<entity>/{domain,mapper,service,service/impl}/`
      `plm-admin/.../web/controller/business/<Entity>Controller.java`
- [ ] 优先使用 `ruoyi-bootstrap` skill Phase 7 模板生成骨架（保证规范一致）
- [ ] 每个 Controller 端点有 `@PreAuthorize` 权限注解
- [ ] 每个写操作有 `@Log` 注解
- [ ] 每个跨表写入有 `@Transactional(rollbackFor = Exception.class)`
- [ ] Mapper XML 用 `<resultMap>` + `<where>` + `<trim>`，无字符串拼 SQL
- [ ] 逻辑删除走 `del_flag='2'`，无 `DELETE FROM`
- [ ] 异常用 `ServiceException`；无 `catch (Exception) {}` 空捕获；无 `e.printStackTrace()`
- [ ] 日志用 SLF4J 参数化（`log.info("x={}", x)`），无字符串拼接
- [ ] 敏感字段日志已脱敏

### B.2 前端（如有）

- [ ] 业务文件落位符合 [开发规范.md §2.1](../../03-开发/开发规范.md)：
      `src/{api,types/api,views}/business/<entity>/`
- [ ] Vue 用 `<script setup lang="ts">` + 显式 `name=...`
- [ ] API 调用走 `@/utils/request`（无直接 axios）
- [ ] 类型走 `types/api/business/<entity>.ts`（无裸 any）
- [ ] 按钮级权限用 `v-hasPermi`
- [ ] 下拉用 `useDict('biz_xxx_yyy')`（无硬编码选项）

### B.3 SQL

- [ ] 业务 SQL 文件命名：`plm-backend/sql/business-<entity>.sql`
- [ ] SQL 已在 staging 演练成功
- [ ] 含 CREATE TABLE + sys_menu inserts + sys_role_menu grants + sys_dict_type/data inserts
- [ ] 列均有 `comment`
- [ ] 字符集 utf8mb4，导入测试用过 `--default-character-set=utf8mb4`

### B.4 测试代码

- [ ] Service 单元测试覆盖率 ≥ 70%（jacoco 报告链接：__）
- [ ] Controller 集成测试覆盖核心端点
- [ ] 前端 utils 单元测试（Vitest）
- [ ] 测试代码 commit 在源码 `src/test/` 下，**不要**写到 `04-测试/` 文档目录

---

## C. 必产出物 — 文档

- [ ] Sprint 计划已写：`03-开发/Sprint 计划与回顾/Sprint-NN-...md`
- [ ] Sprint 回顾已写（哪怕一句话）
- [ ] 如本期有架构决策：ADR 已 accepted，文件在 `03-开发/ADR/NNNN-...md`
- [ ] Swagger 文档与实现一致（启动后端访问 `/swagger-ui.html` 检查）
- [ ] 关键业务代码有 Javadoc / TSDoc（public 方法必须）

---

## D. 必产出物 — 提交质量

- [ ] 所有 commit message 符合 Conventional Commits（由 `.githooks/commit-msg` 保证）
- [ ] 一件事一个 commit；无 "fix typo + add feature + 升级依赖" 混合 commit
- [ ] **未使用 `--no-verify` 绕过 hook**（如使用，PR 描述中说明原因）
- [ ] 已 rebase main，无冲突

---

## E. PR / CR 流程（L1/L2/L3 全部必走）

### PR 创建

- [ ] PR 标题符合 Conventional Commits
- [ ] PR 描述含：背景 / 改动点 / 自测情况 / 关联 issue
- [ ] PR 关联本 Checklist 的实例文件 URL

### CR 审核

- [ ] L1 / L2：至少 1 个 approve（架构级改动 ≥ 2 个）
- [ ] L3：至少 1 个 approve
- [ ] CR 评论闭环（reviewer 提的所有 comment 都已 resolve 或回复）
- [ ] CI 全绿（如已搭 CI）

### 合并

- [ ] 合 main 走 squash 或 rebase（不要 merge commit 污染历史）
- [ ] 合并后本地 main 已更新；feature 分支已删除

---

## F. Definition of Done（出口 DoD）

L1 / L2 必须全部满足：

- [ ] B / C / D / E 全部满足
- [ ] 本地"完整跑通"验证通过：根 [CLAUDE.md](../../CLAUDE.md) 的 curl 测试返回 200
- [ ] **本 Checklist 文件已 commit 入库**（`docs(gate): <module> phase 03 passed`）

L3 仅需：

- [ ] E（PR + CR）通过
- [ ] D 提交规范通过
- [ ] [Phase 04 Gate](Phase04-测试-Gate.md) 的"L3 回归测试子集"通过

---

## G. 评审记录与签字（按 团队规模 调整必填角色数）

`solo`=1（开发 lead 自评 `[solo-review]`）/ `small`=2 / `medium`=3 / `large` 按下表全部签字。

| 角色 | 姓名 | 评审结论 | 签字日期 |
|---|---|---|---|
| 开发 lead | | 通过 / 有条件通过 / 不通过 | YYYY-MM-DD |
| 主 reviewer（small+ 必填） | | | |
| 测试 lead（启动 Phase 04 时，medium+ 必填） | | | |

---

## H. 异常 / 例外

| 项 | 原因 | 补救计划 | 截止日 | 责任人 |
|---|---|---|---|---|
| | | | | |

---

## I. 进入 Phase 04 的准出确认

- [ ] 代码已合 main 且 CI 全绿
- [ ] DB 脚本在 staging 演练成功
- [ ] 测试 lead 已拿到可测产物（API 文档 / 部署到 staging 的版本）

✅ **签字人确认**：

| 角色 | 签字 | 日期 |
|---|---|---|
| 开发 lead | | |
| 测试 lead | | |

---

## 修订记录

| 日期 | 修改人 | 原因 | 决议 |
|---|---|---|---|
| | | | |
