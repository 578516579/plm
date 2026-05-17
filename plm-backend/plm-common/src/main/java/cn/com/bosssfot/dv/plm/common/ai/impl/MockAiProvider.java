package cn.com.bosssfot.dv.plm.common.ai.impl;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.bosssfot.dv.plm.common.ai.AiProvider;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;

/**
 * Mock Provider — 默认始终装配,本地零依赖。
 *
 * <p>用途:</p>
 * <ul>
 *   <li>开发机不需要任何 LLM 即可启动 PLM</li>
 *   <li>E2E / CI 跑全套件时 AI 路径稳定 success=true</li>
 *   <li>生产降级 — 真 provider 全挂掉时 AiService 退到 Mock</li>
 * </ul>
 *
 * <p>输出固定:回显 system + 末条 user 消息,前缀 "[mock]"。</p>
 *
 * @author plm
 */
public class MockAiProvider implements AiProvider {
    private static final Logger log = LoggerFactory.getLogger(MockAiProvider.class);

    @Override
    public String name() { return "mock"; }

    @Override
    public boolean isAvailable() { return true; }

    @Override
    public AiChatResult chat(AiChatRequest req) {
        long start = System.currentTimeMillis();
        String userText = req.firstUserContent();
        StringBuilder sb = new StringBuilder("[mock] ");
        if (req.getSystem() != null && !req.getSystem().isBlank()) {
            sb.append("system=\"").append(shorten(req.getSystem(), 80)).append("\" ");
        }
        sb.append("user=\"").append(shorten(userText, 200)).append("\"");
        log.debug("[Ai-mock] callerTag={} model={}", req.getCallerTag(), req.getModel());

        AiChatResult r = AiChatResult.ok("mock",
                req.getModel() == null || req.getModel().isBlank() ? "mock-model" : req.getModel(),
                sb.toString());
        r.setFinishReason("stop");
        r.setPromptTokens(0);
        r.setCompletionTokens(0);
        r.setTotalTokens(0);
        r.setElapsedMs(System.currentTimeMillis() - start);
        r.setRequestId("mock-" + UUID.randomUUID());
        return r;
    }

    private static String shorten(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
