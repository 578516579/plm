# Proposal 0101: Task MR URL 字段加 host 白名单校验，防钓鱼链接

## 元信息

| 字段 | 值 |
|---|---|
| 编号 | 0101 |
| 标题 | Task / Defect / 其他业务模块的"外部 URL" 字段（MR / PR / issue link 等）必须走 host 白名单校验 |
| 状态 | **proposed** |
| 类型 | 编码规范（安全相关）|
| 提出人 | Wjl + Claude（reflect/2026-W21 批量升格）|
| 提出日期 | 2026-05-17 |
| 来源 | signals 候选 **0025** |
| 评审截止 | 2026-05-24 |
| Tracking 截止 | merged 后 2 周 |

---

## 1. 背景

Task 模块 Phase 03 §J friction 1：Task 实体的 `mergeRequestUrl` 字段当前只做 URL 格式校验（含 `://`），不限制 host。

风险：
- 内部 PM 把任意外部链接（钓鱼站 / 恶意页）填到 MR URL 字段
- 其他人在 Task 详情页点击跳转 → 中招
- 内部 PLM 系统变成钓鱼跳板

PLM 适用场景：MR URL 只应指向团队 git 平台（GitLab / Gitee / GitHub 内部组织），白名单 host 即可阻断 99% 钓鱼场景。

---

## 2. 证据

- 关联 signals: [2026-05.md](../signals/2026-05.md) 候选 0025
- 关联 reflect: [reflect/2026-W20.md](../reflect/2026-W20.md) B2
- 关联 Gate 实例: Task Phase 03 §J friction 1
- 适用扩展：Defect 实体的 `referenceUrl`、Document `externalLink` 等任何业务字段含 URL 都适用

---

## 3. 提案

### 3.1 改 `03-开发/开发规范.md` 加 §Y "外部 URL 字段白名单"

```diff
+## §Y. 外部 URL 字段白名单（强制，proposal 0101）
+
+任何业务实体含"外部 URL"字段（命名后缀 `Url`/`Link`/`Href`）必须：
+
+1. 在 application.yml 配 `plm.url.allowed-hosts: <host-list>`（默认空，强制配置）
+2. ServiceImpl 入口（add / edit）校验 URL host ∈ allowed-hosts
+3. 校验失败抛 `ServiceException(code=708, "URL host 不在白名单")`
+4. 配置项分环境（dev/staging/prod），dev 可放宽（如 + `localhost`）
+
+错误码 708 已在 [PRD-MAPPING.md §4](../PRD-MAPPING.md) 登记。
+
+### 适用清单（当前业务模块）
+
+| 模块 | URL 字段 | 默认白名单（example）|
+|---|---|---|
+| Task | `mergeRequestUrl` | gitlab.内网域 / gitee.com |
+| Defect | `referenceUrl` | 同上 |
+| Document | `externalLink` | 团队 wiki 域 |
+
+### 工具
+
+- ServiceImpl 复用 `cn.com.bosssfot.dv.plm.common.utils.UrlValidator.checkHost(url, allowedHosts)`（待实现，已记入 Sprint backlog）
```

### 3.2 同步更新 `.claude/rules.md` §C（凭据 / 安全段）— 加 1 行

```diff
 - **永远不要**把真实 password / token / API key / JWT secret 写进 yml 或代码。
 ...
+- 业务实体含外部 URL 字段（命名后缀 Url/Link/Href）必须 host 白名单校验（per [proposal 0101](../99-跨阶段/proposals/0101-mr-url-host-whitelist.md)）
```

---

## 4. 影响范围

| 受众 | 影响 |
|---|---|
| Task / Defect / Document 等已含 URL 字段的模块 | ServiceImpl 加 host 校验；application.yml 加 `plm.url.allowed-hosts` |
| 业务模块生成器 | 模板默认含 URL 字段时自动产 host 校验代码（W22 验证） |
| dev 环境 | 默认白名单含 localhost，避免开发期 friction |

---

## 5. 风险

- **风险 1**: 白名单配置遗漏 → 业务方加内部新 git 平台时不在白名单。**缓解**: 校验失败的 error message 提示"运维更新 allowed-hosts"。
- **风险 2**: 子域名匹配复杂（`*.gitlab.com` vs `gitlab.com`）。**缓解**: utils 用现成库（如 `InternetDomainName.from(host).topPrivateDomain()`）。

---

## 6. 备选方案

- A: 客户端 (前端) 校验 — 不选，客户端可绕过
- B: 黑名单 — 不选，黑名单永远不全
- C（选定）: 服务端白名单 + 错误码 708 显式拒绝

---

## 7. 实施计划

```
[x] Step 1: 写 proposal
[ ] Step 2: 评审（后端 lead + 安全角度）
[ ] Step 3: 实现 UrlValidator 工具类（plm-common）
[ ] Step 4: 改 03-开发/开发规范.md §Y + .claude/rules.md §C
[ ] Step 5: Task / Defect / Document Service 加 host 校验
[ ] Step 6: 配 application*.yml 白名单（dev / staging / prod 各自）
[ ] Step 7: tracking 期 grep 是否还有未校验 URL 字段
```

---

## 8. 衡量指标

| 信号 | 基线 | 目标 |
|---|---|---|
| 业务实体 URL 字段含 host 校验比例 | 0%（Task/Defect 当前 0）| 100%（含 Task/Defect/Document）|
| 错误码 708 命中次数（W22+ 抽样）| N/A | ≥ 1（验证白名单生效）|

Tracking 期: merged 后 2 周。

---

## 9. 评审记录

| 评审人 | 立场 | 日期 | 备注 |
|---|---|---|---|
| _(待)_ | | | |

---

## 10. 实施后跟踪

待 merged 后回填。

---

## 修订记录

| 日期 | 修订人 | 改了什么 |
|---|---|---|
| 2026-05-17 | Wjl + Claude | 首版从 signals 候选 0025 升格 |
