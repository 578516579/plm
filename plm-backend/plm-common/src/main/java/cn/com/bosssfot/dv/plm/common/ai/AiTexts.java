package cn.com.bosssfot.dv.plm.common.ai;

import java.util.function.Supplier;

import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;

/**
 * AI 文本生成 + 兜底 — A 档业务模块统一入口(P0-1)。
 *
 * <p>{@link #generate} 内部调用 {@link AiService#chat},因此单测里对 {@code chat} 的
 * stub / verify 仍然生效。语义:</p>
 * <ul>
 *   <li>真 provider 成功且文本非空 → 采用其输出(剥去 {@code ```markdown} 围栏)</li>
 *   <li>否则(mock / 失败 / 空文本)→ 返回 {@code fallback}</li>
 * </ul>
 * <p>业务连续性优先,永不抛、永不阻塞:默认 {@code plm.ai.default-provider=mock} 时走 fallback 模板,
 * 配置真厂商 + key 后自动改用 LLM 输出。</p>
 *
 * <pre>
 * entity.setContent(AiTexts.generate(aiService, req, () -> buildTemplate(entity)));
 * </pre>
 *
 * @author plm
 */
public final class AiTexts {

    private AiTexts() {
    }

    /**
     * @param aiService 注入的 AI 门面(内部会调一次 {@link AiService#chat})
     * @param request   推理请求
     * @param fallback  mock/失败/空时的兜底文本供给(通常是原场景化模板)
     * @return 真 AI 文本 或 fallback
     */
    public static String generate(AiService aiService, AiChatRequest request, Supplier<String> fallback) {
        AiChatResult r = aiService.chat(request);
        boolean fromRealAi = r != null && r.isSuccess()
                && !"mock".equalsIgnoreCase(r.getProvider())
                && r.getText() != null && !r.getText().isBlank();
        if (!fromRealAi) {
            return fallback.get();
        }
        String s = r.getText().trim();
        if (s.startsWith("```")) {
            int nl = s.indexOf('\n');
            if (nl > 0) {
                s = s.substring(nl + 1);
            }
            if (s.endsWith("```")) {
                s = s.substring(0, s.length() - 3);
            }
            s = s.trim();
        }
        return s;
    }
}
