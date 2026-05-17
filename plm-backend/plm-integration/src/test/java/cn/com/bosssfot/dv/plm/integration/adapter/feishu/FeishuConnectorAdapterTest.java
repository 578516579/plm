package cn.com.bosssfot.dv.plm.integration.adapter.feishu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.alibaba.fastjson2.JSON;

import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.mcp.crypto.AesGcmCipher;

/**
 * FeishuConnectorAdapter 验签 + verifyToken 测试。
 *
 * 不动 HTTP 调用（getTenantAccessToken / sendTextMessage 需要真实飞书，留 Phase 04 集成测试），
 * 只测纯函数 + 解密后凭据校验。
 */
class FeishuConnectorAdapterTest {

    private FeishuConnectorAdapter adapter;
    private AesGcmCipher cipher;
    private IntegrationConnector connector;

    private static final String ENCRYPT_KEY = "test-encrypt-key-xyz-123";
    private static final String VERIFICATION_TOKEN = "vtoken-abc-xyz";

    @BeforeEach
    void setUp() throws Exception {
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        cipher = new AesGcmCipher(keyBytes);

        adapter = new FeishuConnectorAdapter();
        Field cipherField = FeishuConnectorAdapter.class.getDeclaredField("cipher");
        cipherField.setAccessible(true);
        cipherField.set(adapter, cipher);

        Field baseField = FeishuConnectorAdapter.class.getDeclaredField("openapiBase");
        baseField.setAccessible(true);
        baseField.set(adapter, "https://open.feishu.cn/open-apis");

        // 构造一个 connector，credential_enc 是加密后的 Credential JSON
        FeishuConnectorAdapter.Credential cred = new FeishuConnectorAdapter.Credential();
        cred.appId = "cli_test";
        cred.appSecret = "secret-xxx";
        cred.verificationToken = VERIFICATION_TOKEN;
        cred.encryptKey = ENCRYPT_KEY;
        String encJson = cipher.encrypt(JSON.toJSONString(cred));

        connector = new IntegrationConnector();
        connector.setId(1L);
        connector.setConnectorType("feishu");
        connector.setCredentialEnc(encJson);
    }

    @Test
    @DisplayName("type() 返回 feishu")
    void typeIsFeishu() {
        assertThat(adapter.type()).isEqualTo("feishu");
    }

    @Test
    @DisplayName("verifyToken 一致 → 通过")
    void verifyTokenMatch() {
        assertThat(adapter.verifyToken(connector, VERIFICATION_TOKEN)).isTrue();
    }

    @Test
    @DisplayName("verifyToken 不一致 → 拒绝")
    void verifyTokenMismatch() {
        assertThat(adapter.verifyToken(connector, "wrong-token")).isFalse();
        assertThat(adapter.verifyToken(connector, null)).isFalse();
        assertThat(adapter.verifyToken(connector, "")).isFalse();
    }

    @Test
    @DisplayName("HMAC-SHA256 签名匹配 → 通过（加密推送模式）")
    void verifyHmacSignatureMatch() throws Exception {
        String timestamp = "1700000000";
        byte[] body = "{\"event\":\"test\"}".getBytes(StandardCharsets.UTF_8);
        String expectedHex = hmacSha256Hex(ENCRYPT_KEY.getBytes(StandardCharsets.UTF_8),
            (timestamp + new String(body, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8));

        assertThat(adapter.verifyWebhookSignature(connector, expectedHex, timestamp, body)).isTrue();
    }

    @Test
    @DisplayName("HMAC 签名不匹配 → 拒绝")
    void verifyHmacSignatureMismatch() {
        byte[] body = "{\"event\":\"test\"}".getBytes(StandardCharsets.UTF_8);
        assertThat(adapter.verifyWebhookSignature(connector, "deadbeef", "1700000000", body))
            .isFalse();
    }

    @Test
    @DisplayName("connector 未启用加密推送（encryptKey 为空）→ 走 token-in-body 模式,直接放行 HMAC 检查")
    void noEncryptKeyFallsThroughHmacCheck() throws Exception {
        // 重新构造一个 encryptKey 为空的 credential
        FeishuConnectorAdapter.Credential cred = new FeishuConnectorAdapter.Credential();
        cred.appId = "cli_test";
        cred.appSecret = "secret-xxx";
        cred.verificationToken = "v";
        cred.encryptKey = "";
        connector.setCredentialEnc(cipher.encrypt(JSON.toJSONString(cred)));

        // 即使 sig/ts 缺失，明文模式下也应返回 true（让上层 controller 走 body.token 校验）
        assertThat(adapter.verifyWebhookSignature(connector, null, null, new byte[0])).isTrue();
    }

    // ─── 工具方法 ───────────────────────────────────────────────────────────

    private static String hmacSha256Hex(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        byte[] out = mac.doFinal(data);
        StringBuilder sb = new StringBuilder(out.length * 2);
        for (byte b : out) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
