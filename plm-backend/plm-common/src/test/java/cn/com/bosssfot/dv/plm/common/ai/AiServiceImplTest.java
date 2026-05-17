package cn.com.bosssfot.dv.plm.common.ai;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;
import cn.com.bosssfot.dv.plm.common.ai.impl.AiServiceImpl;
import cn.com.bosssfot.dv.plm.common.ai.impl.MockAiProvider;

/**
 * AiServiceImpl 路由逻辑单元测试。
 *
 * <p>验证:</p>
 * <ul>
 *   <li>精确路由 — request.provider 命中已装配 provider 时直接选用</li>
 *   <li>默认兜底 — request.provider 为空时走 default-provider</li>
 *   <li>Mock 兜底 — 全部不可用时回 mock(因 Mock 始终 available)</li>
 *   <li>审计 recorder — 装配后每次 chat 都触发一次 record</li>
 *   <li>审计异常吃掉 — recorder 抛错不影响主链路</li>
 * </ul>
 */
class AiServiceImplTest {

    private AiProperties props;
    private MockAiProvider mockProvider;
    private FakeOpenAiProvider fakeOpenAi;
    private AiServiceImpl service;

    @BeforeEach
    void setUp() {
        props = new AiProperties();
        props.setDefaultProvider("mock");
        mockProvider = new MockAiProvider();
        fakeOpenAi = new FakeOpenAiProvider(true);  // available
        service = new AiServiceImpl(List.of(mockProvider, fakeOpenAi), props);
    }

    @Test
    void route_by_explicit_provider() {
        AiChatResult r = service.chat(AiChatRequest.builder("openai").user("hi").build());
        assertTrue(r.isSuccess());
        assertEquals("openai", r.getProvider(), "应路由到 openai");
    }

    @Test
    void route_falls_back_to_default_when_provider_blank() {
        AiChatResult r = service.chat(AiChatRequest.builder("").user("hi").build());
        assertEquals("mock", r.getProvider(), "空 provider → 走 default=mock");
    }

    @Test
    void route_falls_back_to_mock_when_requested_provider_unavailable() {
        // 把 fake provider 改为 unavailable
        FakeOpenAiProvider unavailable = new FakeOpenAiProvider(false);
        AiServiceImpl svc = new AiServiceImpl(List.of(mockProvider, unavailable), props);
        AiChatResult r = svc.chat(AiChatRequest.builder("openai").user("hi").build());
        assertEquals("mock", r.getProvider(), "openai unavailable → 降级 mock");
    }

    @Test
    void route_returns_fail_when_no_provider_available() {
        // 极端:Mock 也没装配 (业务侧手工组装),且 default 不可用
        AiProperties p = new AiProperties();
        p.setDefaultProvider("openai");
        FakeOpenAiProvider unavailable = new FakeOpenAiProvider(false);
        AiServiceImpl svc = new AiServiceImpl(List.of(unavailable), p);
        AiChatResult r = svc.chat(AiChatRequest.builder("openai").user("hi").build());
        assertFalse(r.isSuccess());
        assertNotNull(r.getError());
        assertTrue(r.getError().contains("无可用"));
    }

    @Test
    void recorder_invoked_on_chat() {
        AtomicReference<AiChatRequest> capturedReq = new AtomicReference<>();
        AtomicReference<AiChatResult> capturedRes = new AtomicReference<>();
        service.setRecorder((req, res) -> {
            capturedReq.set(req);
            capturedRes.set(res);
        });
        service.chat(AiChatRequest.builder("mock").user("hi").callerTag("test#42").build());
        assertNotNull(capturedReq.get(), "recorder 必须被调用");
        assertEquals("test#42", capturedReq.get().getCallerTag());
        assertTrue(capturedRes.get().isSuccess());
    }

    @Test
    void recorder_exception_does_not_break_chat() {
        service.setRecorder((req, res) -> { throw new RuntimeException("boom"); });
        // 主链路不应抛异常
        AiChatResult r = service.chat(AiChatRequest.builder("mock").user("hi").build());
        assertTrue(r.isSuccess(), "recorder 抛错不能影响主链路");
    }

    @Test
    void provider_status_contains_all_registered() {
        assertEquals(2, service.providerStatus().size());
        assertTrue(service.providerStatus().get("mock"));
        assertTrue(service.providerStatus().get("openai"));
    }

    @Test
    void default_provider_exposed() {
        assertEquals("mock", service.defaultProvider());
    }

    // === Fake provider 用于测试 ===
    static class FakeOpenAiProvider implements AiProvider {
        private final boolean available;
        FakeOpenAiProvider(boolean available) { this.available = available; }
        @Override public String name() { return "openai"; }
        @Override public boolean isAvailable() { return available; }
        @Override public AiChatResult chat(AiChatRequest req) {
            return AiChatResult.ok("openai", "fake-model", "fake response");
        }
    }
}
