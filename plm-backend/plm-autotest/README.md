# plm-autotest

| 字段 | 值 |
|---|---|
| 模块中文名 | 自动化测试 |
| 业务阶段 | 测试 |
| 当前状态 | **stub**（空壳骨架） |
| 计划启动 | deferred |
| AgriPLM 映射 | 见 [99-跨阶段/AgriPLM-模块映射-2026-05-16.md](../../99-跨阶段/AgriPLM-模块映射-2026-05-16.md) |
| Java 包路径 | `cn.com.bosssfot.dv.plm.autotest` |

## 状态说明

本模块为 Phase B 阶段拉起的空壳。当前只占位 Maven 坐标，未实现任何业务功能。

## 启动 checklist (实现时跑)

- [ ] 复用 `plm-project` 模板创建 Domain / Mapper / Service / Controller (放在 `autotest/` 包下)
- [ ] 走 Phase 01 → 02 → 03 Gate 完成立项 → 设计 → 开发
- [ ] DB 表 `tb_autotest` + 字典 + 菜单
- [ ] 前端 `plm-frontend/packages/plm-autotest/` 实现 api / types / views
- [ ] E2E 用例添加到 `plm-backend/scripts/check-encoding-runtime.sh`
