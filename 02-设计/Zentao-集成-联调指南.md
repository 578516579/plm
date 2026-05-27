# 禅道(ZenTao)双向同步 — 联调指南

> 配套 [02-设计/Zentao-集成-设计.md](Zentao-集成-设计.md) / [Proposal 0014](../99-跨阶段/proposals/0014-zentao-bidirectional-sync.md)
> 适用于:有真实禅道实例 + 测试账号的端到端联调场景

---

## 1. 前置条件

- 后端已重启,加载新代码(`plm-integration`)
- DB 已应用 `business-integration-zentao.sql`(4 张业务表加 external_* 列 + tb_integration_user_mapping)
- 禅道实例可被 PLM 服务器访问(HTTP/HTTPS)
- 禅道实例有一个 API 账号(account/password,**不是**普通用户登录密码,需要在禅道后台「组织 → 用户 → API 设置」开 API 权限)

---

## 2. 配置步骤(在 PLM UI)

### 2.1 应用 DDL

```bash
MYSQL='/c/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe'
"$MYSQL" -uroot -p"$DB_PASSWORD" --default-character-set=utf8mb4 plm < sql/business-integration-zentao.sql
# 验证:tb_defect / tb_requirement / tb_task / tb_testcase 多了 external_source / external_id / external_url 三列
# 验证:tb_integration_user_mapping 表已创建
```

回滚:
```bash
"$MYSQL" -uroot -p"$DB_PASSWORD" --default-character-set=utf8mb4 plm < sql/business-integration-zentao-rollback.sql
```

### 2.2 配置禅道 Connector

登录 PLM admin,菜单「外部集成 → 连接器配置」,点「新增」:

| 字段 | 值 |
|---|---|
| 编码 | `ZENTAO-MAIN`(或自定义,unique) |
| 名称 | `禅道主实例` |
| 类型 | 禅道(zentao) |
| 鉴权 | App Secret(本期复用此类型) |
| 外部系统基址 | `https://zentao.example.com`(不带末尾斜杠) |
| 凭据 JSON(明文输入,后端 AES-256-GCM 加密存储)| `{"account":"plm-bot","password":"xxxxx"}` |
| Webhook 验签 secret | 32 字节随机字符串,自己生成,**记下来填到禅道侧** |
| 配置 JSON | 见下 §2.3 |
| 状态 | 启用(0) |

### 2.3 config_json 关键字段

```json
{
  "productProjectMap":   { "5": 1, "6": 2 },
  "executionSprintMap":  { "12": 3 },
  "outboundCreateOnNew": false,
  "outboundFields":      ["title","status","severity","priority","assignedTo"]
}
```

- `productProjectMap`:禅道 productId → PLM projectId(必填,缺映射的对象 webhook 入站失败)
- `executionSprintMap`:禅道 executionId → PLM sprintId(可选,任务可不挂 sprint)
- `outboundCreateOnNew`:PLM 新建的 defect/req/task/case 是否自动反推禅道(本期实现为 false,留 v0.6+ 完善)
- `outboundFields`:出站 PUT 时只发送白名单字段,留空 = 全发

### 2.4 测试连通性

在连接器列表点「测试」按钮:
- ✅ 成功:`OK, zentao token=xxxx***xxxx`
- ❌ 813:账号/密码错
- ❌ 814:endpoint 不可达

### 2.5 配置禅道侧 Webhook

在禅道后台「通用 → Webhook → 新建」:

| 字段 | 值 |
|---|---|
| 类型 | 通用 / 自定义 |
| URL | `https://plm.example.com/dev-api/integration/webhook/zentao/{connectorId}`(替换 {connectorId} 为 PLM connector 表的 id)|
| Method | POST |
| 密钥参数 | 自定义 header **`X-Zentao-Token`**,值 = PLM connector.webhook_secret |
| 事件 | 勾选 Bug / Story / Task / Case 的 opened / edited / closed |

### 2.6 配置用户映射(可选,出站需要)

菜单「外部集成 → 用户映射」,为每个会触发同步的 PLM 用户添加映射:
- PLM user → 禅道 account

缺映射时:
- 入站:reporter/assignee 留 null,不影响落库
- 出站:assignedTo 默认指给 connector 创建人(本期 fallback)

---

## 3. 联调用例

### 3.1 入站:禅道 → PLM(bug → defect)

1. 在禅道建一个 Bug(产品 = 已配 productProjectMap 的 product)
2. 等 ~1s 看 PLM 后端日志:`[plm-integration/zentao] 入站新建 defect external_id=...`
3. 在 PLM 缺陷模块查看是否有新记录(defect_no 形如 `ZT-BUG-1234`)
4. 在「外部集成 → Webhook 事件」查看 process_status=2(成功)

### 3.2 入站冲突合并(stale)

1. PLM 改一条已同步的 defect(update_time 推到 T+1)
2. 在禅道改同一个 bug(lastEditedDate 在 T 之前 — 用历史时间戳模拟,实际禅道会刷新此字段)
3. 期望:webhook event 落库 process_status=4 已忽略,error="[errorCode=819] stale: PLM newer"

### 3.3 出站:PLM → 禅道(defect → bug)

1. PLM 找一条已同步过的 defect(external_source='zentao', external_id 非空)
2. 修改其 status(如 1 → 2)
3. 后端日志:`[plm-integration/zentao-outbound] 反推 defect external_id=... 成功`
4. 在禅道侧验证 bug.status 已变成 `resolved`

### 3.4 防循环(双向同步关键)

1. 在禅道改 bug.status → resolved
2. PLM 收到 webhook,落库 status=2
3. **关键**:不应该再触发出站反推(SyncContext.inbound 拦截)
4. 验证:Webhook 事件流水中只有 1 条入站记录,无后续出站日志

---

## 4. 排错速查

| 现象 | 原因 | 处理 |
|---|---|---|
| ping 返回 813 | account/password 错 | 重新填凭据 |
| ping 返回 814 | endpoint 写错 / 网络不通 | curl 测试 endpoint |
| webhook 401(X-Zentao-Token 不匹配) | 禅道侧 secret 与 PLM connector.webhook_secret 不一致 | 改禅道侧或 PLM 侧使一致 |
| webhook 200 但 PLM 无新记录 | process_status=4(stale 或 productProjectMap 缺) | 看 process_error 字段 |
| 出站不触发 | SyncContext.inbound 拦截了 / external_id 为空 / 防抖 60s 内 | 看后端 debug 日志 |
| 出站 401 | 禅道 token 过期且自动刷新失败 | 检查 PLM connector 凭据是否仍有效 |

详细错误码见 [02-设计/Zentao-集成-设计.md §10](Zentao-集成-设计.md)。

---

## 5. 衡量指标(参 Proposal 0014 §8)

跑通联调后,留观 4 周(2026-05-25 ~ 2026-06-30),关注:
- 入站成功率 ≥ 95%
- 出站成功率 ≥ 90%
- 同步死锁 = 0
- 误覆盖 ≤ 1 起/月

未达成 → 在 Proposal 0014 §10 写"为什么失败"并回滚。
