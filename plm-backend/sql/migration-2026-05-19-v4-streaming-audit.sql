-- =============================================================================
-- 增量迁移 — 2026-05-19 — V4 Phase 4: 审计表加 streaming + first_token_ms 字段
-- 配合 plm-common.ai V4 Phase 1+3 streaming 能力
-- =============================================================================

-- 加 2 个字段 (都可空,V3 历史数据不受影响)
ALTER TABLE tb_ai_invocation_log
    ADD COLUMN streaming      TINYINT(1) DEFAULT 0    COMMENT '0 阻塞 / 1 流式 (V4 Phase 3+)'
        AFTER success,
    ADD COLUMN first_token_ms BIGINT(20) DEFAULT NULL COMMENT '流式首 token 延迟(ms) - V4 UX 核心指标'
        AFTER elapsed_ms;

-- 验证:
-- SHOW COLUMNS FROM tb_ai_invocation_log WHERE Field IN ('streaming','first_token_ms');
-- SELECT streaming, COUNT(*), AVG(first_token_ms) FROM tb_ai_invocation_log GROUP BY streaming;
