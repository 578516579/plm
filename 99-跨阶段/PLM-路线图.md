# PLM 业务模块路线图 v1

> **基线**: [AgriPLM-模块映射-2026-05-16.md](AgriPLM-模块映射-2026-05-16.md)
> **节奏**: solo / early / internal-tool — 不做时间承诺,只做"优先级 + 依赖"排序
> **维护**: 每完成一个版本更新本文 §当前进度

---

## 当前进度

✅ **v0.1.0 (2026-05-15 上线)** — Project 模块完整生命周期
🔄 **v0.2 (2026-05-16 启动 Phase 01)** — Requirement + Task + Sprint 三件套

---

## 完整路线

### v0.1.0 ✅ 已发布
- ✅ P0: 包名/模块/yml 改造 (cn.com.bosssfot.dv.plm)
- ✅ 凭据外部化 (.env.example)
- ✅ **Project 模块** (`tb_project` + Phase 01-05 Gate 全程归档)
- ✅ Phase 06 cycle 1 运营 substrate (周报/数据看板/反馈/Gate)

### v0.2 🔄 (当前批次)
**目标**: 把"项目"实体扩展到完整的"PM 三件套",支持基础的需求 → 任务 → 迭代工作流。

| # | 模块 | 表 | Phase 状态 |
|---|---|---|---|
| 1 | **Requirement (需求管理)** | `tb_requirement` | 🔄 Phase 01 进行中 (2026-05-16) |
| 2 | **Task (开发任务)** | `tb_task` | 🔄 Phase 01 进行中 |
| 3 | **Sprint (迭代)** | `tb_sprint` | 🔄 Phase 01 进行中 |

**联调依赖**: Task FK→Requirement + FK→Sprint;Sprint FK→Project

**预计字段量**: ~30 个字段 (3 张表合计) + 5-7 张 sys_dict + 9-12 条 sys_menu

### v0.3 (backlog)
**目标**: 把测试相关从 markdown 转到结构化数据。

| # | 模块 | 表 | 备注 |
|---|---|---|---|
| 4 | **Defect (缺陷)** | `tb_defect` | FK→Sprint + FK→Task |
| 5 | **Test Case (测试用例)** | `tb_test_case` | FK→Requirement |
| 6 | **甘特图 / 里程碑** | 复用 Project 表 + `tb_milestone` | 增强 Project 模块 |
| 7 | **角色/团队成员** | sys_user 扩展或 `tb_team` | 用 RuoYi 自带 sys_user 起步 |

### v0.4 (backlog)
**目标**: 文档管理统一化 + 发布记录入库。

| # | 模块 | 表 | 备注 |
|---|---|---|---|
| 8 | **Document (通用文档)** | `tb_document` | `doc_type` 字段统管 PRD/UED/Arch/DB/API/手册/测试报告 |
| 9 | **Release (发布记录)** | `tb_release` | 替代当前手动维护的 Changelog 段落 |
| 10 | **工作台 Dashboard** | (无新表,聚合查询) | 综合统计页面 |
| 11 | **立项建议书** | `tb_proposal` | 把 01-立项/ markdown 转入表 |

### v0.5+ (远期 backlog)
**只有在"通用 PLM 跑得够熟"后再考虑**:

- Figma MCP 集成 (UED 模块)
- Mock 服务控制台 (API Design 模块)
- AI 文档生成 (PRD / 手册 / 测试报告) — **但本 PLM 已经用 Claude 实现了等效**,可能永不实装到产品
- GitLab / 飞书 / 钉钉 MCP
- Feature Flag (用 Unleash 替代,不自建)

### 永不做 (剥离清单)

详见 [AgriPLM-模块映射-2026-05-16.md §3 剥离清单](AgriPLM-模块映射-2026-05-16.md)。摘要:
1. AgriKB 农业知识库 + Milvus 向量
2. Dify 工作流引擎 + 18 工作流
3. 5 个 AI Agent (代码审查/测试生成等)
4. 土壤/气象/灌溉/IoT/病虫害业务
5. 农业 UI 组件库
6. 15 维竞品对比矩阵
7. AsyncAPI / GraphQL Spec (Springdoc 够了)
8. Feature Flag 平台 (用现成 SaaS)

---

## 跨版本架构原则

1. **若依生态优先**: 能用 RuoYi 现成的 (sys_user/role/menu/dept/dict/config/notice) 就用,不重造
2. **文档表统一**: `tb_document` 的 `doc_type` 枚举管 PRD / UED / Arch / DB / API / 测试报告 / 各类手册,避免 6-8 张子表
3. **字典先行**: 每个业务表的枚举字段都建 sys_dict (状态/类型/优先级),不写硬编码
4. **菜单按业务分组**: 业务管理父菜单 2000 下挂二级:
   - 2010 项目管理 ✅
   - 20XX 需求管理 🔜
   - 20XX 任务管理 🔜
   - 20XX 迭代管理 🔜
   - 20XX 缺陷管理 (v0.3)
   - 20XX 测试用例 (v0.3)
   - 20XX 文档管理 (v0.4)
   - 20XX 发布管理 (v0.4)
5. **按 Gate 走**: 每个新模块都必须走 Phase 01-05 (early/solo 可简化但不可跳过)

---

## 每个新模块的标准动作清单 (Phase 01 → 05)

每个模块 Wjl 都必须按下面顺序至少各产出 1 份文件:

| Phase | 必产出物 | 引用模板 |
|---|---|---|
| **01 立项** | PRD + Phase 01 Gate 实例 | [01-立项/](../01-立项/) 写 PRD,[Gate](gate-checklists/Phase01-立项-Gate.md) 复制 |
| **02 设计** | API 设计 + 表结构 + Phase 02 Gate | [02-设计/](../02-设计/) 写设计,Gate 模板复制 |
| **03 开发** | 后端代码 + 前端代码 + SQL 脚本 + Phase 03 Gate | RuoYi 代码生成器 `tool/gen` 或 ruoyi-bootstrap skill Phase 7 |
| **04 测试** | Service 单测 + (轻)集成 / E2E + Phase 04 Gate | 参考 Project 的 17 Service 测试 + 5 Playwright |
| **05 上线** | 发布计划 + Runbook 回滚段 + Changelog + tag + Phase 05 Gate | 参考 Project v0.1.0 上线全套 |
| **06 运营** (持续) | cycle 1 (7 天) + cycle 2 (30 天) + 季度复盘 | 参考 Project Phase 06 cycle 1 启动 |

> 注: 4D 参数化 (L1/L2/L3 × 项目类型 × 团队规模 × 项目成熟度) 全部继承 — solo + early + internal-tool 下 §C 凭据红线 / §H 双人签字 / §D 灰度等都自动简化 (见 proposals 0001-0006)。

---

## 修订记录

| 日期 | 修改人 | 变更 |
|---|---|---|
| 2026-05-16 | Wjl | 首次创建 |
