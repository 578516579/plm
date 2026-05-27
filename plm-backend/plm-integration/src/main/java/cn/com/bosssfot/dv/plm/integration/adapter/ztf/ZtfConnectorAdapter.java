package cn.com.bosssfot.dv.plm.integration.adapter.ztf;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import cn.com.bosssfot.dv.plm.integration.adapter.ConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.mcp.crypto.AesGcmCipher;

/**
 * ZTF(ZenTao Test Framework / 自动化测试执行框架)连接器适配器。
 *
 * <p><b>单向 Inbound</b>:ZTF 跑完测试 → run 结果 webhook 回传 → 更新 tb_autotest 结果字段。
 *    PLM 不回写 ZTF,因此本适配器只暴露:
 * <ul>
 *   <li>{@link #ping(IntegrationConnector)} - 取 token 测连通(脱敏返回)</li>
 *   <li>{@link #verifyWebhookSignature} - X-ZTF-Token 明文常量时间比对 webhook_secret</li>
 *   <li>{@link #getToken(IntegrationConnector)} - 手搓 ConcurrentHashMap+TTL 缓存(非 Caffeine 库,功能等价)</li>
 *   <li>{@link #getRun(IntegrationConnector, String)} - GET 拉 run 详情(补全 webhook payload 缺失字段时用)</li>
 * </ul>
 *
 * <p>资源 create/update <b>不实现</b>(单向无出站,PLM 不创建/修改 ZTF 侧数据)。
 *
 * <p>凭据 JSON 结构(存 {@code tb_integration_connector.credential_enc},AES-256-GCM 加密):
 * <pre>
 * {
 *   "account":  "plm-bot",
 *   "password": "xxxxx"
 * }
 * </pre>
 *
 * <p>{@link IntegrationConnector#getEndpoint()} = ZTF base URL(如 {@code https://ztf.example.com},不带末尾斜杠),
 *    {@link IntegrationConnector#getWebhookSecret()} = X-ZTF-Token 期望值。
 *
 * @see <a href="../../../../../../../../02-设计/ZTF-集成-设计.md">ZTF 集成设计</a>
 */
@Component
public class ZtfConnectorAdapter implements ConnectorAdapter {

    private static final Logger log = LoggerFactory.getLogger(ZtfConnectorAdapter.class);

    /** Token 缓存 TTL 25 分钟(留续签缓冲) */
    private static final long TOKEN_TTL_MS = 25 * 60 * 1000L;

    @Autowired
    private AesGcmCipher cipher;

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    /** connectorId → (token, expireEpochMs);手搓内存缓存(非 Caffeine,功能等价) */
    private final Map<Long, TokenEntry> tokenCache = new ConcurrentHashMap<>();

    @Override
    public String type() {
        return "ztf";
    }

    @Override
    public String ping(IntegrationConnector connector) throws Exception {
        // 清掉旧缓存,强制重签以真正测连通
        tokenCache.remove(connector.getId());
        String token = getToken(connector);
        return "OK, ztf token=" + maskToken(token);
    }

    @Override
    public boolean verifyWebhookSignature(IntegrationConnector connector, String signature, String timestamp, byte[] rawBody) {
        if (connector.getWebhookSecret() == null || connector.getWebhookSecret().isEmpty()) {
            log.warn("[plm-integration/ztf] connector_id={} 未配 webhook_secret,验签直接拒绝", connector.getId());
            return false;
        }
        if (signature == null) return false;
        // ZTF webhook 自定义 header(X-ZTF-Token)明文常量时间比对
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
            throw new RuntimeException("[errorCode=814] ZTF endpoint 不可达: " + ce.getMessage());
        }
        if (resp.statusCode() == 401 || resp.statusCode() == 403) {
            throw new RuntimeException("[errorCode=813] ZTF token 失败,account/password 错: HTTP " + resp.statusCode());
        }
        if (resp.statusCode() != 200 && resp.statusCode() != 201) {
            throw new RuntimeException("ZTF /tokens HTTP " + resp.statusCode() + ": " + resp.body());
        }
        JSONObject json = JSON.parseObject(resp.body());
        String token = json.getString("token");
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("ZTF /tokens 响应缺 token 字段: " + resp.body());
        }
        tokenCache.put(connector.getId(), new TokenEntry(token, now + TOKEN_TTL_MS));
        return token;
    }

    /**
     * 拉取 ZTF run/task 详情(GET /api/v1/tasks/{taskId})。
     *
     * <p>用于 webhook payload 缺失字段(如 total/duration)时补全。401 自动清缓存重签后重试一次。
     */
    public JSONObject getRun(IntegrationConnector connector, String taskId) throws Exception {
        return getRunInternal(connector, taskId, false);
    }

    private JSONObject getRunInternal(IntegrationConnector connector, String taskId, boolean retried) throws Exception {
        String token = getToken(connector);
        String url = ensureEndpoint(connector) + "/api/v1/tasks/" + taskId;
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
            .header("Token", token)
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(15))
            .GET()
            .build();
        HttpResponse<String> resp;
        try {
            resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (java.net.ConnectException | java.net.http.HttpConnectTimeoutException ce) {
            throw new RuntimeException("[errorCode=814] ZTF endpoint 不可达: " + ce.getMessage());
        }
        if (resp.statusCode() == 401 && !retried) {
            tokenCache.remove(connector.getId());
            return getRunInternal(connector, taskId, true);
        }
        if (resp.statusCode() == 404) {
            throw new RuntimeException("ZTF run 不存在: " + url);
        }
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new RuntimeException("ZTF GET " + url + " HTTP " + resp.statusCode() + ": " + truncate(resp.body(), 500));
        }
        String respBody = resp.body();
        if (respBody == null || respBody.isEmpty()) return new JSONObject();
        Object parsed = JSON.parse(respBody);
        if (parsed instanceof JSONObject jo) return jo;
        JSONObject wrap = new JSONObject();
        wrap.put("raw", respBody);
        return wrap;
    }

    // ────────────────────────────────────────────────────────────────────

    private Credential decryptCredential(IntegrationConnector connector) {
        if (connector.getCredentialEnc() == null || connector.getCredentialEnc().isEmpty()) {
            throw new RuntimeException("[errorCode=805] connector.credential_enc 为空,未配置 ZTF 凭据");
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

    /** ZTF 凭据反序列化 DTO */
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

    /** 提供给 Test 注入 token 缓存(测试用,生产路径不调) */
    Map<Long, TokenEntry> _testTokenCache() {
        return tokenCache;
    }
}
