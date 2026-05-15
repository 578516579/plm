# 数据看板（链接索引）

不存看板截图，只维护链接清单。

## 看板列表

| 看板 | 用途 | 链接 | Owner | 状态 |
|---|---|---|---|---|
| 业务核心指标 | DAU/MAU、转化率、留存 | TBD（待 `stable` 阶段配置）| Wjl | 🔵 N/A — `early` 项目无外部用户 |
| 接口性能 | P95 / 错误率 / QPS | TBD（待 `stable` 阶段配置）| Wjl | 🔵 N/A — `early` 阶段以人工巡检替代 |
| JVM / 容器 | CPU / 内存 / GC | TBD（待容器化部署）| Wjl | 🔵 N/A — 本机 dev 阶段，`tasklist` / Druid 替代 |
| 数据库 | 慢 SQL / 连接数 / 主从延迟 | http://localhost:8081/druid（本机） | Wjl | 🟡 已就位（账号见 `.env.example`），仅本机可达 |
| 安全 | 登录异常 / 越权告警 | TBD | Wjl | 🔵 N/A — `internal-tool` + solo，威胁面小 |

> **状态图例**：🟢 已上线监控 / 🟡 已就位但功能受限 / 🔵 暂不适用，待项目成熟度提升后启用
>
> **`early` 阶段替代方案**：
> - 服务存活：`tasklist | findstr java.exe`（看 PID 是否仍在）
> - 错误监控：服务 stdout `grep ERROR`
> - 慢 SQL：Druid 监控台（本机即可）
> - 业务数据量：`SELECT COUNT(*) FROM tb_project`

## 平台

可选：Grafana / Metabase / 飞书多维表格仪表盘 / 阿里云 ARMS / 友盟 / GrowingIO。

## 看板规约

- 每个看板必须有 owner（人 + 角色）
- 关键指标必须配告警（阈值、接收人）写在 [05-上线/Runbook.md](../../05-上线/Runbook.md)
