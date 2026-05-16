# 24 Stub 模块启动路线图

| 字段 | 值 |
|---|---|
| 版本 | v1.0 |
| 当前已 active | 6 个 (Project / Requirement / Sprint / Task / Defect / TestCase) |
| 剩余 stub | 24 个（30 - 6） |
| 排期窗口 | v0.4 / v0.5 / deferred |

---

## 1. 总体规划

| 版本 | 启动模块 | 数量 | 主题 |
|---|---|---|---|
| **v0.3** ✅ | Project, Requirement, Sprint, Task, Defect, TestCase | 6 | MVP 核心闭环 |
| **v0.4** | Proposal, Prd, Arch, Dbdesign, Apidesign, Submission, Release, Apidoc, Testplan, Testreport, Manual-product | 11 | 文档体系入库 + 测试体系闭环 + 发布上线 |
| **v0.5** | Dashboard, Ued, Analytics, Manual-impl, Manual-ops, Dora | 6 | 聚合视图 + 文档完善 + 效能 |
| **deferred** | Competitive, Testdata, Autotest, Pipeline, Feature-flag, Openspec, Ai-agent | 7 | 评估必要性后决议是否做 |

---

## 2. v0.4 启动模块详情（11 个）

### 2.1 文档体系入表（5 个）

把 `01-立项/` / `02-设计/` / `04-测试/` 的 markdown 设计文档逐步入数据库。统一进 `tb_document` 表 + `doc_type` 字段：

| Maven artifactId | doc_type | 替代的 md 文件 | 备注 |
|---|---|---|---|
| `plm-proposal` | `proposal` | `01-立项/<Module>-PRD.md` | 立项建议书 |
| `plm-prd` | `prd` | (上述合并)  | PRD 文档 |
| `plm-arch` | `arch` | `02-设计/<Module>-系统架构.md` | 系统架构 |
| `plm-dbdesign` | `db_design` | `02-设计/<Module>-数据库设计.md` | DB 设计 |
| `plm-apidesign` | `api_design` | `02-设计/<Module>-API设计.md` | API 详细设计 |

**实施提示**：实际上单个 `plm-document` 模块 + `doc_type` 字段更经济。考虑把 plm-proposal/prd/arch/dbdesign/apidesign 合并成 `plm-document` 一个 Maven 模块,5 个 Maven artifactId 退化为路由级分类。

### 2.2 测试体系闭环（2 个）

| Maven artifactId | 用途 | 与现有的衔接 |
|---|---|---|
| `plm-testplan` | 测试方案 | 关联 sprint_id,引用 testcase 列表 |
| `plm-testreport` | 测试报告 | sprint 跑完后归档,引用 defect 列表 |

### 2.3 提测/发布/API 文档（3 个）

| Maven artifactId | 用途 |
|---|---|
| `plm-submission` | 提测管理 (Phase 03 → 04 Gate 实例的入表版本) |
| `plm-release` | 发布管理 (git tag + Runbook + 关联 sprint) |
| `plm-apidoc` | API 文档 (Springdoc 自动生成,辅助筛选) |

### 2.4 产品手册（1 个）

| Maven artifactId | 用途 |
|---|---|
| `plm-manual-product` | 产品手册（面向终端用户） |

---

## 3. v0.5 启动模块详情（6 个）

### 3.1 聚合视图（2 个）

| Maven artifactId | 用途 |
|---|---|
| `plm-dashboard` | 工作台聚合（Project/Req/Task 统计 + 我的待办） |
| `plm-analytics` | 效能分析（关联 signals 自动化） |

### 3.2 文档完善（3 个）

| Maven artifactId | 用途 |
|---|---|
| `plm-ued` | UED 设计稿（Figma 链接 + 版本） |
| `plm-manual-impl` | 实施手册 |
| `plm-manual-ops` | 运维手册 |

### 3.3 效能（1 个）

| Maven artifactId | 用途 |
|---|---|
| `plm-dora` | DORA 4 指标（与 signals 联动，月度统计） |

---

## 4. deferred 模块（7 个，重新评估必要性）

| Maven artifactId | deferred 理由 | 重新评估时机 |
|---|---|---|
| `plm-competitive` | internal-tool 无外部竞品 | v0.5+ 转 external-product 时 |
| `plm-testdata` | 测试数据用 SQL fixture / Faker 即可 | 数据量上千 case 时 |
| `plm-autotest` | Playwright 跑在 CI 即可,不入库 | 需要追溯测试历史时 |
| `plm-pipeline` | GitHub Actions 跑在 CI,不入 PLM 库 | 需要 PLM 跨流水线对比时 |
| `plm-feature-flag` | 用 Unleash/GrowthBook,不重造 | 用户量超过 100 时 |
| `plm-openspec` | 等 AI 引擎选型决议 | v0.6+ AI 引擎落地 |
| `plm-ai-agent` | 同上 | 同上 |

---

## 5. 启动单个 stub 的标准流程

复用 [模块拆分指南.md §3](模块拆分指南.md) 7 步流程,关键步骤:

```bash
# 1. 写 PRD
vim 01-立项/<Module>-PRD.md
# 走 Phase 01 立项 Gate (instances/<module>/Phase01-...)

# 2. 写 DB + API 设计
vim 02-设计/<Module>-数据库设计.md  02-设计/<Module>-API设计.md
# Phase 02 设计 Gate

# 3. 升级 stub → active
cd plm-backend/plm-<module>
# pom: 加 FK 依赖 (plm-project 等)
# 移除 stub 占位 package-info.java
# 复制 plm-defect 或 plm-testcase 模板改写 Domain/Mapper/Service/Controller

cd ../../plm-frontend/packages/plm-<module>
# package.json 升级到 0.3.0 + active
# api/types/views 实现
# router.ts (一般是路由不动,只需要业务页面)

# 4. SQL 入库
mysql --default-character-set=utf8mb4 plm < plm-backend/sql/business-<module>.sql

# 5. legacy 极薄壳 (Stage 1, 见 Legacy-镜像迁移-playbook.md)
# src/api/business/<module>.ts:        export * from '../../../packages/plm-<module>/src/api/index'
# src/types/api/business/<module>.ts:  export * from '../../../../packages/plm-<module>/src/types/index'
# src/views/business/<module>/index.vue:  极薄壳 <View />

# 6. E2E spec
vim plm-frontend/e2e/<module>.spec.ts
# fixtures-<module>.ts 状态机定义

# 7. Phase 03 Gate
vim 99-跨阶段/gate-checklists/instances/<module>/Phase03-...

# 8. mvn install + e2e + commit
```

---

## 6. v0.4 排期建议

按依赖顺序 + 业务连贯性:

```
v0.4.0 (~2 周, ~6 模块)
  ├─ plm-proposal (单独入库基础)
  ├─ plm-prd / plm-arch / plm-dbdesign / plm-apidesign (合并到 plm-document)
  └─ plm-submission (Phase 03→04 入表)

v0.4.1 (~1 周, ~3 模块)
  ├─ plm-release
  ├─ plm-testplan
  └─ plm-testreport

v0.4.2 (~1 周, ~2 模块)
  ├─ plm-apidoc
  └─ plm-manual-product
```

---

## 7. 路线监控

每个 stub 启动后,更新本路线图的状态:

- [ ] v0.4.0 完成: 加 ✅ 进 §2,本路线图改对应模块到 active
- [ ] 月底 reflect/2026-MM.md 引用本路线图统计完成度

---

## 8. 修订记录

| 版本 | 日期 | 变更 |
|---|---|---|
| v1.0 | 2026-05-16 | 首次创建,基于 v0.3 完成 6 模块状态 |
