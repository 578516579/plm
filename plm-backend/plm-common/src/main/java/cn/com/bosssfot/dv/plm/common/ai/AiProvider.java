package cn.com.bosssfot.dv.plm.common.ai;

import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest;
import cn.com.bosssfot.dv.plm.common.ai.dto.AiChatResult;

/**
 * AI Provider SPI — 单个底层 AI 提供方的统一抽象。
 *
 * <p>实现类:</p>
 * <ul>
 *   <li>{@code MockAiProvider}        — 本地零依赖,永远 success=true</li>
 *   <li>{@code OpenAiCompatibleProvider} — OpenAI Chat Completions 协议,
 *       同时覆盖 DeepSeek / 通义千问 / Moonshot / SiliconFlow 等(只需换 base-url)</li>
 *   <li>{@code AnthropicProvider}     — Claude Messages API</li>
 *   <li>{@code DifyAiProvider}        — 委托 {@code DifyService} 走 workflow 编排</li>
 * </ul>
 *
 * <p>实现要求:</p>
 * <ul>
 *   <li>不抛受检异常 — HTTP/超时/解析异常吃掉,返回 {@code success=false} 的 Result</li>
 *   <li>{@link #name()} 必须返回 {@link AiChatRequest#getProvider()} 对应字符串</li>
 *   <li>{@link #isAvailable()} 反映配置是否完整(api-key 非空、enabled=true 等)</li>
 * </ul>
 *
 * @author plm
 */
public interface AiProvider {

    /** Provider 唯一标识 — "mock" / "openai" / "anthropic" / "dify" */
    String name();

    /** 配置是否完整可用(api-key 已注入、开关已开等);Mock 始终 true */
    boolean isAvailable();

    /** 执行一次推理 */
    AiChatResult chat(AiChatRequest request);
}
