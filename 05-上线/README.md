# 05-上线 — Phase 05 Release

> Phase 05 把通过 Phase 04 测试的代码部署到生产。必备产出物:`<Module>-发布计划-YYYY-MM-DD.md`(每模块 1 份),通过 [Phase 05 Gate](../99-跨阶段/gate-checklists/Phase05-上线-Gate.md) 才能进入 Phase 06。

## 内容索引

| 文件 | 类型 | 说明 |
|---|---|---|
| `Changelog.md` | 跨模块 | 全项目版本变更日志,按 v 标 tag |
| `Runbook.md` | 跨模块 | 生产环境运维手册(回滚 / 应急 / 监控) |
| `上线 Checklist.md` | 跨模块模板 | 上线前必检查清单 |
| `发布计划.md` | 顶层模板 | 供新模块复制 |
| `<Module>-发布计划-YYYY-MM-DD.md` | 每模块必备 | 发布范围 / 步骤 / 回滚 / 监控 |

## 当前模块清单

每模块 1 份发布计划,31 模块共 31 份。截至 2026-05-17:**31 份骨架已齐**(Project v0.1.0 已实质化,其余 30 份待人工填实质内容)。

## 发布流程

参考 [Project-发布计划-2026-05-15.md](Project-发布计划-2026-05-15.md) 范本:
1. DB 迁移: `mysql plm < sql/business-<entity>.sql`
2. 后端发布: `mvn install` + 重启 plm-admin
3. 前端发布: `npm run build` + 部署
4. 烟雾测试: 菜单访问 + 关键状态机演进 + E2E 全套
5. 监控: 看 Druid + 应用日志 + 关键告警
6. Changelog 追加 + git tag v0.x.y

## 回滚

每个 `<Module>-发布计划` 必须包含:
- DB rollback SQL 路径
- Code git revert commit ref
- 字典/菜单清理 rollback SQL

## 相关规则

- [.claude/rules.md §F 提交规范](../.claude/rules.md)
- [99-跨阶段/模块工作流.md Phase 05 段](../99-跨阶段/模块工作流.md)
- [.claude/rules.md §O 无文档不执行](../.claude/rules.md)
