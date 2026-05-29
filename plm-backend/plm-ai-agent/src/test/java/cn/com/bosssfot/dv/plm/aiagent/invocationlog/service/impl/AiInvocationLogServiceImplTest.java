package cn.com.bosssfot.dv.plm.aiagent.invocationlog.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cn.com.bosssfot.dv.plm.aiagent.invocationlog.domain.AiInvocationLog;
import cn.com.bosssfot.dv.plm.aiagent.invocationlog.mapper.AiInvocationLogMapper;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;

/**
 * {@link AiInvocationLogServiceImpl} 单元测试 — AI 审计审计层覆盖盲点补强。
 *
 * <p>覆盖盲点(jacoco 0% → 100%):本类没有任何单测,被 jacoco 60% 门槛硬拦。
 * 由本会话 task #4 jacoco 覆盖率扫描发现(2026-05-29)。
 *
 * <p>关键场景:
 * <ul>
 *   <li>record 成功路径(完整字段映射)</li>
 *   <li>record 失败路径(mapper 抛异常被吞,审计绝不阻塞主链路)</li>
 *   <li>record 字段兜底:callerTag/provider 缺失走默认</li>
 *   <li>record streaming/firstTokenMs > 0 路径</li>
 *   <li>4 个委托方法(list / byId / delete / providerSummary)</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class AiInvocationLogServiceImplTest {

    @Mock
    private AiInvocationLogMapper mapper;

    @InjectMocks
    private AiInvocationLogServiceImpl service;

    // ─── record (核心审计写入) ──────────────────────────────────────────

    @Nested
    @DisplayName("record — AI 调用审计写入")
    class RecordTests {

        @Test
        @DisplayName("成功路径:所有字段被透传到 AiInvocationLog 行")
        void recordSuccess() {
            AiChatRequest req = new AiChatRequest();
            req.setCallerTag("requirement.ai.evaluate");
            req.setProvider("openai");
            req.setModel("gpt-4");

            AiChatResult res = new AiChatResult();
            res.setSuccess(true);
            res.setProvider("openai");
            res.setModel("gpt-4");
            res.setFinishReason("stop");
            res.setPromptTokens(100L);
            res.setCompletionTokens(200L);
            res.setTotalTokens(300L);
            res.setElapsedMs(1500L);
            res.setRequestId("req-abc");
            res.setStreaming(false);

            service.record(req, res);

            ArgumentCaptor<AiInvocationLog> captor = ArgumentCaptor.forClass(AiInvocationLog.class);
            verify(mapper, times(1)).insertAiInvocationLog(captor.capture());
            AiInvocationLog saved = captor.getValue();

            assertThat(saved.getCallerTag()).isEqualTo("requirement.ai.evaluate");
            assertThat(saved.getProvider()).isEqualTo("openai");
            assertThat(saved.getModel()).isEqualTo("gpt-4");
            assertThat(saved.getSuccess()).isEqualTo(1);
            assertThat(saved.getStreaming()).isEqualTo(0);
            assertThat(saved.getFinishReason()).isEqualTo("stop");
            assertThat(saved.getPromptTokens()).isEqualTo(100L);
            assertThat(saved.getCompletionTokens()).isEqualTo(200L);
            assertThat(saved.getTotalTokens()).isEqualTo(300L);
            assertThat(saved.getElapsedMs()).isEqualTo(1500L);
            assertThat(saved.getRequestId()).isEqualTo("req-abc");
            assertThat(saved.getInvokedAt()).isNotNull();
        }

        @Test
        @DisplayName("失败路径:result.success=false + 异常 message 入 errorMsg")
        void recordFailureWithError() {
            AiChatRequest req = new AiChatRequest();
            req.setCallerTag("prd.ai.generate");
            req.setProvider("anthropic");

            AiChatResult res = new AiChatResult();
            res.setSuccess(false);
            res.setProvider("anthropic");
            res.setError("API key invalid");

            service.record(req, res);

            ArgumentCaptor<AiInvocationLog> captor = ArgumentCaptor.forClass(AiInvocationLog.class);
            verify(mapper).insertAiInvocationLog(captor.capture());
            AiInvocationLog saved = captor.getValue();
            assertThat(saved.getSuccess()).isEqualTo(0);
            assertThat(saved.getErrorMsg()).isEqualTo("API key invalid");
        }

        @Test
        @DisplayName("兜底:callerTag/provider 缺失 → '(unknown)' / 'mock'")
        void recordWithDefaults() {
            AiChatRequest req = new AiChatRequest();
            // 不设 callerTag 和 provider

            AiChatResult res = new AiChatResult();
            res.setSuccess(true);
            // result.provider 也不设

            service.record(req, res);

            ArgumentCaptor<AiInvocationLog> captor = ArgumentCaptor.forClass(AiInvocationLog.class);
            verify(mapper).insertAiInvocationLog(captor.capture());
            AiInvocationLog saved = captor.getValue();
            assertThat(saved.getCallerTag()).isEqualTo("(unknown)");
            assertThat(saved.getProvider()).isEqualTo("mock");
        }

        @Test
        @DisplayName("streaming 路径:firstTokenMs > 0 → 写入 firstTokenMs 字段")
        void recordStreamingPath() {
            AiChatRequest req = new AiChatRequest();
            req.setCallerTag("agent.stream");
            req.setProvider("openai");

            AiChatResult res = new AiChatResult();
            res.setSuccess(true);
            res.setProvider("openai");
            res.setStreaming(true);
            res.setFirstTokenMs(150L);

            service.record(req, res);

            ArgumentCaptor<AiInvocationLog> captor = ArgumentCaptor.forClass(AiInvocationLog.class);
            verify(mapper).insertAiInvocationLog(captor.capture());
            AiInvocationLog saved = captor.getValue();
            assertThat(saved.getStreaming()).isEqualTo(1);
            assertThat(saved.getFirstTokenMs()).isEqualTo(150L);
        }

        @Test
        @DisplayName("mapper 抛异常被吞:审计绝不阻塞主链路")
        void recordSwallowsException() {
            AiChatRequest req = new AiChatRequest();
            req.setCallerTag("test.swallow");
            req.setProvider("test");
            AiChatResult res = new AiChatResult();
            res.setSuccess(true);
            res.setProvider("test");

            when(mapper.insertAiInvocationLog(any()))
                .thenThrow(new RuntimeException("DB down"));

            // 不应抛出
            service.record(req, res);

            verify(mapper).insertAiInvocationLog(any());
        }

        @Test
        @DisplayName("safeShort:errorMsg 超 500 字符被截断 + '...' 后缀")
        void recordTruncatesLongError() {
            AiChatRequest req = new AiChatRequest();
            req.setCallerTag("test.long.error");
            req.setProvider("test");

            AiChatResult res = new AiChatResult();
            res.setSuccess(false);
            res.setProvider("test");
            res.setError("X".repeat(600)); // 600 字符,超 500 上限

            service.record(req, res);

            ArgumentCaptor<AiInvocationLog> captor = ArgumentCaptor.forClass(AiInvocationLog.class);
            verify(mapper).insertAiInvocationLog(captor.capture());
            String saved = captor.getValue().getErrorMsg();
            assertThat(saved).hasSize(500);
            assertThat(saved).endsWith("...");
        }
    }

    // ─── 4 个委托方法 ──────────────────────────────────────────────────

    @Nested
    @DisplayName("简单委托")
    class DelegationTests {

        @Test
        @DisplayName("selectAiInvocationLogList 委托 mapper")
        void listDelegates() {
            AiInvocationLog query = new AiInvocationLog();
            query.setProvider("openai");
            AiInvocationLog row = new AiInvocationLog();
            row.setLogId(1L);
            when(mapper.selectAiInvocationLogList(query)).thenReturn(List.of(row));

            List<AiInvocationLog> result = service.selectAiInvocationLogList(query);

            assertThat(result).hasSize(1);
            verify(mapper).selectAiInvocationLogList(query);
        }

        @Test
        @DisplayName("selectAiInvocationLogById 委托 mapper")
        void byIdDelegates() {
            AiInvocationLog expected = new AiInvocationLog();
            expected.setLogId(99L);
            when(mapper.selectAiInvocationLogById(99L)).thenReturn(expected);

            AiInvocationLog result = service.selectAiInvocationLogById(99L);

            assertThat(result).isSameAs(expected);
            verify(mapper).selectAiInvocationLogById(99L);
        }

        @Test
        @DisplayName("deleteAiInvocationLogByIds 委托 mapper")
        void deleteDelegates() {
            Long[] ids = { 1L, 2L, 3L };
            when(mapper.deleteAiInvocationLogByIds(ids)).thenReturn(3);

            int rows = service.deleteAiInvocationLogByIds(ids);

            assertThat(rows).isEqualTo(3);
            verify(mapper).deleteAiInvocationLogByIds(ids);
        }

        @Test
        @DisplayName("getProviderSummary 委托 mapper.selectProviderSummary")
        void summaryDelegates() {
            List<Map<String, Object>> summary = List.of(
                Map.of("provider", "openai", "cnt", 100L),
                Map.of("provider", "anthropic", "cnt", 50L)
            );
            when(mapper.selectProviderSummary()).thenReturn(summary);

            List<Map<String, Object>> result = service.getProviderSummary();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).get("provider")).isEqualTo("openai");
            verify(mapper).selectProviderSummary();
        }
    }
}
