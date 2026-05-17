# 04-测试 — Phase 04 Test

> Phase 04 验证 Phase 03 的代码符合 Phase 01-02 的设计。必备产出物:`<Module>-测试计划-YYYY-MM-DD.md` + 测试用例库 3 份子文档(每模块 4 份),通过 [Phase 04 Gate](../99-跨阶段/gate-checklists/Phase04-测试-Gate.md) 才能进入 Phase 05。

## 内容索引

| 文件 | 类型 | 说明 |
|---|---|---|
| `测试计划.md` / `安全审计.md` | 顶层模板/总览 | 跨模块通用 |
| `<Module>-测试计划-YYYY-MM-DD.md` | 每模块必备 | 范围 / 通过标准 / 风险 / 时间表 |
| `测试用例库/` | 详细用例集 | 每模块 3 份子文档(下表) |
| `性能报告/` | 专项 | 性能压测产出 |

### `测试用例库/` 子目录

| 文件 | 粒度 | 范本 |
|---|---|---|
| `E2E-测试矩阵.md` / `E2E-测试数据.md` / `E2E-运行手册.md` | 跨模块 | 整体 E2E 运行约定 |
| `<Module>-functional.md` | 用户视角 | `TC-<Module>-F001` 起 |
| `<Module>-api.md` | 接口级别 | `TC-<Module>-API-001` 起 |
| `<Module>-e2e.md` | 自动化 | Playwright `<entity>.spec.ts` 对应 |

## 当前模块清单

每模块 1 份测试计划 + 3 份用例库 = 4 份,31 模块共 124 份。截至 2026-05-17:**124 份骨架已齐**(详 [审计 2026-05-17](../99-跨阶段/audits/2026-05-17-process-docs-completeness-audit.md))。

## 测试运行

```bash
# 单测
mvn -pl plm-<entity> test

# E2E
cd plm-frontend && export DB_PASSWORD=...
npm run test:e2e -g "<Module>"
```

## 相关规则

- [03-开发/开发规范.md §5 测试规范](../03-开发/开发规范.md)
- [.claude/rules.md §M.2 §M.4 状态机来自原型](../.claude/rules.md)
- [.claude/rules.md §O 无文档不执行](../.claude/rules.md)
