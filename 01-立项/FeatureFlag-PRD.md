# PRD: FeatureFlag 模块 — 功能开关 (DevOps 扩展)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.1 (半实质,2026-05-17 由 Claude 派生于 PRD-MAPPING §2 + AgriAI PRD DevOps 扩展 + 原型 featureflag.html) |
| 作者 | Wjl |
| PRD § | DevOps 扩展(AgriAI-PLM-完整PRD文档.md DevOps 子域) |
| 原型 HTML | [featureflag.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/featureflag.html) (开关列表 + slider + 策略 radio) |
| 评审状态 | skeleton-plus(头部+字段+状态机+AI 已填,业务深度段落待人工) |
| 字段 SSoT | [PRD-MAPPING.md §2 "FeatureFlag (DevOps)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点
<待人工填写>:类似 Inception-PRD §1.1 的 3-4 个具体痛点(灰度发布靠代码注释切换 / 多环境隔离差 / 紧急熔断无统一开关 / 用户分群灰度难)。

### 1.2 目标 (北极星指标)
<待人工填写>:类似 Inception-PRD §1.2,引用 DevOps 扩展验收标准 + 模块特有衡量指标(灰度判定延迟 < 100ms / 开关数量)。

### 1.3 不做的事 (Out of Scope)
本期**不做**:
- **A/B 实验分析** — 仅二态开关,实验留 v0.5+
- **服务端 SDK 拦截器** — 仅 check API,SDK 拦截器留 v0.5+
- **配置变更审批流** — 仅直改,审批留 v0.3
- **流量染色 / 全链路灰度** — 留 v0.5+

---

## 2. 用户与场景

### 2.1 角色
<待人工填写>:DevOps / 后端开发 / PM / SRE。

### 2.2 典型场景

**S1 灰度发布**(最高频)
<待人工填写>:1 段叙述,新功能上线先 canary 1-99% → 全量 100% → 异常关闭 0%

**S2 紧急熔断**(关键场景)
<待人工填写>:线上故障 → 一键 rolloutPercentage=0(all_off)→ 流量下沉到旧逻辑

**S3 多环境隔离**(关键约束)
<待人工填写>:(flagKey, environment)唯一,test/staging/prod 独立配置

**S4 灰度判定**(实时 API)
<待人工填写>:GET /business/feature-flag/check?flagKey=&environment=&userId= — canary 用 `Math.abs(Long.hashCode(userId)) % 100 < rolloutPercentage`

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "FeatureFlag (DevOps)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: flagId / flagNo(FF-YYYY-NNNN)
- 用户输入: flagKey(snake_case)/ title / description / environment / rolloutPercentage / rolloutStrategy / targetUserSegment
- 流程: status(2 态) / authorUserId

**唯一键**: (flagKey, environment) 唯一

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) feature-flag 行:`00↔01` (开启/关闭)。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 开启 | {01 关闭} | 默认初始状态 |
| 01 | 关闭 | {00 开启} | 反向边互转 |

**特殊规则**:
- **策略-百分比一致性硬校验**:all_on=100 / canary 1-99 / all_off=0,不一致抛 703
- (flagKey, environment) 唯一(701)
- environment 字典白名单(test/staging/prod)抛 604

---

## 5. AI 能力

### 5.1 AI 端点
(本模块无 AI 端点。FF 是基础设施型组件。)

### 5.2 当前阶段实现
n/a

### 5.3 mock 输出 / Dify 工作流
n/a

---

## 6. 验收标准

**DevOps 扩展验收**:
- ⏳ 实时灰度判定延迟 < 100ms
- ⏳ 3 环境隔离

**模块特有验收**:
<待人工填写>:E2E 测试 / 策略-百分比一致性单测 / 唯一键冲突 / 灰度算法单测(hashCode 均匀性)。

---

## 7. 不做的事 — 详 §1.3

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [FeatureFlag-数据库设计.md](../02-设计/FeatureFlag-数据库设计.md)
- API 设计: [FeatureFlag-API设计.md](../02-设计/FeatureFlag-API设计.md)
- 测试计划: [FeatureFlag-测试计划-2026-05-17.md](../04-测试/FeatureFlag-测试计划-2026-05-17.md)
- 发布计划: [FeatureFlag-发布计划-2026-05-17.md](../05-上线/FeatureFlag-发布计划-2026-05-17.md)
- 原型: [featureflag.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/featureflag.html)
- AgriAI PRD: [DevOps 扩展](../prd和原型/AgriAI-PLM-完整PRD文档.md)
