package cn.com.bosssfot.dv.plm.common.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 集成配置 — 绑定 application.yml {@code plm.ai.*}。
 *
 * <p>支持 4 个 provider 同时配置,运行时按 {@link cn.com.bosssfot.dv.plm.common.ai.dto.AiChatRequest#getProvider()}
 * 字段路由。任一 provider 未配置 api-key,该 provider 静默不装配(降级到下一个可用的)。</p>
 *
 * <pre>
 * plm:
 *   ai:
 *     default-provider: ${AI_DEFAULT_PROVIDER:mock}  # 业务 agent 未指定 provider 时的兜底
 *     openai:
 *       enabled: ${AI_OPENAI_ENABLED:false}
 *       base-url: ${AI_OPENAI_BASE_URL:https://api.openai.com/v1}
 *           # 一行换 base-url 即可切 DeepSeek (https://api.deepseek.com/v1)
 *           # 或通义 (https://dashscope.aliyuncs.com/compatible-mode/v1)
 *           # 或 Moonshot (https://api.moonshot.cn/v1)
 *       api-key: ${AI_OPENAI_API_KEY:}
 *       default-model: ${AI_OPENAI_DEFAULT_MODEL:gpt-4o-mini}
 *       connect-timeout-ms: 5000
 *       read-timeout-ms: 60000
 *     anthropic:
 *       enabled: ${AI_ANTHROPIC_ENABLED:false}
 *       base-url: ${AI_ANTHROPIC_BASE_URL:https://api.anthropic.com}
 *       api-key: ${AI_ANTHROPIC_API_KEY:}
 *       default-model: ${AI_ANTHROPIC_DEFAULT_MODEL:claude-sonnet-4-5}
 *       version: ${AI_ANTHROPIC_VERSION:2023-06-01}
 *       connect-timeout-ms: 5000
 *       read-timeout-ms: 60000
 *     # dify 仍走 plm.dify.* (现有 DifyService),DifyAiProvider 内部委托
 * </pre>
 *
 * @author plm
 */
@ConfigurationProperties(prefix = "plm.ai")
public class AiProperties {

    private String defaultProvider = "mock";

    private OpenAi openai = new OpenAi();
    private Anthropic anthropic = new Anthropic();

    public String getDefaultProvider() { return defaultProvider; }
    public void setDefaultProvider(String v) { this.defaultProvider = v; }
    public OpenAi getOpenai() { return openai; }
    public void setOpenai(OpenAi v) { this.openai = v; }
    public Anthropic getAnthropic() { return anthropic; }
    public void setAnthropic(Anthropic v) { this.anthropic = v; }

    /** OpenAI 兼容协议 — 覆盖 OpenAI / DeepSeek / 通义 / Moonshot / SiliconFlow 等 */
    public static class OpenAi {
        private boolean enabled = false;
        private String baseUrl = "https://api.openai.com/v1";
        private String apiKey;
        private String defaultModel = "gpt-4o-mini";
        private int connectTimeoutMs = 5000;
        private int readTimeoutMs = 60000;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean v) { this.enabled = v; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String v) { this.baseUrl = v; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String v) { this.apiKey = v; }
        public String getDefaultModel() { return defaultModel; }
        public void setDefaultModel(String v) { this.defaultModel = v; }
        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(int v) { this.connectTimeoutMs = v; }
        public int getReadTimeoutMs() { return readTimeoutMs; }
        public void setReadTimeoutMs(int v) { this.readTimeoutMs = v; }
        public boolean isUsable() {
            return enabled && apiKey != null && !apiKey.isBlank()
                && !"please-change-me".equalsIgnoreCase(apiKey)
                && baseUrl != null && !baseUrl.isBlank();
        }
    }

    /** Anthropic Claude Messages API */
    public static class Anthropic {
        private boolean enabled = false;
        private String baseUrl = "https://api.anthropic.com";
        private String apiKey;
        private String defaultModel = "claude-sonnet-4-5";
        private String version = "2023-06-01";
        private int connectTimeoutMs = 5000;
        private int readTimeoutMs = 60000;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean v) { this.enabled = v; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String v) { this.baseUrl = v; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String v) { this.apiKey = v; }
        public String getDefaultModel() { return defaultModel; }
        public void setDefaultModel(String v) { this.defaultModel = v; }
        public String getVersion() { return version; }
        public void setVersion(String v) { this.version = v; }
        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(int v) { this.connectTimeoutMs = v; }
        public int getReadTimeoutMs() { return readTimeoutMs; }
        public void setReadTimeoutMs(int v) { this.readTimeoutMs = v; }
        public boolean isUsable() {
            return enabled && apiKey != null && !apiKey.isBlank()
                && !"please-change-me".equalsIgnoreCase(apiKey)
                && baseUrl != null && !baseUrl.isBlank();
        }
    }
}
