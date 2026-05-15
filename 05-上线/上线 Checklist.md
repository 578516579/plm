# 上线 Checklist

每次发布前逐项打勾，不漏关键步骤。

## 代码 & 构建
- [ ] PR 全部合入 main，CI 全绿
- [ ] 版本号已更新（pom.xml `<plm.version>`、frontend `package.json` `version`）
- [ ] Changelog 已更新（[Changelog.md](Changelog.md)）
- [ ] tag 已打 (`git tag -a vX.Y.Z -m "..."`)

## 数据库
- [ ] 新增 DDL 已在 staging 跑过、回滚脚本备好
- [ ] 数据迁移在低峰期执行
- [ ] 备份完成（`mysqldump` 或快照）

## 配置 & 凭据
- [ ] 生产环境 `JWT_SECRET / DB_PASSWORD / REDIS_PASSWORD / DRUID_PASSWORD` 已更新
- [ ] 默认弱口令（admin/admin123 等）已禁用或修改
- [ ] Druid 监控台对外端口已限制

## 监控 & 告警
- [ ] 日志接入（看板链接见 [06-运营/数据看板（链接）](../06-运营/数据看板（链接）/)）
- [ ] 关键接口监控告警阈值已设
- [ ] oncall 排班表已更新

## 沟通
- [ ] 发布窗口已通知相关方
- [ ] 客服 / 用户公告已准备
- [ ] 回滚预案 owner 待命（见 [Runbook.md](Runbook.md)）

## 上线后
- [ ] 灰度验证关键路径
- [ ] 全量后 30 分钟内观察 P0 指标
- [ ] 写发布后小结，更新 [Changelog.md](Changelog.md)
