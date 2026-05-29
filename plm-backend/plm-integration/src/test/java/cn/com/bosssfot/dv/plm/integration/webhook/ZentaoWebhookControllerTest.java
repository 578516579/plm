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

import cn.com.bosssfot.dv.plm.integration.adapter.zentao.ZentaoConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationWebhookEventService;

/**
 * {@link ZentaoWebhookController} 入站验签 + 落 event + 幂等键测试。
 *
 * <p>覆盖（设计 §10 / §14）:
 * <ul>
 *   <li>X-Zentao-Token 验签通过 → 200 + 落 event(eventType=zentao.&lt;objectType&gt;.&lt;action&gt;)</li>
 *   <li>X-Zentao-Token 验签失败 → 401 + errorCode 815(注意：zentao 用 815，feishu/gitlab 用 807)</li>
 *   <li>externalEventId 拼接 zentao-&lt;objectType&gt;-&lt;objectId&gt;-&lt;action&gt;-&lt;lastEditedDate&gt;</li>
 *   <li>connector 不存在 / 类型错 / 停用 → 404</li>
 *   <li>幂等:同 payload 二次 receive → service.receive 调 2 次,externalEventId 一致</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ZentaoWebhookControllerTest {

    @Mock
    private IIntegrationConnectorService connectorService;

    @Mock
    private IIntegrationWebhookEventService eventService;

    @Mock
    private ZentaoConnectorAdapter zentaoAdapter;

    @InjectMocks
    private ZentaoWebhookController controller;

    /** 禅道 Bug 更新事件 payload */
    private static final String BUG_PAYLOAD = """
        {
          "action":"edited",
          "objectType":"bug",
          "objectID":"12345",
          "data":{"lastEditedDate":"2026-05-29 10:20:30","openedDate":"2026-05-28 09:00:00"}
        }
        """;

    private static IntegrationConnector zentaoConnector() {
        IntegrationConnector c = new IntegrationConnector();
        c.setId(33L);
        c.setConnectorType("zentao");
        c.setStatus("0");
        c.setWebhookSecret("zentao-secret");
        return c;
    }

    private static MockHttpServletRequest req() {
        MockHttpServletRequest r = new MockHttpServletRequest();
        r.setRemoteAddr("203.0.113.33");
        return r;
    }

    // ─── A. X-Zentao-Token 验签通过 ─────────────────────────────────────────

    @Test
    @DisplayName("A. X-Zentao-Token 验签通过 → 200 + 落 event(eventType=zentao.bug.edited)")
    void validTokenReturns200AndPersistsEvent() {
        IntegrationConnector c = zentaoConnector();
        when(connectorService.selectConnectorById(33L)).thenReturn(c);
        when(zentaoAdapter.verifyWebhookSignature(eq(c), eq("zentao-secret"), any(), any())).thenReturn(true);

        ResponseEntity<?> resp = controller.receive(33L, BUG_PAYLOAD, "zentao-secret", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(200);

        ArgumentCaptor<IntegrationWebhookEvent> captor = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
        verify(eventService, times(1)).receive(captor.capture());
        IntegrationWebhookEvent saved = captor.getValue();
        assertThat(saved.getConnectorId()).isEqualTo(33L);
        assertThat(saved.getEventType()).isEqualTo("zentao.bug.edited");
        assertThat(saved.getSignatureVerified()).isEqualTo("1");
        assertThat(saved.getProcessStatus()).isEqualTo("0");
        assertThat(saved.getSourceIp()).isEqualTo("203.0.113.33");
        assertThat(saved.getSignature()).isEqualTo("X-Zentao-Token");
    }

    // ─── B. X-Zentao-Token 验签失败 (815 不是 807) ─────────────────────────

    @Test
    @DisplayName("B. X-Zentao-Token 验签失败 → 401 + errorCode 815(注意非 807)")
    void invalidTokenReturns401With815() {
        IntegrationConnector c = zentaoConnector();
        when(connectorService.selectConnectorById(33L)).thenReturn(c);
        when(zentaoAdapter.verifyWebhookSignature(eq(c), eq("bad-token"), any(), any())).thenReturn(false);

        ResponseEntity<?> resp = controller.receive(33L, BUG_PAYLOAD, "bad-token", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(401);
        // 关键：zentao 使用 815，与 feishu/gitlab 的 807 区别
        assertThat(resp.getBody().toString()).contains("815");

        ArgumentCaptor<IntegrationWebhookEvent> captor = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
        verify(eventService, times(1)).receive(captor.capture());
        IntegrationWebhookEvent saved = captor.getValue();
        assertThat(saved.getSignatureVerified()).isEqualTo("0");
        assertThat(saved.getProcessStatus()).isEqualTo("4");
    }

    // ─── C. externalEventId 拼接验证 ────────────────────────────────────────

    @Test
    @DisplayName("C. externalEventId = zentao-<objectType>-<objectId>-<action>-<lastEditedDate>")
    void externalEventIdBuiltFromObjectFieldsAndLastEditedDate() {
        IntegrationConnector c = zentaoConnector();
        when(connectorService.selectConnectorById(33L)).thenReturn(c);
        when(zentaoAdapter.verifyWebhookSignature(eq(c), eq("zentao-secret"), any(), any())).thenReturn(true);

        controller.receive(33L, BUG_PAYLOAD, "zentao-secret", req());

        ArgumentCaptor<IntegrationWebhookEvent> captor = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
        verify(eventService, times(1)).receive(captor.capture());
        // zentao-bug-12345-edited-2026-05-29 10:20:30
        assertThat(captor.getValue().getExternalEventId())
            .isEqualTo("zentao-bug-12345-edited-2026-05-29 10:20:30");
    }

    // ─── D. connector 不存在 → 404 ──────────────────────────────────────────

    @Test
    @DisplayName("D. connector 不存在 → 404 + errorCode 805 + 不落 event 不调 adapter")
    void unknownConnectorReturns404() {
        when(connectorService.selectConnectorById(99L)).thenReturn(null);

        ResponseEntity<?> resp = controller.receive(99L, BUG_PAYLOAD, "zentao-secret", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        assertThat(resp.getBody().toString()).contains("805");
        verify(eventService, never()).receive(any());
        verify(zentaoAdapter, never()).verifyWebhookSignature(any(), any(), any(), any());
    }

    // ─── E. connector 类型错 → 404 ──────────────────────────────────────────

    @Test
    @DisplayName("E. connector 类型非 zentao(如 gitlab) → 404 + 不落 event")
    void wrongConnectorTypeReturns404() {
        IntegrationConnector wrongType = zentaoConnector();
        wrongType.setConnectorType("gitlab");
        when(connectorService.selectConnectorById(33L)).thenReturn(wrongType);

        ResponseEntity<?> resp = controller.receive(33L, BUG_PAYLOAD, "zentao-secret", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        verify(eventService, never()).receive(any());
    }

    // ─── F. 同 payload 二次 receive → 幂等键一致 ────────────────────────────

    @Test
    @DisplayName("F. 同 payload 二次 receive → eventService.receive 调 2 次,externalEventId 一致(去重在 service 层)")
    void duplicatePayloadProducesSameExternalEventId() {
        IntegrationConnector c = zentaoConnector();
        when(connectorService.selectConnectorById(33L)).thenReturn(c);
        when(zentaoAdapter.verifyWebhookSignature(eq(c), eq("zentao-secret"), any(), any())).thenReturn(true);

        controller.receive(33L, BUG_PAYLOAD, "zentao-secret", req());
        controller.receive(33L, BUG_PAYLOAD, "zentao-secret", req());

        ArgumentCaptor<IntegrationWebhookEvent> captor = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
        verify(eventService, times(2)).receive(captor.capture());
        assertThat(captor.getAllValues().get(0).getExternalEventId())
            .isEqualTo(captor.getAllValues().get(1).getExternalEventId());
    }
}
