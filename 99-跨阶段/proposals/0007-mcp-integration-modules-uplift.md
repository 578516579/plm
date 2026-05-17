# Proposal 0007: 把 MCP 与外部集成（飞书/GitLab/...）从 v0.5+ 提前到当前

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0007 |
| 标题 | 把 MCP/Integration 模块从 v0.5+ deferred 提到当前迭代（独立 Maven 模块 + 真实连通） |
| 状态 | **merged**（User-requested-bypass） |
| 类型 | 架构 |
| 提出人 | Wjl + Claude |
| 提出日期 | 2026-05-17 |
| 评审人 | Wjl (solo-review) |
| 评审日期 | 2026-05-17 |
| Tracking 截止 | 2026-06-30 |

---

## 1. 背景（What's the problem?）

[99-跨阶段/AgriPLM-模块映射-2026-05-16.md](../AgriPLM-模块映射-2026-05-16.md) 把 "MCP 集成 (GitLab/飞书/Figma/Jira/钉钉)" 显式标为 `v0.5+,且评估必要性`（line 15-16 与 §3 line 123）。基于的判断是 "internal-tool 早期可手动同步"。

但实际诉求出现得更早：

- AgriPLM PRD §2.5 / §3.4 / §3.5 Phase 1 明确把 "基础 MCP Server" 列为 Phase 1（即 MVP），Phase 2 列 "GitLab/飞书集成"
- 团队主沟通渠道是飞书（已有 lark-cli skill，11 个 lark-* skill），手动同步 ROI 极低
- Claude Code / Cursor 已经在仓内日常使用，但目前没有 PLM 的 MCP 工具暴露，AI 没法直接读写 PLM 数据 → 自进化反馈环掉一只手
- 用户在 2026-05-17 会话中明确要求：「设计增加 mcp 模块，和飞书、gitlab 等，可以配置、对接、管理」

继续按 v0.5+ deferred 意味着 5-8 周内无法启动这条线，与显式诉求冲突。

---

## 2. 证据（Evidence）

- 关联 PRD：[prd和原型/AgriAI-PLM-完整PRD文档.md](../../prd和原型/AgriAI-PLM-完整PRD文档.md) §2.5（MCP 工具集）、§3.4（MCP Server 实现）、§3.5 Phase 1（基础 MCP Server）、§4.1 信息架构（系统设置 → MCP集成配置）
- 关联原型：[prd和原型/AgriPLM-DevOps-原型/agriplm_split/settings.html:138-187](../../prd和原型/AgriPLM-DevOps-原型/agriplm_split/settings.html) Tab "MCP集成" + `mcpTable`
- 关联映射 drift：[99-跨阶段/AgriPLM-模块映射-2026-05-16.md:15](../AgriPLM-模块映射-2026-05-16.md) `MCP 集成 → v0.5+,且评估必要性`
- 用户请求：2026-05-17 会话原话 "设计增加 mcp 模块，和飞书、gitlab 等，可以配置、对接、管理" + 后续 AskUserQuestion 选定：
    1. 模块定位 = **独立模块（plm-mcp + plm-integration）**
    2. 本轮产出 = **设计 + 后端脚手架 + 前端页面骨架 + 飞书/GitLab 真实连通**
    3. 首批接入 = **飞书 + GitLab + MCP Server 自身 + 钉钉/Jira/Figma/禅道/ZTF**

---

## 3. 提案（What's the change?）

把 MCP/Integration 从 v0.5+ 提到当前；新增 2 个独立 Maven 模块。

### 改动文件清单

| 文件 | 改动类型 |
|---|---|
| [99-跨阶段/AgriPLM-模块映射-2026-05-16.md](../AgriPLM-模块映射-2026-05-16.md) §0 第 15-16 行 + §3 第 123 行 | 修改：MCP 改为 "v0.5+" → "当前（v0.x，新增 plm-mcp + plm-integration 模块）" |
| [PRD-MAPPING.md](../../PRD-MAPPING.md) §M（新增） | 新增：MCP/Integration 模块字段对照表、状态机、错误码 |
| [02-设计/MCP-集成-设计.md](../../02-设计/MCP-集成-设计.md) | 新建：架构 + DDL + API 契约 + 安全模型 |
| `plm-backend/plm-mcp/` | 新模块 |
| `plm-backend/plm-integration/` | 新模块 |
| [plm-backend/pom.xml](../../plm-backend/pom.xml) | 新增 2 个 module + dependencyManagement |
| [plm-backend/plm-admin/pom.xml](../../plm-backend/plm-admin/pom.xml) | 加 2 个依赖 |
| [plm-backend/.env.example](../../plm-backend/.env.example) | 加 `FEISHU_*` / `GITLAB_*` / `MCP_*` 变量 |
| [plm-backend/sql/](../../plm-backend/sql/) | 加 business-mcp.sql / business-integration.sql + 回滚脚本 |
| `plm-frontend/src/views/business/mcp/` | 新增 Vue 页面 |
| `plm-frontend/src/views/business/integration/` | 新增 Vue 页面 |

### Diff 草案

```diff
--- a/99-跨阶段/AgriPLM-模块映射-2026-05-16.md
+++ b/99-跨阶段/AgriPLM-模块映射-2026-05-16.md
@@ -15,1 +15,1 @@
- | MCP 集成 (GitLab/飞书/Figma/Jira/钉钉) | 标 v0.5+,MVP 可手动同步 |
+ | MCP 集成 (GitLab/飞书/Figma/Jira/钉钉) | **提前到 v0.x（plm-mcp + plm-integration 模块），见 Proposal 0007** |

@@ -123,1 +123,1 @@
- 4. **5 个 MCP Server 集成** — 推到 v0.5+,且评估必要性
+ 4. **5 个 MCP Server 集成** — **已提前到当前迭代**（Proposal 0007）：plm-mcp 暴露 PLM 工具给 Claude Code，plm-integration 管理飞书/GitLab/钉钉/Jira/Figma/禅道/ZTF 连接器
```

---

## 4. 影响范围（Impact）

| 受众 | 影响 |
|---|---|
| 开发者 | 多 2 个 Maven 模块（plm-mcp、plm-integration），新增 4 张表（mcp_server、mcp_tool_audit、integration_connector、integration_webhook_event） |
| Claude | 多 2 个模块要遵守命名规约；后续可通过 plm-mcp 提供的工具集对 PLM 数据做读写操作（自进化反馈环闭环） |
| 测试 / 运维 | 新增 `.env` 变量（飞书 app_id/app_secret、GitLab url/token、MCP 加密 key）；webhook 入口要在 nginx/网关白名单 |
| 已有代码 / 文档 | 仅向后扩展，无破坏；菜单 ID 选 `2400-2499` 段避开现有占用 |

---

## 5. 风险（Risks）

| 风险 | 缓解 |
|---|---|
| 飞书/GitLab token 泄露 | 数据库存 token 必须 AES-256-GCM 加密，密钥从 `MCP_ENCRYPT_KEY` env 注入，禁止默认值能跑（启动期校验） |
| MCP Server 工具被未授权调用 | OAuth 2.0（Phase 1 简化为长效 token + 审计日志全记录）；所有 mcp tool 调用必须落 `mcp_tool_audit` |
| Webhook 仿冒 | 飞书走 verification_token；GitLab 走 X-Gitlab-Token；调用方 IP 留审计 |
| User-requested-bypass 后无 tracking | 本提案附 tracking 期到 2026-06-30，必须按 §8 衡量信号上线 |
| 工作量大，单次提交 review 困难 | 切 3 轮提交：①脚手架+SQL ②飞书 adapter ③GitLab adapter + MCP server endpoint |

---

## 6. 备选方案（Alternatives Considered）

- **方案 A（本提案）**：独立 2 个 Maven 模块 plm-mcp + plm-integration，菜单一级目录
- **方案 B**：落入 plm-system 作为 settings 子页（贴 PRD §4.1）→ 用户已选 A，不复述
- **方案 C**：合并成 1 个 plm-integration 模块，MCP 当 connector_type 的一种 → 长期协议层 (OAuth/SSE/JSON-RPC) 会污染通用 connector 表

选 A（用户确认）。

---

## 7. 实施计划（Implementation Plan）

```
[x] Step 1: 写 proposal + PRD-MAPPING 初版 + 设计文档（本提案的同一 commit）
[x] Step 2: plm-mcp / plm-integration 模块骨架 + SQL DDL + pom 接入
[x] Step 3: 飞书 connector adapter（消息发送 + Webhook 接收 + tenant_access_token 缓存）
[x] Step 4: GitLab connector adapter（PAT 调用 + Webhook 签名校验）
[x] Step 5: MCP Server 基础 endpoint（list_tools / call_tool）
[x] Step 6: 前端页面骨架（mcp、integration）
[x] Step 7: L1 Gate Checklist 实例填写（Phase 02 + Phase 03 各 1 份）
[ ] Step 8: 飞书/GitLab 沙箱 token 写入 .env，端到端跑通 1 个用例（需 Wjl 提供 sandbox 凭据）
```

---

## 8. 衡量指标（How will we know it worked?）

> Tracking 期内观察哪些信号？怎样算"成功"？

| 信号 | 基线 | 目标 |
|---|---|---|
| Claude Code 通过 MCP 直接读写 PLM 数据（无需手动 SQL）的会话占比 | 0% | ≥ 30% |
| 飞书机器人推送的项目状态变更通知数 / 月 | 0 | ≥ 50 条 |
| GitLab MR 合入自动写回 task.status 的次数 / 月 | 0 | ≥ 20 |
| `mcp_tool_audit` 表写入失败次数（应 0） | n/a | 0 |
| 加密 key 配置缺失而启动失败的次数 | n/a | ≤ 0（必须强制） |

跟踪期：2026-05-17 ~ 2026-06-30。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| Wjl | ✅ 通过 (solo-review) | 2026-05-17 | User-requested-bypass：未经过完整 proposal review 流程，直接合入；tracking 期内若指标未达成走回滚 |

---

## 10. 实施后跟踪（merged 后填）

### 实际 PR / commit

- PR: 待开
- 合入 commit: 同次 commit `feat(mcp): 新增 plm-mcp + plm-integration 模块脚手架 + 飞书/GitLab adapter`

### Tracking 数据

| 信号 | 基线 | 目标 | W21 | W22 | W23 | W24 |
|---|---|---|---|---|---|---|
| Claude MCP 直读 PLM 占比 | 0% | ≥30% | | | | |
| 飞书机器人推送/月 | 0 | ≥50 | | | | |
| GitLab→task 自动回写/月 | 0 | ≥20 | | | | |

### 最终判定
- [ ] done（达成目标，本提案归档）
- [ ] reverted

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 初版 + User-requested-bypass merged |
