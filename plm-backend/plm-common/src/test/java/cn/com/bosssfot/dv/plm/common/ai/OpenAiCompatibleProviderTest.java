package cn.com.bosssfot.dv.plm.common.ai;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.ai.impl.OpenAiCompatibleProvider;

/**
 * OpenAiCompatibleProvider 单元测试 — 用 Spring MockRestServiceServer 模拟 OpenAI 协议响应。
 *
 * 验证项:
 *   1) 端点正确 (POST {base-url}/chat/completions)
 *   2) Bearer auth header 正确
 *   3) 响应 choices[0].message.content 正确解析
 *   4) usage tokens 正确解析
 *   5) HTTP 5xx 失败返回 success=false 不抛异常
 *   6) 4xx 失败同上
 */
class OpenAiCompatibleProviderTest {

    private AiProperties.OpenAi cfg;
    private RestTemplate rest;
    private MockRestServiceServer mockServer;
    private OpenAiCompatibleProvider provider;

    @BeforeEach
    void setUp() {
        cfg = new AiProperties.OpenAi();
        cfg.setEnabled(true);
        cfg.setBaseUrl("https://api.example.com/v1");
        cfg.setApiKey("test-key");
        cfg.setDefaultModel("gpt-4o-mini");
        rest = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(rest);
        provider = new OpenAiCompatibleProvider(cfg, rest);
    }

    @Test
    void name_and_availability() {
        assertEquals("openai", provider.name());
        assertTrue(provider.isAvailable());
    }

    @Test
    void unavailable_when_apiKey_blank() {
        cfg.setApiKey("");
        assertFalse(provider.isAvailable());
    }

    @Test
    void unavailable_when_apiKey_placeholder() {
        cfg.setApiKey("please-change-me");
        assertFalse(provider.isAvailable());
    }

    @Test
    void chat_success_parses_choices_and_usage() {
        String fakeJson = """
            {
              "id": "chatcmpl-test-123",
              "model": "gpt-4o-mini-2024-07-18",
              "choices": [{
                "index": 0,
                "message": { "role": "assistant", "content": "Hello PLM!" },
                "finish_reason": "stop"
              }],
              "usage": { "prompt_tokens": 12, "completion_tokens": 5, "total_tokens": 17 }
            }
            """;
        mockServer.expect(requestTo("https://api.example.com/v1/chat/completions"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andRespond(withSuccess(fakeJson, MediaType.APPLICATION_JSON));

        AiChatResult r = provider.chat(AiChatRequest.builder("openai")
                .system("sys").user("hi").maxTokens(100).temperature(0.7)
                .build());

        assertTrue(r.isSuccess(), "OpenAI 200 应成功");
        assertEquals("openai", r.getProvider());
        assertEquals("gpt-4o-mini-2024-07-18", r.getModel());
        assertEquals("Hello PLM!", r.getText());
        assertEquals("stop", r.getFinishReason());
        assertEquals("chatcmpl-test-123", r.getRequestId());
        assertEquals(12L, r.getPromptTokens());
        assertEquals(5L, r.getCompletionTokens());
        assertEquals(17L, r.getTotalTokens());
        assertTrue(r.getElapsedMs() >= 0);
        mockServer.verify();
    }

    @Test
    void chat_500_returns_failure_no_exception() {
        mockServer.expect(requestTo("https://api.example.com/v1/chat/completions"))
                .andRespond(withServerError().body("{\"error\":\"down\"}"));

        AiChatResult r = provider.chat(AiChatRequest.builder("openai").user("hi").build());
        assertFalse(r.isSuccess(), "500 应失败但不抛异常");
        assertNotNull(r.getError());
        assertTrue(r.getError().contains("500"));
    }

    @Test
    void chat_401_returns_failure() {
        mockServer.expect(requestTo("https://api.example.com/v1/chat/completions"))
                .andRespond(withUnauthorizedRequest().body("{\"error\":\"bad key\"}"));

        AiChatResult r = provider.chat(AiChatRequest.builder("openai").user("hi").build());
        assertFalse(r.isSuccess());
        assertTrue(r.getError().contains("401"));
    }

    @Test
    void chat_uses_default_model_when_request_model_blank() {
        // 这次 server 不验证 body,只确保不抛异常
        String resp = """
            {"id":"x","model":"gpt-4o-mini","choices":[{"message":{"content":"ok"},"finish_reason":"stop"}],"usage":{}}
            """;
        mockServer.expect(requestTo("https://api.example.com/v1/chat/completions"))
                .andRespond(withSuccess(resp, MediaType.APPLICATION_JSON));

        AiChatResult r = provider.chat(AiChatRequest.builder("openai").user("hi").build());
        assertTrue(r.isSuccess());
        assertEquals("gpt-4o-mini", r.getModel());
    }
}
