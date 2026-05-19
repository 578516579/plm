package cn.com.bosssfot.dv.plm.common.ai.dto;

import java.io.Serializable;

/**
 * AI 调用结果 — 跨 provider 统一结构。
 *
 * <p>业务层只看 {@link #isSuccess()} + {@link #getText()};失败时看 {@link #getError()}。</p>
 *
 * @author plm
 */
public class AiChatResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    /** 模型生成的文本(所有 provider 都把主要输出归一到这个字段) */
    private String text;
    /** 实际响应的 model 名(可能与请求不同,如 deepseek-chat 实际返回 deepseek-chat-v2.5) */
    private String model;
    /** 实际 provider */
    private String provider;
    /** 完成原因 stop / length / tool_use / content_filter */
    private String finishReason;
    /** 输入 token */
    private long promptTokens;
    /** 输出 token */
    private long completionTokens;
    /** 总 token */
    private long totalTokens;
    /** 端到端耗时(毫秒) */
    private long elapsedMs;
    /** 流式首 token 延迟(毫秒);非流式时为 0 — V4 Phase 4 加 */
    private long firstTokenMs;
    /** 是否走流式 — V4 Phase 4 加;false=阻塞 chat,true=streaming */
    private boolean streaming;
    /** provider 回传的请求 id,用于审计 */
    private String requestId;
    /** 失败原因,success=true 时为 null */
    private String error;

    public static AiChatResult ok(String provider, String model, String text) {
        AiChatResult r = new AiChatResult();
        r.success = true;
        r.provider = provider;
        r.model = model;
        r.text = text == null ? "" : text;
        return r;
    }

    public static AiChatResult fail(String provider, String error) {
        AiChatResult r = new AiChatResult();
        r.success = false;
        r.provider = provider;
        r.error = error;
        r.text = "";
        return r;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean v) { this.success = v; }
    public String getText() { return text; }
    public void setText(String v) { this.text = v; }
    public String getModel() { return model; }
    public void setModel(String v) { this.model = v; }
    public String getProvider() { return provider; }
    public void setProvider(String v) { this.provider = v; }
    public String getFinishReason() { return finishReason; }
    public void setFinishReason(String v) { this.finishReason = v; }
    public long getPromptTokens() { return promptTokens; }
    public void setPromptTokens(long v) { this.promptTokens = v; }
    public long getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(long v) { this.completionTokens = v; }
    public long getTotalTokens() { return totalTokens; }
    public void setTotalTokens(long v) { this.totalTokens = v; }
    public long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(long v) { this.elapsedMs = v; }
    public long getFirstTokenMs() { return firstTokenMs; }
    public void setFirstTokenMs(long v) { this.firstTokenMs = v; }
    public boolean isStreaming() { return streaming; }
    public void setStreaming(boolean v) { this.streaming = v; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String v) { this.requestId = v; }
    public String getError() { return error; }
    public void setError(String v) { this.error = v; }
}
