package cn.com.bosssfot.dv.plm.integration.ztf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.alibaba.fastjson2.JSON;

import cn.com.bosssfot.dv.plm.integration.adapter.ztf.ZtfConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.mcp.crypto.AesGcmCipher;

/**
 * {@link ZtfConnectorAdapter} 验签 / type / token 缓存 / 凭据解密测试。
 *
 * <p>不动真实 HTTP 调用(ping / getToken 网络往返 / getRun 需真实 ZTF,留 Phase 04 集成测试)。
 * 这里测:
 * <ul>
 *   <li>{@link ZtfConnectorAdapter#type()} = "ztf"</li>
 *   <li>{@link ZtfConnectorAdapter#verifyWebhookSignature} X-ZTF-Token 常量时间比对:对/错/缺/未配 secret</li>
 *   <li>token 缓存命中:预置未过期缓存条目 → getToken 直接返回,<b>不发 HTTP</b>(endpoint 不可达也不抛)</li>
 *   <li>凭据解密:credential_enc AES-GCM 加密后,decryptCredential 还原 account/password</li>
 * </ul>
 *
 * <p>缓存 / 解密用反射注入,范式同 {@code FeishuConnectorAdapterTest}(私有 cipher 字段反射注入)。
 */
class ZtfConnectorAdapterTest {

    private ZtfConnectorAdapter adapter;
    private AesGcmCipher cipher;

    @BeforeEach
    void setUp() throws Exception {
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        cipher = new AesGcmCipher(keyBytes);

        adapter = new ZtfConnectorAdapter();
        Field cipherField = ZtfConnectorAdapter.class.getDeclaredField("cipher");
        cipherField.setAccessible(true);
        cipherField.set(adapter, cipher);
    }

    private static IntegrationConnector connector(Long id, String secret) {
        IntegrationConnector c = new IntegrationConnector();
        c.setId(id);
        c.setConnectorType("ztf");
        c.setStatus("0");
        c.setEndpoint("https://ztf.invalid.example.com"); // 不可达;命中缓存时不会真请求
        c.setWebhookSecret(secret);
        return c;
    }

    // ─── type() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("type() 返回 ztf")
    void typeIsZtf() {
        assertThat(adapter.type()).isEqualTo("ztf");
    }

    // ─── verifyWebhookSignature ──────────────────────────────────────────────

    @Test
    @DisplayName("X-ZTF-Token 与 webhook_secret 一致 → 通过")
    void verifyTokenMatch() {
        IntegrationConnector c = connector(1L, "ztf-secret-token");
        assertThat(adapter.verifyWebhookSignature(c, "ztf-secret-token", null,
            "any-body".getBytes(StandardCharsets.UTF_8))).isTrue();
    }

    @Test
    @DisplayName("X-ZTF-Token 错误 → 拒绝")
    void verifyTokenMismatch() {
        IntegrationConnector c = connector(1L, "ztf-secret-token");
        assertThat(adapter.verifyWebhookSignature(c, "wrong-token", null,
            "any-body".getBytes(StandardCharsets.UTF_8))).isFalse();
        // 长度不同也走常量时间比对的 length 短路 → false
        assertThat(adapter.verifyWebhookSignature(c, "short", null, new byte[0])).isFalse();
    }

    @Test
    @DisplayName("signature 头缺失 → 拒绝")
    void verifyMissingSignatureRejects() {
        IntegrationConnector c = connector(1L, "ztf-secret-token");
        assertThat(adapter.verifyWebhookSignature(c, null, null, new byte[0])).isFalse();
    }

    @Test
    @DisplayName("connector 未配 webhook_secret → 直接拒绝(防误配通过)")
    void verifyNoSecretRejects() {
        IntegrationConnector cNull = connector(1L, null);
        assertThat(adapter.verifyWebhookSignature(cNull, "any", null, new byte[0])).isFalse();

        IntegrationConnector cEmpty = connector(1L, "");
        assertThat(adapter.verifyWebhookSignature(cEmpty, "any", null, new byte[0])).isFalse();
    }

    // ─── token 缓存命中 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("token 缓存命中:未过期条目直接返回,不发 HTTP(endpoint 不可达也不抛)")
    @SuppressWarnings("unchecked")
    void getTokenReturnsCachedWithoutHttp() throws Exception {
        IntegrationConnector c = connector(42L, "secret");

        // 反射拿到私有 tokenCache,塞一个未来过期的 TokenEntry
        Field cacheField = ZtfConnectorAdapter.class.getDeclaredField("tokenCache");
        cacheField.setAccessible(true);
        Map<Long, Object> cache = (Map<Long, Object>) cacheField.get(adapter);

        // TokenEntry 是私有静态嵌套类,反射构造(token, expireAt)
        Class<?> entryClz = Class.forName(
            "cn.com.bosssfot.dv.plm.integration.adapter.ztf.ZtfConnectorAdapter$TokenEntry");
        Constructor<?> ctor = entryClz.getDeclaredConstructor(String.class, long.class);
        ctor.setAccessible(true);
        Object entry = ctor.newInstance("cached-token-xyz", System.currentTimeMillis() + 600_000L);
        cache.put(42L, entry);

        // 命中缓存:若走 HTTP 会因 endpoint 不可达抛 [errorCode=814];这里应直接返回缓存值
        String token = adapter.getToken(c);
        assertThat(token).isEqualTo("cached-token-xyz");
    }

    @Test
    @DisplayName("token 缓存过期:条目过期后命中 HTTP 路径(endpoint 不可达 → 抛 814,证明缓存未命中)")
    @SuppressWarnings("unchecked")
    void getTokenExpiredEntryFallsThroughToHttp() throws Exception {
        IntegrationConnector c = connector(43L, "secret");
        // credential_enc 必须有值(走 HTTP 路径要先解密),否则会先抛 805
        ZtfConnectorAdapter.Credential cred = new ZtfConnectorAdapter.Credential();
        cred.account = "plm-bot";
        cred.password = "pwd";
        c.setCredentialEnc(cipher.encrypt(JSON.toJSONString(cred)));

        Field cacheField = ZtfConnectorAdapter.class.getDeclaredField("tokenCache");
        cacheField.setAccessible(true);
        Map<Long, Object> cache = (Map<Long, Object>) cacheField.get(adapter);

        Class<?> entryClz = Class.forName(
            "cn.com.bosssfot.dv.plm.integration.adapter.ztf.ZtfConnectorAdapter$TokenEntry");
        Constructor<?> ctor = entryClz.getDeclaredConstructor(String.class, long.class);
        ctor.setAccessible(true);
        // 已过期(expireAt 在过去)
        Object expired = ctor.newInstance("stale-token", System.currentTimeMillis() - 1000L);
        cache.put(43L, expired);

        // 缓存过期 → 走 HTTP 重签;endpoint 不可达 → 814(证明没用旧缓存)
        assertThatThrownBy(() -> adapter.getToken(c))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("814");
    }

    // ─── 凭据解密 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("凭据解密:AES-GCM credential_enc 还原 account/password(往返一致)")
    void decryptCredentialRoundTrip() throws Exception {
        ZtfConnectorAdapter.Credential cred = new ZtfConnectorAdapter.Credential();
        cred.account = "plm-bot";
        cred.password = "s3cr3t-pwd";
        String enc = cipher.encrypt(JSON.toJSONString(cred));

        // 直接验证 cipher 解密 + JSON 反序列化等价(decryptCredential 私有,等价测往返)
        String json = cipher.decrypt(enc);
        ZtfConnectorAdapter.Credential back = JSON.parseObject(json, ZtfConnectorAdapter.Credential.class);
        assertThat(back.account).isEqualTo("plm-bot");
        assertThat(back.password).isEqualTo("s3cr3t-pwd");
    }

    @Test
    @DisplayName("credential_enc 为空 → getToken 抛 805(未配凭据)")
    void getTokenMissingCredentialThrows805() {
        IntegrationConnector c = connector(44L, "secret");
        c.setCredentialEnc(null); // 缓存空 + 凭据空 → 解密前先抛 805
        assertThatThrownBy(() -> adapter.getToken(c))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("805");
    }
}
