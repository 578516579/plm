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
import cn.com.bosssfot.dv.plm.common.ai.impl.AnthropicProvider;

/**
 * AnthropicProvider 单元测试 — 验证 Claude Messages API 协议特有点:
 *   - x-api-key header (非 Bearer)
 *   - anthropic-version header
 *   - system 顶层字段
 *   - content[0].text 解析
 *   - input_tokens/output_tokens (非 prompt/completion)
 */
class AnthropicProviderTest {

    private AiProperties.Anthropic cfg;
    private RestTemplate rest;
    private MockRestServiceServer mockServer;
    private AnthropicProvider provider;

    @BeforeEach
    void setUp() {
        cfg = new AiProperties.Anthropic();
        cfg.setEnabled(true);
        cfg.setBaseUrl("https://api.anthropic.example.com");
        cfg.setApiKey("sk-ant-test");
        cfg.setDefaultModel("claude-sonnet-4-5");
        cfg.setVersion("2023-06-01");
        rest = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(rest);
        provider = new AnthropicProvider(cfg, rest);
    }

    @Test
    void name_is_anthropic() {
        assertEquals("anthropic", provider.name());
    }

    @Test
    void chat_success_uses_correct_headers_and_parses_content_array() {
        String fakeJson = """
            {
              "id": "msg_test_abc",
              "model": "claude-sonnet-4-5-20250619",
              "content": [
                { "type": "text", "text": "Hello from Claude!" }
              ],
              "stop_reason": "end_turn",
              "usage": { "input_tokens": 20, "output_tokens": 10 }
            }
            """;
        mockServer.expect(requestTo("https://api.anthropic.example.com/v1/messages"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("x-api-key", "sk-ant-test"))
                .andExpect(header("anthropic-version", "2023-06-01"))
                .andRespond(withSuccess(fakeJson, MediaType.APPLICATION_JSON));

        AiChatResult r = provider.chat(AiChatRequest.builder("anthropic")
                .system("you are claude").user("hi").maxTokens(100).build());

        assertTrue(r.isSuccess());
        assertEquals("anthropic", r.getProvider());
        assertEquals("claude-sonnet-4-5-20250619", r.getModel());
        assertEquals("Hello from Claude!", r.getText());
        assertEquals("end_turn", r.getFinishReason());
        assertEquals("msg_test_abc", r.getRequestId());
        assertEquals(20L, r.getPromptTokens(), "input_tokens 映射到 promptTokens");
        assertEquals(10L, r.getCompletionTokens(), "output_tokens 映射到 completionTokens");
        assertEquals(30L, r.getTotalTokens(), "totalTokens = input + output");
        mockServer.verify();
    }

    @Test
    void chat_concatenates_multiple_text_blocks() {
        String fakeJson = """
            {
              "id": "msg_x",
              "model": "claude-sonnet-4-5",
              "content": [
                { "type": "text", "text": "Part 1. " },
                { "type": "text", "text": "Part 2." }
              ],
              "stop_reason": "end_turn",
              "usage": { "input_tokens": 5, "output_tokens": 3 }
            }
            """;
        mockServer.expect(requestTo("https://api.anthropic.example.com/v1/messages"))
                .andRespond(withSuccess(fakeJson, MediaType.APPLICATION_JSON));

        AiChatResult r = provider.chat(AiChatRequest.builder("anthropic").user("hi").build());
        assertEquals("Part 1. Part 2.", r.getText(), "多个 text block 应拼接");
    }

    @Test
    void chat_429_returns_failure() {
        mockServer.expect(requestTo("https://api.anthropic.example.com/v1/messages"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS)
                        .body("{\"error\":\"rate limited\"}"));

        AiChatResult r = provider.chat(AiChatRequest.builder("anthropic").user("hi").build());
        assertFalse(r.isSuccess());
        assertTrue(r.getError().contains("429"));
    }
}
