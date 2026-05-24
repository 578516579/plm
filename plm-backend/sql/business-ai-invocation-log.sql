-- =============================================================================
-- AI Invocation Log — V2 多 Provider 集成审计表
-- 每次 AiService.chat() 调用都写一条,可按 caller_tag / provider / model 聚合
-- =============================================================================
DROP TABLE IF EXISTS tb_ai_invocation_log;
CREATE TABLE tb_ai_invocation_log (
    log_id              BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键',
    caller_tag          VARCHAR(120)  NOT NULL                 COMMENT '调用方标识 e.g. ai-agent#AGT-2026-0001 / inception#42',
    provider            VARCHAR(20)   NOT NULL                 COMMENT 'mock/dify/openai/anthropic',
    model               VARCHAR(120)                           COMMENT '模型名(实际响应)',
    success             TINYINT(1)    NOT NULL DEFAULT 0       COMMENT '0 失败 / 1 成功',
    streaming           TINYINT(1)    DEFAULT 0                COMMENT '0 阻塞 / 1 流式 (V4 Phase 3+)',
    finish_reason       VARCHAR(40)                            COMMENT 'stop / length / tool_use / content_filter',
    prompt_tokens       BIGINT(20)    DEFAULT 0                COMMENT '输入 token',
    completion_tokens   BIGINT(20)    DEFAULT 0                COMMENT '输出 token',
    total_tokens        BIGINT(20)    DEFAULT 0                COMMENT '总 token',
    elapsed_ms          BIGINT(20)    DEFAULT 0                COMMENT '端到端耗时(毫秒)',
    first_token_ms      BIGINT(20)    DEFAULT NULL             COMMENT '流式首 token 延迟(ms) - V4 UX 核心指标',
    request_id          VARCHAR(120)                           COMMENT 'provider 回传的请求 id',
    error_msg           VARCHAR(500)                           COMMENT '失败原因 (success=0 时)',
    invoked_at          DATETIME      NOT NULL                 COMMENT '调用时间',
    PRIMARY KEY (log_id),
    KEY idx_log_caller    (caller_tag),
    KEY idx_log_provider  (provider),
    KEY idx_log_success   (success),
    KEY idx_log_invoked_at (invoked_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 调用审计日志';
