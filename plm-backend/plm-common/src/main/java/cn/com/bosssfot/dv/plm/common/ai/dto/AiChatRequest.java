package cn.com.bosssfot.dv.plm.common.ai.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 调用请求 — 跨 provider 通用。
 *
 * <p>设计原则:让业务层不感知具体 LLM 协议差异,只填业务字段;
 * 各 {@link cn.com.bosssfot.dv.plm.common.ai.AiProvider} 实现负责协议适配。</p>
 *
 * <pre>
 * 构造示例:
 *   AiChatRequest req = AiChatRequest.builder("openai")
 *       .model("deepseek-chat")
 *       .system("你是 PLM 项目立项专家")
 *       .user("帮我写一份农业 IoT 平台的立项建议书")
 *       .temperature(0.7)
 *       .maxTokens(2000)
 *       .build();
 * </pre>
 *
 * @author plm
 */
public class AiChatRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 选择哪个 provider: mock / openai / anthropic / dify */
    private String provider;
    /** 模型名 — provider 内部识别(provider=dify 时忽略,走 workflow) */
    private String model;
    /** 系统指令(顶层,等价 messages 中首条 system) */
    private String system;
    /** 多轮对话消息 */
    private List<AiChatMessage> messages = new ArrayList<>();
    /** 采样温度,null=用 provider 默认 */
    private Double temperature;
    /** 最大输出 token */
    private Integer maxTokens;
    /** dify provider 专用:workflow inputs(其他 provider 忽略) */
    private Map<String, Object> difyInputs = new LinkedHashMap<>();
    /** 标识调用方(便于审计/限流),如 "ai-agent#AGT-2026-0001" */
    private String callerTag;

    public static Builder builder(String provider) { return new Builder(provider); }

    public String getProvider() { return provider; }
    public void setProvider(String v) { this.provider = v; }
    public String getModel() { return model; }
    public void setModel(String v) { this.model = v; }
    public String getSystem() { return system; }
    public void setSystem(String v) { this.system = v; }
    public List<AiChatMessage> getMessages() { return messages; }
    public void setMessages(List<AiChatMessage> v) { this.messages = v == null ? new ArrayList<>() : v; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double v) { this.temperature = v; }
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer v) { this.maxTokens = v; }
    public Map<String, Object> getDifyInputs() { return difyInputs; }
    public void setDifyInputs(Map<String, Object> v) { this.difyInputs = v == null ? new LinkedHashMap<>() : v; }
    public String getCallerTag() { return callerTag; }
    public void setCallerTag(String v) { this.callerTag = v; }

    /** 取首条 user 消息(便于 Dify 取 query) */
    public String firstUserContent() {
        for (AiChatMessage m : messages) {
            if ("user".equalsIgnoreCase(m.getRole())) return m.getContent();
        }
        return "";
    }

    public static class Builder {
        private final AiChatRequest r = new AiChatRequest();
        Builder(String provider) { r.provider = provider; }
        public Builder model(String m) { r.model = m; return this; }
        public Builder system(String s) { r.system = s; return this; }
        public Builder user(String content) { r.messages.add(AiChatMessage.user(content)); return this; }
        public Builder assistant(String content) { r.messages.add(AiChatMessage.assistant(content)); return this; }
        public Builder message(AiChatMessage m) { r.messages.add(m); return this; }
        public Builder messages(List<AiChatMessage> ms) { r.messages.addAll(ms); return this; }
        public Builder temperature(Double t) { r.temperature = t; return this; }
        public Builder maxTokens(Integer n) { r.maxTokens = n; return this; }
        public Builder difyInput(String k, Object v) { r.difyInputs.put(k, v); return this; }
        public Builder difyInputs(Map<String, Object> m) { r.difyInputs.putAll(m == null ? Collections.emptyMap() : m); return this; }
        public Builder callerTag(String t) { r.callerTag = t; return this; }
        public AiChatRequest build() { return r; }
    }
}
