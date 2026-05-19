package cn.com.bosssfot.dv.plm.common.ai;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatChunk;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.ai.impl.AiServiceImpl;
import cn.com.bosssfot.dv.plm.common.ai.impl.MockAiProvider;

/**
 * V4 Phase 1 流式 SPI 单元测试
 *
 * <p>覆盖:</p>
 * <ul>
 *   <li>MockAiProvider.chatStream 分块 emit + done</li>
 *   <li>AiProvider default chatStream(包成单 chunk)</li>
 *   <li>AiService.chatStream() 路由 + 审计 recorder 触发</li>
 *   <li>fallback:provider 不可用降级到 mock</li>
 * </ul>
 */
class AiChatStreamTest {

    private MockAiProvider mockProvider;
    private AiServiceImpl service;
    private AiProperties props;

    @BeforeEach
    void setUp() {
        props = new AiProperties();
        props.setDefaultProvider("mock");
        mockProvider = new MockAiProvider();
        service = new AiServiceImpl(List.of(mockProvider), props);
    }

    @Test
    void mock_chatStream_emits_multiple_chunks_with_done() {
        AiChatRequest req = AiChatRequest.builder("mock")
                .system("Hello PLM")
                .user("Please generate stuff")
                .build();

        Iterator<AiChatChunk> it = mockProvider.chatStream(req);
        List<AiChatChunk> chunks = new ArrayList<>();
        while (it.hasNext()) chunks.add(it.next());

        // 至少 2 个 chunk (因为 mock text 含多个 token)
        assertTrue(chunks.size() >= 2, "应该 emit 多个 chunk,实际:" + chunks.size());

        // 最后一个 chunk 必须是 done
        AiChatChunk last = chunks.get(chunks.size() - 1);
        assertTrue(last.isDone(), "最后一个 chunk 必须 done=true");
        assertEquals("mock", last.getProvider());
        assertEquals("stop", last.getFinishReason());
        assertNotNull(last.getRequestId());
        assertTrue(last.getRequestId().startsWith("mock-"));

        // 中间 chunk 应该 done=false
        for (int i = 0; i < chunks.size() - 1; i++) {
            assertFalse(chunks.get(i).isDone(), "chunk[" + i + "] 不应该 done");
            assertNotNull(chunks.get(i).getDeltaText());
            assertNotNull(chunks.get(i).getAccumulatedText());
        }

        // 累积文本严格递增
        for (int i = 1; i < chunks.size(); i++) {
            String prev = chunks.get(i - 1).getAccumulatedText();
            String curr = chunks.get(i).getAccumulatedText();
            assertTrue(curr.length() >= prev.length(),
                    "accumulated 应递增 [" + i + "]: prev='" + prev + "' curr='" + curr + "'");
        }
    }

    @Test
    void aiService_chatStream_routes_to_mock() {
        AiChatRequest req = AiChatRequest.builder("mock").user("hi").build();
        Iterator<AiChatChunk> it = service.chatStream(req);

        AiChatChunk lastDone = null;
        int count = 0;
        while (it.hasNext()) {
            AiChatChunk c = it.next();
            count++;
            if (c.isDone()) lastDone = c;
        }

        assertTrue(count >= 1, "至少 1 chunk");
        assertNotNull(lastDone, "必有 done chunk");
        assertEquals("mock", lastDone.getProvider());
    }

    @Test
    void aiService_chatStream_recorder_invoked_on_done() {
        AtomicReference<AiChatResult> captured = new AtomicReference<>();
        service.setRecorder((req, res) -> captured.set(res));

        Iterator<AiChatChunk> it = service.chatStream(
                AiChatRequest.builder("mock").user("hi").callerTag("test#stream").build());
        while (it.hasNext()) it.next();   // 消费所有 chunk(包括 done 触发审计)

        assertNotNull(captured.get(), "done chunk 应触发 recorder");
        assertTrue(captured.get().isSuccess());
        assertEquals("mock", captured.get().getProvider());
    }

    @Test
    void default_chatStream_fallback_to_chat_as_single_chunk() {
        // 假 provider 不 override chatStream,走 default 实现
        AiProvider plainProvider = new AiProvider() {
            @Override public String name() { return "fake"; }
            @Override public boolean isAvailable() { return true; }
            @Override public AiChatResult chat(AiChatRequest req) {
                return AiChatResult.ok("fake", "fake-model", "fake text");
            }
        };

        Iterator<AiChatChunk> it = plainProvider.chatStream(
                AiChatRequest.builder("fake").user("hi").build());

        assertTrue(it.hasNext());
        AiChatChunk only = it.next();
        assertTrue(only.isDone(), "default 实现应该 emit 单个 done chunk");
        assertEquals("fake", only.getProvider());
        assertEquals("fake text", only.getDeltaText());
        assertFalse(it.hasNext(), "不应该有第二个 chunk");
    }

    @Test
    void aiService_chatStream_returns_error_chunk_when_no_provider() {
        // 极端:default 不可用 + 没装配 mock
        AiProperties p = new AiProperties();
        p.setDefaultProvider("openai");
        AiProvider unavailable = new AiProvider() {
            @Override public String name() { return "openai"; }
            @Override public boolean isAvailable() { return false; }
            @Override public AiChatResult chat(AiChatRequest req) { return null; }
        };
        AiServiceImpl svc = new AiServiceImpl(List.of(unavailable), p);

        Iterator<AiChatChunk> it = svc.chatStream(
                AiChatRequest.builder("openai").user("hi").build());

        assertTrue(it.hasNext());
        AiChatChunk only = it.next();
        assertTrue(only.isDone());
        assertNotNull(only.getError());
        assertTrue(only.getError().contains("无可用"));
    }
}
