package cn.com.bosssfot.dv.plm.integration.adapter.gitlab;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;

/**
 * GitLabConnectorAdapter 验签测试。
 *
 * 不动 HTTP 调用（PAT/MR 列表需要真实 GitLab，留 Phase 04 集成测试），
 * 这里只测纯函数 {@link GitLabConnectorAdapter#verifyWebhookSignature}。
 */
class GitLabConnectorAdapterTest {

    private final GitLabConnectorAdapter adapter = new GitLabConnectorAdapter();

    private static IntegrationConnector connectorWithSecret(String secret) {
        IntegrationConnector c = new IntegrationConnector();
        c.setId(1L);
        c.setConnectorType("gitlab");
        c.setWebhookSecret(secret);
        return c;
    }

    @Test
    @DisplayName("X-Gitlab-Token 与 webhook_secret 一致 → 通过")
    void verifyMatch() {
        IntegrationConnector c = connectorWithSecret("super-secret-token");
        assertThat(adapter.verifyWebhookSignature(c, "super-secret-token", null,
            "any-body".getBytes(StandardCharsets.UTF_8))).isTrue();
    }

    @Test
    @DisplayName("X-Gitlab-Token 不匹配 → 拒绝")
    void verifyMismatch() {
        IntegrationConnector c = connectorWithSecret("super-secret-token");
        assertThat(adapter.verifyWebhookSignature(c, "wrong-token", null,
            "any-body".getBytes(StandardCharsets.UTF_8))).isFalse();
    }

    @Test
    @DisplayName("connector 未配 webhook_secret → 直接拒绝（防误配通过）")
    void noSecretConfiguredRejects() {
        IntegrationConnector c = connectorWithSecret(null);
        assertThat(adapter.verifyWebhookSignature(c, "any", null, new byte[0])).isFalse();

        c.setWebhookSecret("");
        assertThat(adapter.verifyWebhookSignature(c, "any", null, new byte[0])).isFalse();
    }

    @Test
    @DisplayName("signature 头缺失 → 拒绝")
    void missingSignatureRejects() {
        IntegrationConnector c = connectorWithSecret("secret");
        assertThat(adapter.verifyWebhookSignature(c, null, null, new byte[0])).isFalse();
    }

    @Test
    @DisplayName("type() 返回 gitlab")
    void typeIsGitlab() {
        assertThat(adapter.type()).isEqualTo("gitlab");
    }
}
