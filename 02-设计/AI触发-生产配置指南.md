# AI 真厂商接入 — 生产配置指南

> 状态: V3 落地完成 (2026-05-18)
> 关联: [V1 Dify 集成](Dify集成-系统设计.md) · [V2 多 Provider](AI多Provider-系统设计-V2.md)
> 适用对象:运维/部署工程师 · 系统管理员
> 安全声明:**所有 api-key 仅通过环境变量注入,严禁 commit 进 git**

---

## 1. 快速决策树:选哪个 Provider

```
你需要的 AI 能力是什么?
│
├─ 单轮 prompt → text(总结/生成/审查/翻译)
│   ├─ 中国大陆部署、低成本           → openai 协议 + DeepSeek/通义/Moonshot
│   ├─ 数据合规要求高、能用海外服务   → anthropic (Claude)
│   ├─ 已有 OpenAI 账号               → openai 协议 + 官方 base-url
│   └─ 私有部署 / 内网受限              → 自家 OpenAI 兼容网关 (vLLM/LM Studio 等)
│
├─ 多节点编排(RAG/工具调用/分支)
│   └─ Dify (workflow 模式)
│
└─ 本地开发 / CI / 故障降级
    └─ mock(默认,零依赖)
```

---

## 2. 配置 OpenAI 兼容协议(覆盖国产/海外主流)

### 2.1 DeepSeek(推荐:性价比最高,中国大陆首选)

```bash
# 注册 https://platform.deepseek.com 获取 API key
export AI_DEFAULT_PROVIDER=openai
export AI_OPENAI_ENABLED=true
export AI_OPENAI_BASE_URL=https://api.deepseek.com/v1
export AI_OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxx
export AI_OPENAI_DEFAULT_MODEL=deepseek-chat       # 通用对话
# 或 export AI_OPENAI_DEFAULT_MODEL=deepseek-reasoner   # 推理强化
```

### 2.2 通义千问(阿里云,数据合规友好)

```bash
# https://dashscope.aliyun.com 开通后取 key
export AI_OPENAI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
export AI_OPENAI_API_KEY=sk-xxx
export AI_OPENAI_DEFAULT_MODEL=qwen-max            # 或 qwen-turbo (便宜) / qwen-plus
```

### 2.3 Moonshot(月之暗面 Kimi,长上下文 200K)

```bash
export AI_OPENAI_BASE_URL=https://api.moonshot.cn/v1
export AI_OPENAI_API_KEY=sk-xxx
export AI_OPENAI_DEFAULT_MODEL=moonshot-v1-32k     # 或 moonshot-v1-128k / moonshot-v1-8k
```

### 2.4 智谱 GLM(清华系)

```bash
export AI_OPENAI_BASE_URL=https://open.bigmodel.cn/api/paas/v4
export AI_OPENAI_API_KEY=xxx.xxxxx
export AI_OPENAI_DEFAULT_MODEL=glm-4-plus          # 或 glm-4 / glm-4-flash
```

### 2.5 SiliconFlow(各厂商模型聚合)

```bash
export AI_OPENAI_BASE_URL=https://api.siliconflow.cn/v1
export AI_OPENAI_API_KEY=sk-xxx
export AI_OPENAI_DEFAULT_MODEL=deepseek-ai/DeepSeek-V2.5
```

### 2.6 OpenAI 官方

```bash
export AI_OPENAI_BASE_URL=https://api.openai.com/v1
export AI_OPENAI_API_KEY=sk-proj-xxx
export AI_OPENAI_DEFAULT_MODEL=gpt-4o-mini         # 推荐 mini 性价比 / 或 gpt-4o
```

### 2.7 私有部署 (vLLM / LM Studio / Ollama OpenAI-compat)

```bash
export AI_OPENAI_BASE_URL=http://10.0.0.50:8000/v1   # 自家网关
export AI_OPENAI_API_KEY=any-string-anyway-not-checked
export AI_OPENAI_DEFAULT_MODEL=Qwen/Qwen2.5-14B-Instruct  # 自家加载的模型
```

---

## 3. 配置 Anthropic Claude

```bash
export AI_DEFAULT_PROVIDER=anthropic
export AI_ANTHROPIC_ENABLED=true
export AI_ANTHROPIC_API_KEY=sk-ant-api03-xxxxxxxxxxxxxxxxxxxxxxxxx
export AI_ANTHROPIC_DEFAULT_MODEL=claude-sonnet-4-5     # 推荐
# 或 claude-opus-4 (最强,贵) / claude-haiku-4 (快,便宜)
export AI_ANTHROPIC_BASE_URL=https://api.anthropic.com  # 默认值,无需改
export AI_ANTHROPIC_VERSION=2023-06-01                  # 默认值
```

**国内访问 Anthropic 需走代理**,可通过自家网关:
```bash
export AI_ANTHROPIC_BASE_URL=https://my-claude-proxy.example.com
```

---

## 4. 配置 Dify(workflow 编排)

```bash
export DIFY_ENABLED=true
export DIFY_BASE_URL=https://api.dify.ai/v1     # 或私有部署地址
export DIFY_API_KEY=app-xxxxxxxxxxxxxxxxxxxxx
# 6 类 Agent 兜底 workflow 路由(tb_ai_agent.dify_workflow_id 优先)
export DIFY_WF_REQUIREMENT=wf-xxxxxxxx
export DIFY_WF_PRD=wf-xxxxxxxx
# ... 其余 4 个
```

详见 [V1 Dify 集成文档](Dify集成-系统设计.md)。

---

## 5. 同时启用多个 Provider

```bash
# OpenAI 兼容 + Anthropic + Dify 三者全开,Mock 永远可用
export AI_OPENAI_ENABLED=true
export AI_OPENAI_API_KEY=...
export AI_ANTHROPIC_ENABLED=true
export AI_ANTHROPIC_API_KEY=...
export DIFY_ENABLED=true
export DIFY_API_KEY=...

# 默认走哪个
export AI_DEFAULT_PROVIDER=openai   # 或 anthropic / dify / mock

# 在 tb_ai_agent 表里,某些 agent 可个性化:
# UPDATE tb_ai_agent SET provider='anthropic', model_name='claude-sonnet-4-5'
#   WHERE agent_no='AGT-2026-0042';  -- 专门用 Claude 跑这个 agent
```

---

## 6. 验证步骤

### 6.1 启动后看日志

```
[Dify] HTTP 实现已装配 — baseUrl=..., readTimeout=60000ms     ← Dify 真调上了
[AiService] init — providers=[mock, openai, anthropic, dify], default=openai
[AiService] 审计 recorder 已接入: AiInvocationLogServiceImpl
```

### 6.2 检查 health 端点

```bash
curl -H "Authorization: Bearer <jwt>" \
  http://your-host:8081/business/ai-agent/ai/health
```

预期响应:
```json
{
  "code": 200,
  "defaultProvider": "openai",
  "providers": {
    "mock": true,
    "openai": true,
    "anthropic": true,
    "dify": true
  },
  "openaiEnabled": true,
  "openaiBaseUrl": "https://api.deepseek.com/v1",
  "openaiModel": "deepseek-chat",
  ...
}
```

### 6.3 触发一次真调

```bash
# 创建 agent provider=openai
curl -X POST http://your-host:8081/business/ai-agent \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{"agentName":"Test","agentType":"prd","provider":"openai","modelName":"deepseek-chat","authorUserId":1}'

# 调用 invoke
curl -X POST http://your-host:8081/business/ai-agent/invoke/<id> \
  -H "Authorization: Bearer <jwt>"
```

后端日志应看到:
```
[AiService] route → openai (want=openai)
[Ai-openai] chatcmpl-xxx ok,model=deepseek-chat,tokens=421,elapsed=3245ms,caller=ai-agent#AGT-...
```

### 6.4 查审计表

```sql
-- 看最近 10 条审计
SELECT log_id, caller_tag, provider, model, success, total_tokens, elapsed_ms, invoked_at
FROM tb_ai_invocation_log
ORDER BY log_id DESC LIMIT 10;

-- Provider 维度汇总(对应 /business/ai-invocation-log/summary)
SELECT provider, COUNT(*) cnt, SUM(success) ok,
       SUM(total_tokens) tokens, ROUND(AVG(elapsed_ms)) avg_ms,
       ROUND(SUM(success)*100/COUNT(*), 2) success_rate
FROM tb_ai_invocation_log GROUP BY provider;
```

---

## 7. 安全清单

| 项 | 要求 |
|---|---|
| api-key 来源 | **只**通过环境变量/K8s Secret/CI Secret 注入。 `.env.example` 占位空串 |
| `.env` 文件 | 已被 `.gitignore` 忽略;`.env.example` 是文档不是真值 |
| `/ai/health` | **不返回** api-key,只暴露 enabled/baseUrl/defaultModel |
| 日志输出 | api-key 不写日志(已审查 OpenAiCompatibleProvider/AnthropicProvider) |
| 用户消息 | 注意 prompt 中不要拼接业务敏感数据(身份证/银行卡等),如需要可在调用方做脱敏 |
| 跨境合规 | 中国数据出境严格场景:用 DeepSeek/通义/Moonshot/GLM 等境内服务,不用 OpenAI/Anthropic |
| 限流配额 | V4 规划中(P2),目前依赖各厂商自身限流;请在 console 配预算告警 |

---

## 8. 成本估算(2026-05 价格)

| Provider | 模型 | 输入(¥/1M tokens) | 输出(¥/1M tokens) | 备注 |
|---|---|---|---|---|
| DeepSeek | deepseek-chat | ~¥2 | ~¥8 | 中文场景性价比第一 |
| DeepSeek | deepseek-reasoner | ~¥4 | ~¥16 | 推理增强 |
| 通义 | qwen-turbo | ~¥0.3 | ~¥0.6 | 便宜但能力一般 |
| 通义 | qwen-max | ~¥20 | ~¥60 | 旗舰 |
| Moonshot | moonshot-v1-8k | ~¥12 | ~¥12 | 长上下文友好 |
| OpenAI | gpt-4o-mini | ~$0.15 | ~$0.60 | 海外便宜首选 |
| OpenAI | gpt-4o | ~$2.5 | ~$10 | |
| Anthropic | claude-sonnet-4-5 | ~$3 | ~$15 | |
| Anthropic | claude-haiku-4 | ~$1 | ~$5 | |

> 实际价格请以各厂商官网为准,本表仅参考。
> PLM 单次调用一般 < 2K tokens,预估单价 < ¥0.01

---

## 9. 故障排查

### 现象:health 显示 openai=false
检查:
1. `echo $AI_OPENAI_ENABLED` 是否 `true`?
2. `echo $AI_OPENAI_API_KEY` 是否非空且不为占位 `please-change-me`?
3. 后端启动日志看 `[Ai-openai]` 装配信息

### 现象:调用 invoke 返回 708 "AI 调用失败"
后端日志看具体错误:
- `HTTP 401` → api-key 错或过期
- `HTTP 429` → 触发了厂商限流,等待或换更高配额
- `HTTP 500` / `Connection refused` → 厂商服务故障,看厂商 status 页
- `Connection timeout` → 网络/防火墙问题,调大 `AI_OPENAI_READ_TIMEOUT_MS`

### 现象:审计表没记录
- 看后端日志是否有 `[AiService] 审计 recorder 已接入: AiInvocationLogServiceImpl`
- 没有 → plm-ai-agent 没起来,或 `@Service` 未扫到
- 有但表空 → 看 `[ai-audit] record failed` 警告

### 现象:E2E 失败
不应该发生(默认 mock 模式下输出仍是硬编码 mock)。如果 default=真厂商,可能 LLM 输出不含 E2E 断言关键字。临时切回 mock 排查:
```bash
unset AI_DEFAULT_PROVIDER
unset AI_OPENAI_ENABLED
# 重启后端
```

---

## 10. V3 完成清单

- [x] 4 Provider 自动装配 + 路由(mock/openai/anthropic/dify)
- [x] OpenAI 兼容协议覆盖 6+ 厂商(改 base-url 即可)
- [x] Anthropic 协议特殊性正确处理(x-api-key/system 顶层/content 数组)
- [x] Dify workflow 模式委托给 V1 DifyService
- [x] tb_ai_agent 加 provider + model_name 字段 + 4 项字典
- [x] 前端 view 加 provider/model 表单 + AI 集成总览徽章
- [x] tb_ai_invocation_log 审计表 + AiInvocationRecorder SPI
- [x] 13 个业务模块 aiGenerate/aiAnalyze 改造为通过 AiService 调用
- [x] /ai/health 端点不暴露 api-key
- [x] /business/ai-invocation-log/list /summary 端点
- [x] 单元测试 24 通过(MockAi/AiService/OpenAi/Anthropic)
- [x] E2E 全套件 120/120 零退步

## 11. V4 路线

- streaming 模式 (SSE 推 token,前端实时显示)
- 限流配额 (Bucket4j 或 Redis sliding window,按 provider/caller 维度)
- Tool use / function calling 支持
- 多 provider 内部负载均衡 + 自动故障转移
- AWS Bedrock / Azure OpenAI / GCP Vertex 适配器
- MCP 集成 — Dify 调 Claude Code
- 业务侧切换为读 result.getText() 替换 mock 输出(逐模块灰度)
