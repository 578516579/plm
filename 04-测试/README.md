# 04-测试 — PLM 测试阶段产出物

测试相关的过程文档与自动化资产入口。

## 目录结构

```
04-测试/
├── README.md                      # 本文件 — 入口与导航
├── 测试策略.md                    # 测试架构总览 / 工具栈 / Gate 接入 / 路线图（上位文档）
├── 测试计划.md                    # 当前期测试范围/策略/资源/退出标准
├── Project-测试计划-2026-05-15.md # 历史 — Project 模块单独测试计划样本
├── 安全审计.md                    # 安全测试输出
├── 性能报告/                      # 性能测试输出
└── 测试用例库/                    # 用例与自动化资产
    ├── README.md
    ├── E2E-测试矩阵.md            # 自动化用例总览（v2.0 — 91 case / 24 spec）
    ├── E2E-运行手册.md            # 跑测试的命令 + 排错 + CI 集成
    ├── E2E-测试数据.md            # fixtures / RUN_ID 隔离 / 状态机矩阵
    ├── Project-functional.md      # Project 功能用例
    ├── Project-api.md             # Project 接口用例
    └── Project-e2e.md             # Project E2E 场景
```

## 自动化测试速查

| 我想… | 去哪 |
|---|---|
| 跑 E2E（命令） | [E2E-运行手册.md §2](测试用例库/E2E-运行手册.md) |
| 看现有 case 覆盖了什么 | [E2E-测试矩阵.md §2](测试用例库/E2E-测试矩阵.md) |
| 新增模块加 spec | [E2E-测试矩阵.md §1.1](测试用例库/E2E-测试矩阵.md) 找模板模块 |
| 测试失败排查 | [E2E-运行手册.md §4](测试用例库/E2E-运行手册.md) |
| Claude 跑 E2E 帮我 | [.claude/skills/plm-e2e/](../.claude/skills/plm-e2e/SKILL.md) 或 `/e2e-run` |
| 集成 CI/CD | [E2E-运行手册.md §5](测试用例库/E2E-运行手册.md) GitHub Actions 模板 |
| 字符编码乱码问题 | [E2E-运行手册.md §4.6](测试用例库/E2E-运行手册.md) + [03-开发/字符编码规范.md](../03-开发/字符编码规范.md) |

## Phase 03 → Phase 04 准入

**强制流程**（[.claude/rules.md §G.4](../.claude/rules.md)）：

1. 启动后端（带 `-Dfile.encoding=UTF-8` 等 4 个标志）+ 前端 + MySQL + Redis
2. 跑 `npm run test:e2e:encoding`（6 case 必须全过 — **P0 守门员**）
3. 跑 `npm run test:e2e` 全套件 91+ case（任何 fail 不允许进 Phase 04）
4. 把通过证据写进 `99-跨阶段/gate-checklists/instances/<模块>/Phase03-Gate-*.md` §I

## 各类测试归属

| 类型 | 工具 | 位置 |
|---|---|---|
| 单元测试 | JUnit / Vitest | 源码 `src/test/`（不入本目录） |
| 接口测试 | Playwright APIRequestContext | [plm-frontend/e2e/](../plm-frontend/e2e/) |
| 集成测试 | Playwright | 同上 |
| UI 自动化 | Playwright | 同上 |
| 性能测试 | (TBD — 见 [性能报告/](性能报告/)) | 单独 |
| 安全测试 | OWASP ZAP / 手测 | [安全审计.md](安全审计.md) |
| 回归测试 | Playwright 全套件 | [E2E-测试矩阵.md](测试用例库/E2E-测试矩阵.md) |

## 上位策略文档

- [测试策略.md](测试策略.md) — 测试架构总览 / 工具栈 / Gate 接入 / 路线图
