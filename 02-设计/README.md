# 02-设计 — Phase 02 Design

> Phase 02 把 PRD 转化为技术设计。必备产出物:`<Module>-数据库设计.md` + `<Module>-API设计.md`(每模块 2 份),通过 [Phase 02 Gate](../99-跨阶段/gate-checklists/Phase02-设计-Gate.md) 才能进入 Phase 03。

## 内容索引

| 文件 | 类型 | 说明 |
|---|---|---|
| `API 设计.md` / `数据库设计.md` / `系统架构.md` | 顶层总览 | 跨模块的总体设计(非每模块切分) |
| `UED规范.md` | 设计规范 | UED 视觉与交互规范,前端/Claude 共用 |
| `<Module>-数据库设计.md` | 每模块必备 | 表结构 + ER 图 + 索引 + 数据迁移;字段表引用 [PRD-MAPPING.md §2](../PRD-MAPPING.md) |
| `<Module>-API设计.md` | 每模块必备 | 端点清单 + 请求/响应 Schema + 错误码 + 鉴权 |
| `用户旅程图/` / `设计稿(Figma 链接)/` | 辅助产物 | Project 模块作完整范本 |

## 当前模块清单

每模块 2 份(数据库 + API),31 模块共 62 份。截至 2026-05-17:**62 份骨架已齐**。

实质内容填充建议:`<Module>-数据库设计.md` 高度可派生(直接引 SQL DDL + PRD-MAPPING §2),`<Module>-API设计.md` 高度可派生(直接引 Controller + Mapper.xml)。

## 模板使用

参考 [Project-数据库设计.md](Project-数据库设计.md) + [Project-API设计.md](Project-API设计.md)(v0.1.0 完整范本)。

## 相关规则

- [.claude/rules.md §N UED 前端视觉约束](../.claude/rules.md)
- [.claude/rules.md §O 无文档不执行](../.claude/rules.md)
