package cn.com.bosssfot.dv.plm.integration.adapter.zentao;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import cn.com.bosssfot.dv.plm.integration.adapter.ConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.mcp.crypto.AesGcmCipher;

/**
 * 禅道(ZenTao v18+) 连接器适配器。
 *
 * <p>支持:
 * <ul>
 *   <li>{@link #ping(IntegrationConnector)} - POST /api/v1/tokens 验账号 + 取 token</li>
 *   <li>{@link #verifyWebhookSignature} - X-Zentao-Token 明文比对 webhook_secret(常量时间)</li>
 *   <li>{@link #getToken(IntegrationConnector)} - Caffeine 风格内存缓存 25min,过期重签</li>
 *   <li>4 类资源 list/get/create/update:bug / story / task / case</li>
 * </ul>
 *
 * <p>凭据 JSON 结构(存 {@code tb_integration_connector.credential_enc} 加密):
 * <pre>
 * {
 *   "account":  "plm-bot",
 *   "password": "xxxxx"
 * }
 * </pre>
 *
 * <p>{@link IntegrationConnector#getEndpoint()} = 禅道 base URL(如 {@code https://zentao.example.com},不带末尾斜杠),
 *    {@link IntegrationConnector#getWebhookSecret()} = X-Zentao-Token 期望值。
 *
 * <p>v15- 老 sessionID 流派**本期不兼容**,真实联调发现版本差异需退化时在 Proposal 0014 §5 风险栏跟踪。
 *
 * @see <a href="https://www.zentao.net/book/api">ZenTao REST API 文档</a>
 */
@Component
public class ZentaoConnectorAdapter implements ConnectorAdapter {

    private static final Logger log = LoggerFactory.getLogger(ZentaoConnectorAdapter.class);

    /** Token 缓存 TTL 25 分钟(禅道默认 30 分钟,留 5 分钟续签缓冲) */
    private static final long TOKEN_TTL_MS = 25 * 60 * 1000L;

    @Autowired
    private AesGcmCipher cipher;

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    /** connectorId → (token, expireEpochMs) */
    private final Map<Long, TokenEntry> tokenCache = new ConcurrentHashMap<>();

    @Override
    public String type() {
        return "zentao";
    }

    @Override
    public String ping(IntegrationConnector connector) throws Exception {
        // 清掉旧缓存,强制重签
        tokenCache.remove(connector.getId());
        String token = getToken(connector);
        return "OK, zentao token=" + maskToken(token);
    }

    @Override
    public boolean verifyWebhookSignature(IntegrationConnector connector, String signature, String timestamp, byte[] rawBody) {
        if (connector.getWebhookSecret() == null || connector.getWebhookSecret().isEmpty()) {
            log.warn("[plm-integration/zentao] connector_id={} 未配 webhook_secret,验签直接拒绝", connector.getId());
            return false;
        }
        if (signature == null) return false;
        // 禅道 webhook 自定义 header(X-Zentao-Token)明文比对
        return constantTimeEquals(signature, connector.getWebhookSecret());
    }

    /** 取 token,带 25min 内存缓存;401 时清缓存重签 */
    public String getToken(IntegrationConnector connector) throws Exception {
        TokenEntry entry = tokenCache.get(connector.getId());
        long now = System.currentTimeMillis();
        if (entry != null && entry.expireAt > now) {
            return entry.token;
        }
        Credential cred = decryptCredential(connector);
        String url = ensureEndpoint(connector) + "/api/v1/tokens";
        JSONObject body = new JSONObject();
        body.put("account", cred.account);
        body.put("password", cred.password);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(10))
            .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
            .build();
        HttpResponse<String> resp;
        try {
            resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (java.net.ConnectException | java.net.http.HttpConnectTimeoutException ce) {
            throw new RuntimeException("[errorCode=814] 禅道 endpoint 不可达: " + ce.getMessage());
        }
        if (resp.statusCode() == 401 || resp.statusCode() == 403) {
            throw new RuntimeException("[errorCode=813] 禅道 token 失败,account/password 错: HTTP " + resp.statusCode());
        }
        if (resp.statusCode() != 200 && resp.statusCode() != 201) {
            throw new RuntimeException("禅道 /tokens HTTP " + resp.statusCode() + ": " + resp.body());
        }
        JSONObject json = JSON.parseObject(resp.body());
        String token = json.getString("token");
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("禅道 /tokens 响应缺 token 字段: " + resp.body());
        }
        tokenCache.put(connector.getId(), new TokenEntry(token, now + TOKEN_TTL_MS));
        return token;
    }

    // ───────── Bug ──────────────────────────────────────────────────────

    public List<JSONObject> listBugs(IntegrationConnector connector, String productId, String status) throws Exception {
        StringBuilder url = new StringBuilder(ensureEndpoint(connector))
            .append("/api/v1/products/").append(productId).append("/bugs");
        if (status != null && !status.isEmpty()) url.append("?status=").append(status);
        JSONObject resp = request(connector, "GET", url.toString(), null);
        JSONArray arr = resp.getJSONArray("bugs");
        return arr == null ? List.of() : arr.toJavaList(JSONObject.class);
    }

    public JSONObject getBug(IntegrationConnector connector, String bugId) throws Exception {
        return request(connector, "GET", ensureEndpoint(connector) + "/api/v1/bugs/" + bugId, null);
    }

    public JSONObject createBug(IntegrationConnector connector, String productId, JSONObject body) throws Exception {
        return request(connector, "POST",
            ensureEndpoint(connector) + "/api/v1/products/" + productId + "/bugs",
            body);
    }

    public JSONObject updateBug(IntegrationConnector connector, String bugId, JSONObject body) throws Exception {
        return request(connector, "PUT", ensureEndpoint(connector) + "/api/v1/bugs/" + bugId, body);
    }

    // ───────── Story ────────────────────────────────────────────────────

    public List<JSONObject> listStories(IntegrationConnector connector, String productId, String status) throws Exception {
        StringBuilder url = new StringBuilder(ensureEndpoint(connector))
            .append("/api/v1/products/").append(productId).append("/stories");
        if (status != null && !status.isEmpty()) url.append("?status=").append(status);
        JSONObject resp = request(connector, "GET", url.toString(), null);
        JSONArray arr = resp.getJSONArray("stories");
        return arr == null ? List.of() : arr.toJavaList(JSONObject.class);
    }

    public JSONObject getStory(IntegrationConnector connector, String storyId) throws Exception {
        return request(connector, "GET", ensureEndpoint(connector) + "/api/v1/stories/" + storyId, null);
    }

    public JSONObject createStory(IntegrationConnector connector, String productId, JSONObject body) throws Exception {
        return request(connector, "POST",
            ensureEndpoint(connector) + "/api/v1/products/" + productId + "/stories",
            body);
    }

    public JSONObject updateStory(IntegrationConnector connector, String storyId, JSONObject body) throws Exception {
        return request(connector, "PUT", ensureEndpoint(connector) + "/api/v1/stories/" + storyId, body);
    }

    // ───────── Task ─────────────────────────────────────────────────────

    public List<JSONObject> listTasks(IntegrationConnector connector, String executionId, String status) throws Exception {
        StringBuilder url = new StringBuilder(ensureEndpoint(connector))
            .append("/api/v1/executions/").append(executionId).append("/tasks");
        if (status != null && !status.isEmpty()) url.append("?status=").append(status);
        JSONObject resp = request(connector, "GET", url.toString(), null);
        JSONArray arr = resp.getJSONArray("tasks");
        return arr == null ? List.of() : arr.toJavaList(JSONObject.class);
    }

    public JSONObject getTask(IntegrationConnector connector, String taskId) throws Exception {
        return request(connector, "GET", ensureEndpoint(connector) + "/api/v1/tasks/" + taskId, null);
    }

    public JSONObject createTask(IntegrationConnector connector, String executionId, JSONObject body) throws Exception {
        return request(connector, "POST",
            ensureEndpoint(connector) + "/api/v1/executions/" + executionId + "/tasks",
            body);
    }

    public JSONObject updateTask(IntegrationConnector connector, String taskId, JSONObject body) throws Exception {
        return request(connector, "PUT", ensureEndpoint(connector) + "/api/v1/tasks/" + taskId, body);
    }

    // ───────── Case ─────────────────────────────────────────────────────

    public List<JSONObject> listCases(IntegrationConnector connector, String productId, String status) throws Exception {
        StringBuilder url = new StringBuilder(ensureEndpoint(connector))
            .append("/api/v1/products/").append(productId).append("/cases");
        if (status != null && !status.isEmpty()) url.append("?status=").append(status);
        JSONObject resp = request(connector, "GET", url.toString(), null);
        JSONArray arr = resp.getJSONArray("cases");
        return arr == null ? List.of() : arr.toJavaList(JSONObject.class);
    }

    public JSONObject getCase(IntegrationConnector connector, String caseId) throws Exception {
        return request(connector, "GET", ensureEndpoint(connector) + "/api/v1/cases/" + caseId, null);
    }

    public JSONObject createCase(IntegrationConnector connector, String productId, JSONObject body) throws Exception {
        return request(connector, "POST",
            ensureEndpoint(connector) + "/api/v1/products/" + productId + "/cases",
            body);
    }

    public JSONObject updateCase(IntegrationConnector connector, String caseId, JSONObject body) throws Exception {
        return request(connector, "PUT", ensureEndpoint(connector) + "/api/v1/cases/" + caseId, body);
    }

    // ────────────────────────────────────────────────────────────────────

    /**
     * 通用 HTTP 调用:带 Token 头,401 自动清缓存重签后重试一次。
     * @param body GET/DELETE 时传 null
     */
    JSONObject request(IntegrationConnector connector, String method, String url, JSONObject body) throws Exception {
        return requestInternal(connector, method, url, body, false);
    }

    private JSONObject requestInternal(IntegrationConnector connector, String method, String url, JSONObject body, boolean retried) throws Exception {
        String token = getToken(connector);
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
            .header("Token", token)
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(15));
        switch (method) {
            case "GET":    b.GET(); break;
            case "DELETE": b.DELETE(); break;
            case "POST":   b.POST(HttpRequest.BodyPublishers.ofString(body == null ? "{}" : body.toJSONString())); break;
            case "PUT":    b.PUT(HttpRequest.BodyPublishers.ofString(body == null ? "{}" : body.toJSONString())); break;
            default: throw new IllegalArgumentException("不支持的 HTTP method: " + method);
        }
        HttpResponse<String> resp;
        try {
            resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        } catch (java.net.ConnectException | java.net.http.HttpConnectTimeoutException ce) {
            throw new RuntimeException("[errorCode=814] 禅道 endpoint 不可达: " + ce.getMessage());
        }
        if (resp.statusCode() == 401 && !retried) {
            tokenCache.remove(connector.getId());
            return requestInternal(connector, method, url, body, true);
        }
        if (resp.statusCode() == 404) {
            throw new RuntimeException("禅道资源不存在: " + url);
        }
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new RuntimeException("禅道 " + method + " " + url + " HTTP " + resp.statusCode() + ": " + truncate(resp.body(), 500));
        }
        String respBody = resp.body();
        if (respBody == null || respBody.isEmpty()) return new JSONObject();
        // 兼容禅道返回 JSON 对象 / JSON 数组 / 字符串 message
        Object parsed = JSON.parse(respBody);
        if (parsed instanceof JSONObject jo) return jo;
        if (parsed instanceof JSONArray ja) {
            JSONObject wrap = new JSONObject();
            wrap.put("data", ja);
            return wrap;
        }
        JSONObject wrap = new JSONObject();
        wrap.put("raw", respBody);
        return wrap;
    }

    private Credential decryptCredential(IntegrationConnector connector) {
        if (connector.getCredentialEnc() == null || connector.getCredentialEnc().isEmpty()) {
            throw new RuntimeException("[errorCode=805] connector.credential_enc 为空,未配置禅道凭据");
        }
        String json = cipher.decrypt(connector.getCredentialEnc());
        return JSON.parseObject(json, Credential.class);
    }

    private static String ensureEndpoint(IntegrationConnector connector) {
        String ep = connector.getEndpoint();
        if (ep == null || ep.isEmpty()) {
            throw new RuntimeException("[errorCode=805] connector.endpoint 为空");
        }
        return ep.endsWith("/") ? ep.substring(0, ep.length() - 1) : ep;
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private static String maskToken(String token) {
        if (token == null || token.length() < 8) return "***";
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    /** 禅道凭据反序列化 DTO */
    public static class Credential {
        public String account;
        public String password;
    }

    /** Token 缓存条目 */
    private static class TokenEntry {
        final String token;
        final long expireAt;
        TokenEntry(String token, long expireAt) { this.token = token; this.expireAt = expireAt; }
    }

    /** 提供给 Service 层调用以构建禅道侧 URL(展示用) */
    public String buildExternalUrl(IntegrationConnector connector, String objectType, String objectId) {
        return ensureEndpoint(connector) + "/zentao/" + objectType + "-view-" + objectId + ".html";
    }

    /** 提供给 Test 注入 endpoint(测试用,生产路径不调) */
    Map<Long, TokenEntry> _testTokenCache() {
        return tokenCache;
    }

    /** 提供给 Test 注入 HttpClient 替身(本期暂不暴露,留接口) */
    @SuppressWarnings("unused")
    private static Map<String, Object> _internalState() {
        return new HashMap<>();
    }
}
