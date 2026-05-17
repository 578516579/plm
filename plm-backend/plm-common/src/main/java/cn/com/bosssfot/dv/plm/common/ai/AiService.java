package cn.com.bosssfot.dv.plm.common.ai;

import java.util.List;
import java.util.Map;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;

/**
 * AI 统一门面 — 业务模块(plm-ai-agent / plm-inception / plm-competitive / ...)
 * 通过此接口调 AI,不感知底层是 Dify 还是 OpenAI 还是 Anthropic。
 *
 * <p>路由规则:</p>
 * <ol>
 *   <li>请求中 {@code request.provider} 非空且对应 Provider 已装配 → 用该 Provider</li>
 *   <li>否则取 {@code plm.ai.default-provider} 兜底</li>
 *   <li>兜底也不可用 → 走 Mock(永不阻塞业务)</li>
 * </ol>
 *
 * <p>典型用法:</p>
 * <pre>
 * &#64;Autowired private AiService aiService;
 *
 * public Foo aiGenerate(Long id) {
 *     Foo f = mapper.selectById(id);
 *     AiChatResult r = aiService.chat(AiChatRequest.builder(f.getProvider())  // "openai"/"anthropic"/"dify"/"mock"
 *         .model(f.getModelName())
 *         .system("你是 PLM 资深架构师")
 *         .user("请生成需求文档:" + f.getTitle())
 *         .maxTokens(2000).temperature(0.5)
 *         .callerTag("plm-foo#" + id)
 *         .build());
 *     if (!r.isSuccess()) throw new ServiceException("AI 失败:" + r.getError(), 708);
 *     f.setAiGenerated(r.getText());
 *     mapper.update(f);
 *     return f;
 * }
 * </pre>
 *
 * @author plm
 */
public interface AiService {

    /** 执行一次推理(按 request.provider 路由) */
    AiChatResult chat(AiChatRequest request);

    /** 获取所有已装配的 provider 名 → 可用状态(用于 health 端点) */
    Map<String, Boolean> providerStatus();

    /** 当前默认 provider (来自 plm.ai.default-provider) */
    String defaultProvider();

    /** 获取注册的 provider 名列表 */
    default List<String> providers() { return List.copyOf(providerStatus().keySet()); }
}
