package cn.com.bosssfot.dv.plm.integration.adapter.gitlab;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.List;
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
 * GitLab 连接器适配器。
 *
 * <p>支持：
 * <ul>
 *   <li>{@link #ping(IntegrationConnector)} - GET /api/v4/user 验证 PAT</li>
 *   <li>{@link #verifyWebhookSignature} - 比较 X-Gitlab-Token == connector.webhook_secret</li>
 *   <li>{@link #listMergeRequests(IntegrationConnector, String, String)} - 拉项目 MR 列表</li>
 * </ul>
 *
 * <p>凭据 JSON 结构（存 {@code tb_integration_connector.credential_enc} 加密）:
 * <pre>
 * {
 *   "token": "glpat-xxxxxxxxxxxxxxxxxxxx"
 * }
 * </pre>
 *
 * <p>{@link IntegrationConnector#getEndpoint()} = GitLab base URL（如 {@code https://gitlab.com}），
 *    {@link IntegrationConnector#getWebhookSecret()} = X-Gitlab-Token 期望值。
 */
@Component
public class GitLabConnectorAdapter implements ConnectorAdapter {

    private static final Logger log = LoggerFactory.getLogger(GitLabConnectorAdapter.class);

    @Autowired
    private AesGcmCipher cipher;

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    @Override
    public String type() {
        return "gitlab";
    }

    @Override
    public String ping(IntegrationConnector connector) throws Exception {
        Credential cred = decryptCredential(connector);
        String url = ensureEndpoint(connector) + "/api/v4/user";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
            .header("PRIVATE-TOKEN", cred.token)
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 401) {
            throw new RuntimeException("[errorCode=806] GitLab token 无效 / 已过期");
        }
        if (resp.statusCode() != 200) {
            throw new RuntimeException("GitLab /user HTTP " + resp.statusCode() + ": " + resp.body());
        }
        JSONObject user = JSON.parseObject(resp.body());
        return "OK, user=" + user.getString("username") + " (" + user.getString("name") + ")";
    }

    @Override
    public boolean verifyWebhookSignature(IntegrationConnector connector, String signature, String timestamp, byte[] rawBody) {
        if (connector.getWebhookSecret() == null || connector.getWebhookSecret().isEmpty()) {
            log.warn("[plm-integration/gitlab] connector_id={} 未配 webhook_secret，验签直接拒绝", connector.getId());
            return false;
        }
        if (signature == null) return false;
        // GitLab webhook 验签：X-Gitlab-Token 头 == 在 GitLab project webhook 配置里设置的 Secret Token（明文比对）
        return constantTimeEquals(signature, connector.getWebhookSecret());
    }

    /** 列出指定项目的 MR */
    public List<JSONObject> listMergeRequests(IntegrationConnector connector, String projectIdOrPath, String state) throws Exception {
        Credential cred = decryptCredential(connector);
        String pathEncoded = URLEncoder.encode(projectIdOrPath, StandardCharsets.UTF_8);
        String stateParam = (state == null || state.isEmpty()) ? "all" : state;
        String url = ensureEndpoint(connector) + "/api/v4/projects/" + pathEncoded
            + "/merge_requests?state=" + stateParam;
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
            .header("PRIVATE-TOKEN", cred.token)
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 404) {
            throw new RuntimeException("项目 " + projectIdOrPath + " 不存在或无权访问");
        }
        if (resp.statusCode() != 200) {
            throw new RuntimeException("GitLab /merge_requests HTTP " + resp.statusCode() + ": " + resp.body());
        }
        return JSON.parseObject(resp.body(), JSONArray.class).toJavaList(JSONObject.class);
    }

    // ─────────────────────────────────────────────────────────────────────

    private Credential decryptCredential(IntegrationConnector connector) {
        if (connector.getCredentialEnc() == null || connector.getCredentialEnc().isEmpty()) {
            throw new RuntimeException("[errorCode=805] connector.credential_enc 为空，未配置 GitLab 凭据");
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

    /** GitLab 凭据反序列化 DTO */
    public static class Credential {
        public String token;
    }
}
