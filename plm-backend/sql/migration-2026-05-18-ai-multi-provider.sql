-- =============================================================================
-- 增量迁移 — 2026-05-18 — AI 多 Provider 支持
-- 把 tb_ai_agent 从"只支持 Dify"升级为"支持 mock/dify/openai/anthropic 4 种"
-- 配合 plm-common.ai 包 (AiService 门面 + 4 个 Provider)
-- =============================================================================

-- 1. 表加 2 个字段
ALTER TABLE tb_ai_agent
    ADD COLUMN provider   VARCHAR(20)  NOT NULL DEFAULT 'mock' COMMENT '字典 biz_ai_provider: mock/dify/openai/anthropic'
        AFTER dify_workflow_id,
    ADD COLUMN model_name VARCHAR(120) NULL                    COMMENT '模型名 e.g. gpt-4o-mini / deepseek-chat / claude-sonnet-4-5'
        AFTER provider;

-- 2. 已有数据兜底:有 dify_workflow_id 的迁移为 dify provider,其他保持 mock
UPDATE tb_ai_agent
   SET provider = 'dify'
 WHERE provider = 'mock'
   AND dify_workflow_id IS NOT NULL
   AND dify_workflow_id <> '';

-- 3. 字典:新增 biz_ai_provider
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'AI Provider', 'biz_ai_provider', '0', 'admin', SYSDATE(), '4 种 provider'
  FROM dual
 WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'biz_ai_provider');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, 'Mock 占位',   'mock',      'biz_ai_provider', '', 'info',    'Y', '0', 'admin', SYSDATE(), '本地/降级'
  FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'biz_ai_provider' AND dict_value = 'mock')
UNION ALL
SELECT 2, 'Dify 编排',   'dify',      'biz_ai_provider', '', 'primary', 'N', '0', 'admin', SYSDATE(), 'workflow 编排'
  FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'biz_ai_provider' AND dict_value = 'dify')
UNION ALL
SELECT 3, 'OpenAI 兼容', 'openai',    'biz_ai_provider', '', 'success', 'N', '0', 'admin', SYSDATE(), 'OpenAI/DeepSeek/通义/Moonshot'
  FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'biz_ai_provider' AND dict_value = 'openai')
UNION ALL
SELECT 4, 'Anthropic',   'anthropic', 'biz_ai_provider', '', 'warning', 'N', '0', 'admin', SYSDATE(), 'Claude Messages API'
  FROM dual WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'biz_ai_provider' AND dict_value = 'anthropic');

-- 验证:
-- SELECT agent_no, agent_name, provider, model_name, dify_workflow_id FROM tb_ai_agent LIMIT 10;
-- SELECT dict_label, dict_value FROM sys_dict_data WHERE dict_type = 'biz_ai_provider';
