---
description: 跑 PLM 字符编码守门员 (encoding.spec.ts, 6 case)。乱码事故回归用，~20s。
---

# /e2e-encoding — 字符编码守门员

只跑 [encoding.spec.ts](plm-frontend/e2e/encoding.spec.ts)（6 case，~20s）。**这是 P0 阻塞守门员**，任何"DB HEX 含 EFBFBD" 必须立即停下。

## 何时单独跑

- 改了 [`plm-admin/src/main/resources/application.yml`](plm-backend/plm-admin/src/main/resources/application.yml) 的 `server.servlet.encoding` 配置
- 改了 JDBC URL（[application-druid.yml](plm-backend/plm-admin/src/main/resources/application-druid.yml)）的 `characterEncoding` 参数
- 改了 mybatis SQL 映射
- 后端启动命令的 `-Dfile.encoding=UTF-8` 等 4 个标志疑似被改
- 发现某模块 list 接口返回的某字段显示乱码

## 跑

```bash
cd plm-frontend
npm run test:e2e:encoding
```

期望最后一行：`6 passed (~20s)`。

## 失败 → P0 流程

立刻按 [03-开发/字符编码规范.md](03-开发/字符编码规范.md) §排查清单：

1. `ps -ef | grep "java.*plm-admin"` —— 看后端启动有没有带 `-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8`
2. `SELECT @@character_set_database, @@collation_database FROM dual;` —— 必须 utf8mb4 + utf8mb4_0900_ai_ci
3. `mysql --default-character-set=utf8mb4 plm -e "SELECT HEX(project_name) FROM tb_project LIMIT 1"` —— 如果 HEX 已有 EFBFBD，问题已经在 DB 里，需要清理 + 修复链路再重灌
4. JDBC URL 必须含 `characterEncoding=utf8&useUnicode=true`

**不允许** "暂时跳过 encoding 套件" — 见 [.claude/rules.md §G.4](.claude/rules.md) 一票否决项。
