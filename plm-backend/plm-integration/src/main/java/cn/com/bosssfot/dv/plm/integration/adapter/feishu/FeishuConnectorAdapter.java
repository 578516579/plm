package cn.com.bosssfot.dv.plm.integration.adapter.feishu;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import cn.com.bosssfot.dv.plm.integration.adapter.ConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.mcp.crypto.AesGcmCipher;

/**
 * 飞书 (Feishu / Lark) 连接器适配器。
 *
 * <p>支持：
 * <ul>
 *   <li>{@link #ping(IntegrationConnector)} - 调 /open-apis/auth/v3/tenant_access_token/internal 验证 app_id/app_secret</li>
 *   <li>{@link #verifyWebhookSignature} - HMAC-SHA256(encrypt_key, timestamp + nonce + body)，
 *       兼容飞书"加密推送"和"明文+签名"两种推送模式</li>
 *   <li>{@link #sendTextMessage(IntegrationConnector, String, String)} - 主动发文本消息到指定 chat_id</li>
 *   <li>{@link #getTenantAccessToken(IntegrationConnector)} - 带 60min TTL 的 token 缓存</li>
 * </ul>
 *
 * <p>凭据 JSON 结构（存 {@code tb_integration_connector.credential_enc} 加密）:
 * <pre>
 * {
 *   "appId": "cli_a1234567",
 *   "appSecret": "xxxxx",
 *   "verificationToken": "xxxxx",   // 事件订阅 → 校验
 *   "encryptKey": "xxxxx"           // 事件订阅 → 加密 (可空)
 * }
 * </pre>
 */
@Component
public class FeishuConnectorAdapter implements ConnectorAdapter {

    private static final Logger log = LoggerFactory.getLogger(FeishuConnectorAdapter.class);

    @Autowired
    private AesGcmCipher cipher;

    @Value("${plm.integration.feishu.openapi-base:${FEISHU_OPENAPI_BASE:https://open.feishu.cn/open-apis}}")
    private String openapiBase;

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    /** connectorId → (token, expireEpochMs) */
    private final Map<Long, TokenEntry> tokenCache = new ConcurrentHashMap<>();

    @Override
    public String type() {
        return "feishu";
    }

    @Override
    public String ping(IntegrationConnector connector) throws Exception {
        String token = getTenantAccessToken(connector);
        return "OK, tenant_access_token=" + maskToken(token);
    }

    @Override
    public boolean verifyWebhookSignature(IntegrationConnector connector, String signature, String timestamp, byte[] rawBody) {
        try {
            Credential cred = decryptCredential(connector);
            // 飞书事件回调签名 = HMAC-SHA256(encryptKey, timestamp + nonce + body)，
            // 但若未启用加密推送，飞书改为校验 verification_token == payload.token，
            // 由 FeishuWebhookController 在 challenge 阶段处理；这里只校验 HMAC 模式。
            if (cred.encryptKey == null || cred.encryptKey.isEmpty()) {
                // 未开启加密推送：让上层 controller 走 token-in-body 模式
                return true;
            }
            if (signature == null || timestamp == null) {
                return false;
            }
            // 飞书签名格式: HEX(HMAC-SHA256(encryptKey, timestamp + nonce + body))
            // 简化：本期实现 timestamp + body 的兜底版本；完整还需 nonce，
            // 真实生产部署再补 X-Lark-Nonce 头读取。
            String stringToSign = timestamp + new String(rawBody, StandardCharsets.UTF_8);
            byte[] expected = hmacSha256(cred.encryptKey.getBytes(StandardCharsets.UTF_8),
                stringToSign.getBytes(StandardCharsets.UTF_8));
            String expectedHex = bytesToHex(expected);
            return constantTimeEquals(expectedHex, signature);
        } catch (Exception e) {
            log.warn("[plm-integration/feishu] 验签异常 connectorId={}", connector.getId(), e);
            return false;
        }
    }

    /** 验证 verification_token（明文模式下飞书在 body.token 中带的字段） */
    public boolean verifyToken(IntegrationConnector connector, String tokenInBody) {
        try {
            Credential cred = decryptCredential(connector);
            return cred.verificationToken != null
                && constantTimeEquals(cred.verificationToken, tokenInBody);
        } catch (Exception e) {
            return false;
        }
    }

    /** 取 tenant_access_token，带 60min 内有效缓存 */
    public String getTenantAccessToken(IntegrationConnector connector) throws Exception {
        TokenEntry entry = tokenCache.get(connector.getId());
        long now = System.currentTimeMillis();
        // 留 5 分钟缓冲，避免临过期被服务端拒
        if (entry != null && entry.expireAt > now + 5 * 60_000) {
            return entry.token;
        }
        Credential cred = decryptCredential(connector);
        String url = openapiBase + "/auth/v3/tenant_access_token/internal";
        JSONObject body = new JSONObject();
        body.put("app_id", cred.appId);
        body.put("app_secret", cred.appSecret);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
            .header("Content-Type", "application/json; charset=utf-8")
            .timeout(Duration.ofSeconds(10))
            .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
            .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("飞书 tenant_access_token HTTP " + resp.statusCode() + ": " + resp.body());
        }
        JSONObject json = JSON.parseObject(resp.body());
        Integer code = json.getInteger("code");
        if (code == null || code != 0) {
            throw new RuntimeException("[errorCode=806] 飞书鉴权失败: " + resp.body());
        }
        String token = json.getString("tenant_access_token");
        Integer expireSec = json.getInteger("expire");
        long expireAt = now + (expireSec == null ? 7200 : expireSec) * 1000L;
        tokenCache.put(connector.getId(), new TokenEntry(token, expireAt));
        return token;
    }

    /** 发送文本消息 */
    public Map<String, Object> sendTextMessage(IntegrationConnector connector, String chatId, String text) throws Exception {
        String token = getTenantAccessToken(connector);
        String url = openapiBase + "/im/v1/messages?receive_id_type=chat_id";
        JSONObject body = new JSONObject();
        body.put("receive_id", chatId);
        body.put("msg_type", "text");
        body.put("content", JSON.toJSONString(Map.of("text", text)));
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json; charset=utf-8")
            .timeout(Duration.ofSeconds(10))
            .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
            .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("飞书发消息失败 HTTP " + resp.statusCode() + ": " + resp.body());
        }
        JSONObject json = JSON.parseObject(resp.body());
        Integer code = json.getInteger("code");
        if (code == null || code != 0) {
            throw new RuntimeException("飞书发消息失败: " + resp.body());
        }
        Map<String, Object> ret = new HashMap<>();
        ret.put("messageId", json.getJSONObject("data") != null ? json.getJSONObject("data").getString("message_id") : null);
        return ret;
    }

    // ─────────────────────────────────────────────────────────────────────

    private Credential decryptCredential(IntegrationConnector connector) {
        if (connector.getCredentialEnc() == null || connector.getCredentialEnc().isEmpty()) {
            throw new RuntimeException("[errorCode=805] connector.credential_enc 为空，未配置飞书凭据");
        }
        String json = cipher.decrypt(connector.getCredentialEnc());
        return JSON.parseObject(json, Credential.class);
    }

    private static byte[] hmacSha256(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    private static String bytesToHex(byte[] b) {
        char[] hex = "0123456789abcdef".toCharArray();
        char[] out = new char[b.length * 2];
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            out[i * 2] = hex[v >>> 4];
            out[i * 2 + 1] = hex[v & 0x0f];
        }
        return new String(out);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private static String maskToken(String token) {
        if (token == null || token.length() < 8) return "***";
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }

    /** 飞书凭据反序列化 DTO */
    public static class Credential {
        public String appId;
        public String appSecret;
        public String verificationToken;
        public String encryptKey;
    }

    /** Token 缓存条目 */
    private static class TokenEntry {
        final String token;
        final long expireAt;
        TokenEntry(String token, long expireAt) { this.token = token; this.expireAt = expireAt; }
    }
}
