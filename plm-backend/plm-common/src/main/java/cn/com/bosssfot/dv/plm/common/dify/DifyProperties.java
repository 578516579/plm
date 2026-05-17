package cn.com.bosssfot.dv.plm.common.dify;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Dify 集成配置 — 绑定 application.yml `plm.dify.*`
 *
 * <pre>
 * plm:
 *   dify:
 *     enabled: false            # 总开关:false → 走 mock(默认,保护本地/CI E2E)
 *     base-url: https://api.dify.ai/v1
 *     api-key: app-xxxx         # Dify "Application API Key" (Bearer)
 *     connect-timeout-ms: 5000
 *     read-timeout-ms: 60000    # workflow 推理通常 10~60s
 *     default-user: plm-system  # response_mode=blocking 需要 user 字段
 *     workflows:                # agent_type → workflow_id 路由表 (可选,优先取 tb_ai_agent.dify_workflow_id)
 *       requirement: wf-xxxx
 *       prd: wf-yyyy
 *       code: wf-zzzz
 *       test: wf-aaaa
 *       release: wf-bbbb
 *       ops: wf-cccc
 * </pre>
 *
 * 安全:api-key 走环境变量 ${DIFY_API_KEY},不入库不入 git。
 * 降级:enabled=false 或 api-key 为空 → 全部回退 mock,业务调用方无感知。
 *
 * @author plm
 */
@ConfigurationProperties(prefix = "plm.dify")
public class DifyProperties {
    /** 总开关 — 默认 false,保护本地启动零依赖与 E2E 稳定 */
    private boolean enabled = false;

    /** Dify Service API 基础 URL,如 https://api.dify.ai/v1 或私有部署地址 */
    private String baseUrl = "https://api.dify.ai/v1";

    /** Dify 应用 API Key (Bearer),** 必须 ** 通过环境变量注入 */
    private String apiKey;

    /** TCP 建连超时(毫秒) */
    private int connectTimeoutMs = 5000;

    /** 响应读取超时(毫秒) — workflow 推理一般 10~60s */
    private int readTimeoutMs = 60000;

    /** Dify Service API blocking 模式需要的 user 标识 */
    private String defaultUser = "plm-system";

    /** agent_type → workflow_id 兜底路由 (tb_ai_agent.dify_workflow_id 优先) */
    private Map<String, String> workflows = new LinkedHashMap<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { this.enabled = v; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String v) { this.baseUrl = v; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String v) { this.apiKey = v; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int v) { this.connectTimeoutMs = v; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int v) { this.readTimeoutMs = v; }
    public String getDefaultUser() { return defaultUser; }
    public void setDefaultUser(String v) { this.defaultUser = v; }
    public Map<String, String> getWorkflows() { return workflows; }
    public void setWorkflows(Map<String, String> v) { this.workflows = v; }

    /** 运行时判定:配置完整且开关打开 — 决定 DifyService 走 HTTP 还是 Mock */
    public boolean isUsable() {
        return enabled
            && apiKey != null && !apiKey.isBlank()
            && !"please-change-me".equalsIgnoreCase(apiKey)
            && baseUrl != null && !baseUrl.isBlank();
    }
}
