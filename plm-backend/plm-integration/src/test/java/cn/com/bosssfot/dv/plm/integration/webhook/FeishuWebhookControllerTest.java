package cn.com.bosssfot.dv.plm.integration.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import cn.com.bosssfot.dv.plm.integration.adapter.feishu.FeishuConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationWebhookEventService;

/**
 * {@link FeishuWebhookController} 入站验签 + 落 event + URL challenge 测试。
 *
 * <p>覆盖（设计 §10 / §14）:
 * <ul>
 *   <li>业务事件 Header 模式验签通过 / fallback body.token 验签通过 / 验签失败 → 落 event + 状态码</li>
 *   <li>URL verification challenge：token 通过 → 200 + body 含 challenge；token 失败 → 401 + 不落 event</li>
 *   <li>非法 JSON → 400 + errorCode 601 + 不落 event</li>
 *   <li>connector 不存在 / 类型非 feishu / 停用 → 404 + 不落 event 不调 adapter</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class FeishuWebhookControllerTest {

    @Mock
    private IIntegrationConnectorService connectorService;

    @Mock
    private IIntegrationWebhookEventService eventService;

    @Mock
    private FeishuConnectorAdapter feishuAdapter;

    @InjectMocks
    private FeishuWebhookController controller;

    /** 业务事件 payload（v2 header.event_type + header.event_id；token 在顶层用于明文模式 fallback） */
    private static final String BUSINESS_PAYLOAD = """
        {
          "schema":"2.0",
          "token":"vt-good",
          "header":{"event_id":"evt-feishu-1001","event_type":"im.message.receive_v1"},
          "event":{"sender":{"sender_id":{"open_id":"ou_xxx"}}}
        }
        """;

    /** URL verification challenge payload */
    private static final String CHALLENGE_PAYLOAD = """
        {"type":"url_verification","challenge":"chg-abc-123","token":"vt-good"}
        """;

    private static IntegrationConnector feishuConnector() {
        IntegrationConnector c = new IntegrationConnector();
        c.setId(11L);
        c.setConnectorType("feishu");
        c.setStatus("0");
        c.setWebhookSecret("feishu-encrypt-key");
        return c;
    }

    private static MockHttpServletRequest req() {
        MockHttpServletRequest r = new MockHttpServletRequest();
        r.setRemoteAddr("203.0.113.11");
        return r;
    }

    // ─── A. 业务事件 Header 验签通过 ─────────────────────────────────────────

    @Test
    @DisplayName("A. 业务事件 Header sig+ts 验签通过 → 200 + 落 event(verified=1, status=0)")
    void businessEventWithHeaderSignatureReturns200AndPersistsEvent() {
        IntegrationConnector c = feishuConnector();
        when(connectorService.selectConnectorById(11L)).thenReturn(c);
        when(feishuAdapter.verifyWebhookSignature(eq(c), eq("good-sig"), eq("1717000000"), any()))
            .thenReturn(true);

        ResponseEntity<?> resp = controller.receive(11L, BUSINESS_PAYLOAD, "good-sig", "1717000000", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(200);

        ArgumentCaptor<IntegrationWebhookEvent> captor = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
        verify(eventService, times(1)).receive(captor.capture());
        IntegrationWebhookEvent saved = captor.getValue();
        assertThat(saved.getConnectorId()).isEqualTo(11L);
        assertThat(saved.getEventType()).isEqualTo("feishu.im.message.receive_v1");
        assertThat(saved.getExternalEventId()).isEqualTo("evt-feishu-1001");
        assertThat(saved.getSignatureVerified()).isEqualTo("1");
        assertThat(saved.getProcessStatus()).isEqualTo("0");
        assertThat(saved.getSourceIp()).isEqualTo("203.0.113.11");
        assertThat(saved.getSignature()).isEqualTo("good-sig");
    }

    // ─── B. 业务事件 fallback body.token 验签通过 ────────────────────────────

    @Test
    @DisplayName("B. 业务事件 无 Header sig → fallback body.token 验签通过 → 200 + 落 event")
    void businessEventFallbackTokenInBodyVerified() {
        IntegrationConnector c = feishuConnector();
        when(connectorService.selectConnectorById(11L)).thenReturn(c);
        when(feishuAdapter.verifyToken(eq(c), eq("vt-good"))).thenReturn(true);

        ResponseEntity<?> resp = controller.receive(11L, BUSINESS_PAYLOAD, null, null, req());

        assertThat(resp.getStatusCode().value()).isEqualTo(200);

        ArgumentCaptor<IntegrationWebhookEvent> captor = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
        verify(eventService, times(1)).receive(captor.capture());
        IntegrationWebhookEvent saved = captor.getValue();
        assertThat(saved.getSignatureVerified()).isEqualTo("1");
        assertThat(saved.getProcessStatus()).isEqualTo("0");
        // Header 不传 → signature 字段落 null
        assertThat(saved.getSignature()).isNull();
        verify(feishuAdapter, never()).verifyWebhookSignature(any(), any(), any(), any());
    }

    // ─── C. 业务事件验签失败 ────────────────────────────────────────────────

    @Test
    @DisplayName("C. 业务事件验签失败 → 401 + errorCode 807 + 落 event(verified=0, status=4)")
    void businessEventInvalidSignatureReturns401() {
        IntegrationConnector c = feishuConnector();
        when(connectorService.selectConnectorById(11L)).thenReturn(c);
        when(feishuAdapter.verifyWebhookSignature(eq(c), eq("bad-sig"), eq("1717000000"), any()))
            .thenReturn(false);

        ResponseEntity<?> resp = controller.receive(11L, BUSINESS_PAYLOAD, "bad-sig", "1717000000", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(401);
        assertThat(resp.getBody().toString()).contains("807");

        ArgumentCaptor<IntegrationWebhookEvent> captor = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
        verify(eventService, times(1)).receive(captor.capture());
        IntegrationWebhookEvent saved = captor.getValue();
        assertThat(saved.getSignatureVerified()).isEqualTo("0");
        assertThat(saved.getProcessStatus()).isEqualTo("4");
    }

    // ─── D. URL verification challenge 通过 ─────────────────────────────────

    @Test
    @DisplayName("D. URL verification challenge token 通过 → 200 + body 含 challenge 原样回写 + 不落 event")
    void challengeWithValidTokenEchoesChallenge() {
        IntegrationConnector c = feishuConnector();
        when(connectorService.selectConnectorById(11L)).thenReturn(c);
        when(feishuAdapter.verifyToken(eq(c), eq("vt-good"))).thenReturn(true);

        ResponseEntity<?> resp = controller.receive(11L, CHALLENGE_PAYLOAD, null, null, req());

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertThat(body).containsEntry("challenge", "chg-abc-123");

        // challenge 阶段不落 event
        verify(eventService, never()).receive(any());
    }

    // ─── E. URL verification challenge 失败 ─────────────────────────────────

    @Test
    @DisplayName("E. URL verification challenge token 失败 → 401 + errorCode 807 + 不落 event")
    void challengeWithInvalidTokenReturns401() {
        IntegrationConnector c = feishuConnector();
        when(connectorService.selectConnectorById(11L)).thenReturn(c);
        when(feishuAdapter.verifyToken(eq(c), eq("vt-good"))).thenReturn(false);

        ResponseEntity<?> resp = controller.receive(11L, CHALLENGE_PAYLOAD, null, null, req());

        assertThat(resp.getStatusCode().value()).isEqualTo(401);
        assertThat(resp.getBody().toString()).contains("807");
        verify(eventService, never()).receive(any());
    }

    // ─── F. 非法 JSON body ───────────────────────────────────────────────────

    @Test
    @DisplayName("F. 非法 JSON body → 400 + errorCode 601 + 不落 event")
    void invalidJsonBodyReturns400() {
        IntegrationConnector c = feishuConnector();
        when(connectorService.selectConnectorById(11L)).thenReturn(c);

        ResponseEntity<?> resp = controller.receive(11L, "{not-json...", "sig", "ts", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        assertThat(resp.getBody().toString()).contains("601");
        verify(eventService, never()).receive(any());
        verify(feishuAdapter, never()).verifyWebhookSignature(any(), any(), any(), any());
        verify(feishuAdapter, never()).verifyToken(any(), any());
    }

    // ─── G. connector 不存在 ────────────────────────────────────────────────

    @Test
    @DisplayName("G. connector 不存在 → 404 + errorCode 805 + 不落 event 不调 adapter")
    void unknownConnectorReturns404() {
        when(connectorService.selectConnectorById(99L)).thenReturn(null);

        ResponseEntity<?> resp = controller.receive(99L, BUSINESS_PAYLOAD, "sig", "ts", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        assertThat(resp.getBody().toString()).contains("805");
        verify(eventService, never()).receive(any());
        verify(feishuAdapter, never()).verifyWebhookSignature(any(), any(), any(), any());
        verify(feishuAdapter, never()).verifyToken(any(), any());
    }

    // ─── H. connector 类型错(gitlab) ────────────────────────────────────────

    @Test
    @DisplayName("H. connector 类型非 feishu(如 gitlab) → 404 + 不落 event")
    void wrongConnectorTypeReturns404() {
        IntegrationConnector wrongType = feishuConnector();
        wrongType.setConnectorType("gitlab");
        when(connectorService.selectConnectorById(11L)).thenReturn(wrongType);

        ResponseEntity<?> resp = controller.receive(11L, BUSINESS_PAYLOAD, "sig", "ts", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        verify(eventService, never()).receive(any());
    }

    // ─── I. connector 停用 ──────────────────────────────────────────────────

    @Test
    @DisplayName("I. connector 停用(status=1) → 404 + 不落 event")
    void disabledConnectorReturns404() {
        IntegrationConnector disabled = feishuConnector();
        disabled.setStatus("1");
        when(connectorService.selectConnectorById(11L)).thenReturn(disabled);

        ResponseEntity<?> resp = controller.receive(11L, BUSINESS_PAYLOAD, "sig", "ts", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        verify(eventService, never()).receive(any());
    }
}
