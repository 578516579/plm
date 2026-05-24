package cn.com.bosssfot.dv.plm.common.ai;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.ai.impl.MockAiProvider;

/**
 * MockAiProvider 单元测试 — 验证降级实现的稳定性。
 */
class MockAiProviderTest {

    private final MockAiProvider provider = new MockAiProvider();

    @Test
    void name_returns_mock() {
        assertEquals("mock", provider.name());
    }

    @Test
    void always_available() {
        assertTrue(provider.isAvailable(), "Mock provider 必须永远 available");
    }

    @Test
    void chat_returns_success_with_echoed_content() {
        AiChatRequest req = AiChatRequest.builder("mock")
                .system("你是 PLM 助手")
                .user("生成测试")
                .callerTag("test#1")
                .build();
        AiChatResult r = provider.chat(req);
        assertTrue(r.isSuccess());
        assertEquals("mock", r.getProvider());
        assertNotNull(r.getText());
        assertTrue(r.getText().contains("[mock]"));
        assertTrue(r.getText().contains("生成测试"), "应该 echo 用户消息");
        assertNotNull(r.getRequestId());
        assertTrue(r.getRequestId().startsWith("mock-"));
        assertEquals("stop", r.getFinishReason());
        assertEquals(0L, r.getTotalTokens());
    }

    @Test
    void chat_handles_empty_request() {
        AiChatResult r = provider.chat(AiChatRequest.builder("mock").build());
        assertTrue(r.isSuccess(), "空请求 mock 也应该 success");
        assertEquals("mock-model", r.getModel(), "未指定 model 时返回 mock-model");
    }

    @Test
    void chat_respects_specified_model() {
        AiChatRequest req = AiChatRequest.builder("mock").model("gpt-4o-mini").build();
        AiChatResult r = provider.chat(req);
        assertEquals("gpt-4o-mini", r.getModel());
    }
}
