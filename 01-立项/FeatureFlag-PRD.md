# PRD: FeatureFlag 模块 — Feature Flag 灰度发布 (DevOps)

## 文档信息

| 字段 | 值 |
|---|---|
| 版本 | v1.2 (完整实质,2026-05-17 由 Claude 基于 9 个范本批量升级) |
| 作者 | Wjl |
| PRD § | DevOps(AgriAI-PLM-完整PRD文档.md DevOps 章节 灰度发布与紧急开关) |
| 原型 HTML | [featureflag.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/featureflag.html) (rolloutPercentage slider + 策略 radio + 目标用户分群) |
| 评审状态 | pending (待 Phase 01 Gate 评审) |
| 关联 ADR | 策略-百分比一致性硬校验 + UNIQUE(flagKey, environment) |
| 关联 OKR | _2026 Q2-O6-KR4: FeatureFlag 模块上线,功能开关响应 ≤ 50ms_ |
| 字段 SSoT | [PRD-MAPPING.md §2 "FeatureFlag (DevOps)"](../PRD-MAPPING.md) |

---

## 1. 背景与目标

### 1.1 现状痛点

PLM 团队当前没有 Feature Flag 能力,4 个具体问题:

1. **灰度发布靠 nginx 流量切分**:nginx 切流量配置改动需 SRE 介入,**Q1 某新功能想灰度 10% 调试,SRE 协调耗费 2h**,开发等待。
2. **紧急开关缺位**:线上 bug 想"快速关停某功能"只能 hotfix 发布,**Q1 出现过 1 次紧急开关需 3h 走完发版流程**,损失严重。
3. **同用户灰度命中不一致**:不同请求同一用户有时看到新功能、有时看不到,**hash 算法不统一导致用户体验割裂**。
4. **环境隔离缺位**:test 环境的 Feature Flag 与 prod 环境配置混杂,**测试通过的开关到 prod 发现配置错**。

### 1.2 目标 (北极星指标)

**目标**:6 个月内沉淀 ≥ 100 个 Feature Flag,做"3 策略 + 3 环境 + hash 一致性 + 紧急开关"四件套。

**衡量指标**:
- **功能开关响应 ≤ 50ms**(GET /business/feature-flag/check)
- **3 策略覆盖率**(all_on / canary / all_off)
- **同用户 hash 一致 100%**(同 user 多次请求看到同样的开关状态)
- **环境隔离 100%**(test/staging/prod 完全分离,UNIQUE(flagKey,environment))
- **紧急开关响应 ≤ 1 分钟**(从决策到 0% 流量)

### 1.3 不做的事 (Out of Scope)

本期**不做**:
- **A/B 测试统计**(开关 A vs 开关 B 的转化率对比)— 留 v0.3 走 Analytics
- **基于用户属性灰度**(地域 / 设备 / VIP 等级)— 仅 userId hash + 全开 / 全关,属性灰度留 v0.3
- **Feature Flag 自动过期清理** — 仅手动管理,自动清理留 v0.5+
- **灰度时间窗**(灰度 1 周后自动 all_on)— 留 v0.3
- **Feature Flag 与代码关联**(用了哪个 flag 在哪个 Controller)— 留 v0.5+
- **客户级别开关**(同 prod 不同客户不同配置)— 留 v0.5+

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 权限 | 典型动作 |
|---|---|---|
| **DevOps / SRE** | CRUD 自己负责的 flag | 创建灰度 / 紧急关停 |
| **开发** | 查看 + 创建(test 环境) | 灰度调试 |
| **管理员** | 全 CRUD | 跨环境配置 + 紧急决策 |
| **业务用户** | 间接消费(通过 check 接口) | 透明 |

### 2.2 典型场景

**S1 灰度发布**(最高频)
> 开发完成新功能 "AI 灌溉推荐 v2 算法" → DevOps 进入 Feature Flag 菜单 → 新建 → flagKey="ai_irrigation_v2" + title "AI 灌溉算法 v2 灰度" + environment="prod" + rolloutPercentage=10 + rolloutStrategy="canary"(3 值:all_on/canary/all_off)→ status='00 开启' → 10% 用户看到新功能

**S2 策略-百分比一致性硬校验**(关键流程,业务硬规则)
> DevOps 错误地选 rolloutStrategy="canary" 但 rolloutPercentage=100 → **Service 校验:canary 必须 1-99**,百分比 100 应改 strategy="all_on" → 抛 ServiceException(703 业务硬规则)

**S3 业务模块查询**(最高频,核心接口)
> 业务模块代码:`POST /business/feature-flag/check?flagKey=ai_irrigation_v2&environment=prod&userId=12345` → Service:
> 1. 查找 (flagKey, environment) UNIQUE 记录 → 找到 status='00 开启'
> 2. canary 策略 → 计算 `Math.abs(Long.hashCode(12345)) % 100 = 67`
> 3. 67 >= rolloutPercentage=10 → 返回 false(不命中)
> 4. 业务降级到 v1 算法

**S4 紧急关停**(关键流程)
> 线上发现 v2 算法 bug → DevOps 立即改 rolloutStrategy="all_off" + rolloutPercentage=0 → 1 分钟内所有用户回到 v1

**S5 环境隔离**(关键流程,UNIQUE 约束)
> 同样的 flagKey="ai_irrigation_v2" 可在 test/staging/prod 3 环境各有独立配置 → UNIQUE(flagKey, environment) 不冲突 → 测试通过 staging 后切 prod

---

## 3. 字段定义

**单一事实来源**: [PRD-MAPPING.md §2 "FeatureFlag (DevOps)"](../PRD-MAPPING.md)。本 PRD 不重复字段表,字段层 drift 走 §M.2 流程。

字段一览(详 SSoT):
- 基础: flagId / flagNo (`FF-YYYY-NNNN`)
- 标识: flagKey(snake_case,UNIQUE 联合 environment)
- 用户输入: title / description / environment(3 值字典:test/staging/prod)
- 灰度配置: rolloutPercentage(0-100)/ rolloutStrategy(3 值:all_on/canary/all_off)/ targetUserSegment(CSV 或表达式)
- 流程: status(2 态开启/关闭)/ authorUserId

---

## 4. 状态机

详 [PRD-MAPPING.md §3](../PRD-MAPPING.md) feature-flag 行:2 态开启/关闭。

| status | label | 转入 | 说明 |
|---|---|---|---|
| 00 | 开启 | {01 关闭} | 默认初始;check 接口正常判定 |
| 01 | 关闭 | {00 开启} | 反向可重启;check 接口一律 false |

**特殊规则**:
- 新建强制 `status='00'`(违反抛 601)
- environment 3 值字典白名单(604)
- rolloutStrategy 3 值字典白名单(604)
- rolloutPercentage 范围 [0, 100](602)
- **业务硬规则 703 策略-百分比一致性**:
  - all_on → rolloutPercentage 必须 100
  - canary → rolloutPercentage 必须 1-99
  - all_off → rolloutPercentage 必须 0
- **UNIQUE(flagKey, environment)**:违反抛 701
- flagKey 必须 snake_case(602)

---

## 5. AI 能力

### 5.1 业务入口

`GET /business/feature-flag/check?flagKey=&environment=&userId=` — 实时判定,响应 ≤ 50ms。

### 5.2 当前实现

判定算法:
1. 查 (flagKey, environment) UNIQUE 记录
2. status='01 关闭' → 直接 false
3. all_on → true / all_off → false
4. canary:`Math.abs(Long.hashCode(userId)) % 100 < rolloutPercentage` → 命中 true

**本期无 AI 端点**。

### 5.3 路线图

- v0.3: 基于用户属性灰度(地域 / 设备 / VIP)
- v0.3: 灰度时间窗自动 all_on
- v0.5+: A/B 统计 / 客户级开关

---

## 6. 验收标准

**DevOps FeatureFlag 验收**:
- ⏳ **3 策略支持**(all_on/canary/all_off 字典就位)
- ⏳ **3 环境隔离**(test/staging/prod + UNIQUE(flagKey,environment))
- ⏳ **canary hash 一致性**(同 userId 多次请求同结果)
- ⏳ **紧急开关响应 ≤ 1 分钟**(改 status='01' 立即生效)

**模块特有验收**(本会话已落地):
- 2 态状态机合法转换单测覆盖
- environment / rolloutStrategy 字典白名单(604)
- rolloutPercentage 范围校验(602)
- **策略-百分比一致性 703 业务硬规则**单测覆盖
- **UNIQUE(flagKey, environment) → 701** 单测覆盖
- check 接口 hash 一致性 + 性能 ≤ 50ms

---

## 7. 不做的事 — 详 §1.3

- A/B 统计 / 属性灰度 / 自动过期 / 时间窗 / 代码关联 / 客户级开关

---

## 8. 关联文档

- 字段层 SSoT: [PRD-MAPPING.md §2](../PRD-MAPPING.md)
- 数据库设计: [FeatureFlag-数据库设计.md](../02-设计/FeatureFlag-数据库设计.md)
- API 设计: [FeatureFlag-API设计.md](../02-设计/FeatureFlag-API设计.md)
- 测试计划: [FeatureFlag-测试计划-2026-05-17.md](../04-测试/FeatureFlag-测试计划-2026-05-17.md)
- 发布计划: [FeatureFlag-发布计划-2026-05-17.md](../05-上线/FeatureFlag-发布计划-2026-05-17.md)
- 原型: [featureflag.html](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/featureflag.html)
- AgriAI PRD: [DevOps](../prd和原型/AgriPLM-DevOps-原型/agriplm_split/devops.html)
- 关联模块: [Release-PRD.md](Release-PRD.md)(发布时启用 flag)/ [Pipeline-PRD.md](Pipeline-PRD.md)
