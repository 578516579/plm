package cn.com.bosssfot.dv.plm.integration.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import cn.com.bosssfot.dv.plm.integration.adapter.gitlab.GitLabConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationWebhookEventService;

/**
 * {@link GitLabWebhookController} 入站验签 + 落 event + fallbackEventId 测试。
 *
 * <p>覆盖（设计 §10 / §14）:
 * <ul>
 *   <li>X-Gitlab-Token 验签通过 → 200 + event.eventType=gitlab.&lt;event&gt; externalEventId=UUID header</li>
 *   <li>X-Gitlab-Token 验签失败 → 401 + errorCode 807 + 落 event(verified=0, status=4)</li>
 *   <li>UUID Header 缺 → fallbackEventId 兜底（parse body.object_attributes.id 或 fallback-&lt;nano&gt;）</li>
 *   <li>connector 不存在 / 类型错 / 停用 → 404</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class GitLabWebhookControllerTest {

    @Mock
    private IIntegrationConnectorService connectorService;

    @Mock
    private IIntegrationWebhookEventService eventService;

    @Mock
    private GitLabConnectorAdapter gitlabAdapter;

    @InjectMocks
    private GitLabWebhookController controller;

    private static final String MR_PAYLOAD = """
        {
          "object_kind":"merge_request",
          "object_attributes":{"id":98765,"iid":42,"state":"opened","title":"feat: x"}
        }
        """;

    private static IntegrationConnector gitlabConnector() {
        IntegrationConnector c = new IntegrationConnector();
        c.setId(22L);
        c.setConnectorType("gitlab");
        c.setStatus("0");
        c.setWebhookSecret("gitlab-secret");
        return c;
    }

    private static MockHttpServletRequest req() {
        MockHttpServletRequest r = new MockHttpServletRequest();
        r.setRemoteAddr("203.0.113.22");
        return r;
    }

    // ─── A. X-Gitlab-Token 验签通过 ─────────────────────────────────────────

    @Test
    @DisplayName("A. X-Gitlab-Token 验签通过 → 200 + 落 event(eventType=gitlab.merge_request, externalEventId=Header UUID)")
    void validTokenReturns200AndPersistsEvent() {
        IntegrationConnector c = gitlabConnector();
        when(connectorService.selectConnectorById(22L)).thenReturn(c);
        when(gitlabAdapter.verifyWebhookSignature(eq(c), eq("gitlab-secret"), any(), any())).thenReturn(true);

        ResponseEntity<?> resp = controller.receive(
            22L, MR_PAYLOAD, "gitlab-secret", "Merge Request Hook", "uuid-evt-7777", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(200);

        ArgumentCaptor<IntegrationWebhookEvent> captor = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
        verify(eventService, times(1)).receive(captor.capture());
        IntegrationWebhookEvent saved = captor.getValue();
        assertThat(saved.getConnectorId()).isEqualTo(22L);
        // 大写空格转下划线小写
        assertThat(saved.getEventType()).isEqualTo("gitlab.merge_request_hook");
        assertThat(saved.getExternalEventId()).isEqualTo("uuid-evt-7777");
        assertThat(saved.getSignatureVerified()).isEqualTo("1");
        assertThat(saved.getProcessStatus()).isEqualTo("0");
        assertThat(saved.getSourceIp()).isEqualTo("203.0.113.22");
        // signature 字段在 Controller 里写的是 "X-Gitlab-Token"(token 非 null 时)
        assertThat(saved.getSignature()).isEqualTo("X-Gitlab-Token");
    }

    // ─── B. X-Gitlab-Token 验签失败 ─────────────────────────────────────────

    @Test
    @DisplayName("B. X-Gitlab-Token 验签失败 → 401 + errorCode 807 + 落 event(verified=0, status=4)")
    void invalidTokenReturns401() {
        IntegrationConnector c = gitlabConnector();
        when(connectorService.selectConnectorById(22L)).thenReturn(c);
        when(gitlabAdapter.verifyWebhookSignature(eq(c), eq("bad-token"), any(), any())).thenReturn(false);

        ResponseEntity<?> resp = controller.receive(
            22L, MR_PAYLOAD, "bad-token", "Merge Request Hook", "uuid-x", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(401);
        assertThat(resp.getBody().toString()).contains("807");

        ArgumentCaptor<IntegrationWebhookEvent> captor = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
        verify(eventService, times(1)).receive(captor.capture());
        IntegrationWebhookEvent saved = captor.getValue();
        assertThat(saved.getSignatureVerified()).isEqualTo("0");
        assertThat(saved.getProcessStatus()).isEqualTo("4");
    }

    // ─── C. 无 X-Gitlab-Event-UUID → fallbackEventId ────────────────────────

    @Test
    @DisplayName("C. 无 UUID Header → fallbackEventId 走 body.object_attributes.id(gitlab-oa-<id>)")
    void noUuidHeaderUsesFallbackFromObjectAttributesId() {
        IntegrationConnector c = gitlabConnector();
        when(connectorService.selectConnectorById(22L)).thenReturn(c);
        when(gitlabAdapter.verifyWebhookSignature(eq(c), eq("gitlab-secret"), any(), any())).thenReturn(true);

        ResponseEntity<?> resp = controller.receive(
            22L, MR_PAYLOAD, "gitlab-secret", "Merge Request Hook", null, req());

        assertThat(resp.getStatusCode().value()).isEqualTo(200);

        ArgumentCaptor<IntegrationWebhookEvent> captor = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
        verify(eventService, times(1)).receive(captor.capture());
        IntegrationWebhookEvent saved = captor.getValue();
        // body.object_attributes.id=98765 → gitlab-oa-98765
        assertThat(saved.getExternalEventId()).isEqualTo("gitlab-oa-98765");
    }

    @Test
    @DisplayName("C2. 无 UUID Header 且 body 无 object_attributes.id → fallback-<nanoTime>")
    void noUuidNoObjectAttributesUsesNanoFallback() {
        IntegrationConnector c = gitlabConnector();
        when(connectorService.selectConnectorById(22L)).thenReturn(c);
        when(gitlabAdapter.verifyWebhookSignature(eq(c), eq("gitlab-secret"), any(), any())).thenReturn(true);

        String pushPayload = """
            {"object_kind":"push","ref":"refs/heads/main","commits":[]}
            """;

        ResponseEntity<?> resp = controller.receive(
            22L, pushPayload, "gitlab-secret", "Push Hook", null, req());

        assertThat(resp.getStatusCode().value()).isEqualTo(200);

        ArgumentCaptor<IntegrationWebhookEvent> captor = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
        verify(eventService, times(1)).receive(captor.capture());
        assertThat(captor.getValue().getExternalEventId()).startsWith("fallback-");
    }

    // ─── D. connector 不存在 → 404 ──────────────────────────────────────────

    @Test
    @DisplayName("D. connector 不存在 → 404 + errorCode 805 + 不落 event 不调 adapter")
    void unknownConnectorReturns404() {
        when(connectorService.selectConnectorById(99L)).thenReturn(null);

        ResponseEntity<?> resp = controller.receive(
            99L, MR_PAYLOAD, "gitlab-secret", "Merge Request Hook", "uuid-x", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        assertThat(resp.getBody().toString()).contains("805");
        verify(eventService, never()).receive(any());
        verify(gitlabAdapter, never()).verifyWebhookSignature(any(), any(), any(), any());
    }

    // ─── E. connector 类型错 → 404 ──────────────────────────────────────────

    @Test
    @DisplayName("E. connector 类型非 gitlab(如 feishu) → 404 + 不落 event")
    void wrongConnectorTypeReturns404() {
        IntegrationConnector wrongType = gitlabConnector();
        wrongType.setConnectorType("feishu");
        when(connectorService.selectConnectorById(22L)).thenReturn(wrongType);

        ResponseEntity<?> resp = controller.receive(
            22L, MR_PAYLOAD, "gitlab-secret", "Merge Request Hook", "uuid-x", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        verify(eventService, never()).receive(any());
    }

    // ─── F. connector 停用 → 404 ────────────────────────────────────────────

    @Test
    @DisplayName("F. connector 停用(status=1) → 404 + 不落 event")
    void disabledConnectorReturns404() {
        IntegrationConnector disabled = gitlabConnector();
        disabled.setStatus("1");
        when(connectorService.selectConnectorById(22L)).thenReturn(disabled);

        ResponseEntity<?> resp = controller.receive(
            22L, MR_PAYLOAD, "gitlab-secret", "Merge Request Hook", "uuid-x", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        verify(eventService, never()).receive(any());
    }
}
