# Runbook — 应急操作手册

线上常见故障的诊断与处置脚本。pager 响起时按这本翻。

## 1. 系统起不来
- 后端日志看 `Started PlmApplication` 是否出现
- 数据库连接：[gotchas #3 Redis IPv6](../CLAUDE.md) 之外，看 Druid 连接池是否打满
- 端口冲突：`netstat -ano | grep <port>`

## 2. 后端 503 / 大量超时
- 看 Druid 监控台慢 SQL 排行
- 看 Redis：`redis-cli info clients` / `info memory`
- 看 JVM：`jstack <pid>` / GC 日志
- 临时降级：把流量切到上一个 tag 的副本

## 3. 数据库异常
- 主从延迟：`SHOW SLAVE STATUS`
- 锁等待：`SELECT * FROM performance_schema.data_locks;`
- 误操作恢复：从最近备份 + binlog 时间点恢复（提前演练过）

## 4. 缓存击穿 / 雪崩
- 临时方案：服务降级、限流
- 长期：加二级缓存 / 互斥锁

## 5. 紧急回滚
```bash
# 后端
kubectl set image deployment/plm-admin plm-admin=plm-admin:<上一个 tag>
# 数据库（仅当本次发布有 DDL）
mysql -u... -p... plm < sql/rollback_<本次发布>.sql
```

## 6. 联系人
| 角色 | 主 | 备 |
|---|---|---|
| oncall | | |
| DBA | | |
| 网络 / 云厂商支持 | | |
