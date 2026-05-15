# Phase 02 — 设计 Gate Checklist

> 复制本模板到 `instances/<模块>/Phase02-设计-Gate-<YYYY-MM-DD>.md`，每项打勾 / 填值后归档。
> 模板说明见 [README.md](README.md)。

---

## 头部信息（必填）

| 字段 | 值 |
|---|---|
| 模块名 | |
| 分级 | L1 / L2 |
| **分级理由** | _引用 [README §维度 1](../README.md) 的具体判定列_ |
| **项目类型** | `external-product` / `internal-tool` / `framework-upgrade` |
| **团队规模** | `solo` / `small` / `medium` / `large` |
| Owner（技术 lead） | |
| 起始日期 | YYYY-MM-DD |
| 目标完成日期 | YYYY-MM-DD |
| 实际完成日期 | YYYY-MM-DD |
| 关联 PRD | 链接 + 版本号 |

---

## A. 准入条件（进入 Phase 02 前必须满足）

- [ ] [Phase 01 Gate](Phase01-立项-Gate.md) 已签字通过
- [ ] PRD v1.0+ 已正式发布（链接：__）
- [ ] 技术 lead 已 review PRD 并确认"技术上可做"
- [ ] 已分配设计 lead / 架构师 / DBA owner

---

## B. 必产出物

### B.1 系统架构

- [ ] 文件：`02-设计/<模块>-系统架构.md`
- [ ] 含：C4 Context / Container 图（mermaid 或飞书画板链接）
- [ ] 含：组件清单（模块 → 职责 → 技术栈 → owner）
- [ ] 含：关键业务路径的端到端时序（至少 1 条）
- [ ] 含：部署拓扑（dev / staging / prod 各环境节点）
- [ ] 含：与现有 PLM 架构的集成点说明

### B.2 数据库设计

- [ ] 文件：`02-设计/<模块>-数据库设计.md`
- [ ] 含：ER 图（mermaid `erDiagram` 或飞书画板链接）
- [ ] 含：表清单（每张表注明 owner / 主键 / 关键索引）
- [ ] 含：DDL 草案 SQL（命名按 [开发规范.md §0](../../03-开发/开发规范.md)：`tb_<entity>` / 通用 6 字段 / `del_flag`）
- [ ] 含：索引策略（哪些列必须索引、组合索引顺序）
- [ ] 含：迁移方案（新表 / 改表 / 数据迁移脚本）
- [ ] DBA 已 review DDL

### B.3 API 设计

- [ ] 文件：`02-设计/<模块>-API设计.md`
- [ ] 含：REST 端点清单（method / path / 权限串 / 请求体 / 响应体）
- [ ] 权限串符合 `business:<entity>:<action>` 规范
- [ ] 含：错误码（code → message → HTTP status → 含义）
- [ ] 含：鉴权方式（JWT Bearer）
- [ ] 含：分页 / 排序 / 过滤约定（已 follow 项目 BasePageQuery）
- [ ] **前端 lead 已 review API 契约并签字接受**
- [ ] 含：版本策略（破坏性变更如何处理）

### B.4 UI 设计（如有前端）

- [ ] 文件：`02-设计/用户旅程图/<模块>.md`（用户旅程图，mermaid `journey` 或图片）
- [ ] 文件：`02-设计/设计稿（Figma 链接）/<模块>.md`（Figma 链接 + 截图）
- [ ] 高保真稿已 review
- [ ] **测试 lead 已 review 设计可测性**（哪些场景能自动化）

### B.5 ADR（如有重大决策）

- [ ] 如本模块引入新依赖 / 改变现有架构 / 选型分歧大 → 写 ADR
- [ ] ADR 文件路径：`03-开发/ADR/NNNN-<标题>.md`
- [ ] ADR 状态：accepted（不能停在 proposed 就进入开发）

---

## C. Definition of Done（出口 DoD）

- [ ] B.1 / B.2 / B.3 全部完成，B.4（如有前端）完成，B.5（如适用）完成
- [ ] API 契约 + DB 设计 **已冻结**（冻结后改动需走变更评审，记录到本 Checklist 修订）
- [ ] 测试 lead 已基于设计估算测试工作量并加入下个 Sprint
- [ ] 评审会已召开，纪要归档到 [99-跨阶段/会议纪要/](../会议纪要/)
- [ ] 评审中识别的新风险已录入 [99-跨阶段/风险登记册.md](../风险登记册.md)
- [ ] **本 Checklist 文件已 commit 入库**（`docs(gate): <module> phase 02 passed`）

---

## D. 评审记录与签字（按 团队规模 调整必填角色数）

按团队规模需达成的最少签字角色数：`solo`=1（自评 `[solo-review]`）/ `small`=2 / `medium`=3 / `large`=5。

| 角色 | 姓名 | 评审结论 | 签字日期 |
|---|---|---|---|
| 技术 lead | | 通过 / 有条件通过 / 不通过 | YYYY-MM-DD |
| 后端架构师（medium+ 必填） | | | |
| 前端 lead（如适用） | | | |
| DBA（涉及 DB 设计必填） | | | |
| 测试 lead（medium+ 必填） | | | |
| 安全（L1 涉及鉴权 / 数据敏感时强制） | | | |

---

## E. 异常 / 例外说明

| 项 | 原因 | 补救计划 | 截止日 | 责任人 |
|---|---|---|---|---|
| | | | | |

---

## F. 进入 Phase 03 的准出确认

- [ ] API 契约文档可直接生成 Mock / Swagger（前后端能并行开发）
- [ ] DB 脚本已在 dev 环境演练成功
- [ ] 至少一个开发者已 review 并确认"可以照此实施"

✅ **签字人确认**：

| 角色 | 签字 | 日期 |
|---|---|---|
| 技术 lead | | |
| 测试 lead | | |

---

## L2 简化指引

L2 中型需求可：
- **B.1 系统架构** → 1 张组件图 + 一段技术选型说明即可
- **B.4 UI 设计** → 高保真可改用线框稿，不强制 Figma
- **D 签字** → 技术 lead + 测试 lead 两方即可
- 评审会可线上半小时（不强求线下专项会）

L1 重大需求**不可简化**。

---

## 修订记录

| 日期 | 修改人 | 原因 | 决议 |
|---|---|---|---|
| | | | |
