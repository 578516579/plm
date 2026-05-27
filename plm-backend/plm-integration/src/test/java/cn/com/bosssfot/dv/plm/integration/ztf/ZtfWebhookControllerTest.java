package cn.com.bosssfot.dv.plm.integration.ztf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

import cn.com.bosssfot.dv.plm.integration.adapter.ztf.ZtfConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationWebhookEventService;
import cn.com.bosssfot.dv.plm.integration.webhook.ZtfWebhookController;

/**
 * {@link ZtfWebhookController} 入站验签 + 落 event + 幂等测试。
 *
 * <p>覆盖(设计 §10 / §14):
 * <ul>
 *   <li>验签通过 → 落 event(process_status=0)+ 返 200</li>
 *   <li>验签失败 → 落 event(signature_verified=0, process_status=4)+ 返 401 + errorCode 815</li>
 *   <li>connector 不存在 / 非 ztf / 停用 → 404 + 不落 event</li>
 *   <li>幂等:同 externalEventId 二次 receive 委托 eventService.receive(去重在 service 层),验证两次都调用</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ZtfWebhookControllerTest {

    @Mock
    private IIntegrationConnectorService connectorService;

    @Mock
    private IIntegrationWebhookEventService eventService;

    @Mock
    private ZtfConnectorAdapter ztfAdapter;

    @InjectMocks
    private ZtfWebhookController controller;

    private static final String PAYLOAD = """
        {"event":"completed","taskId":"T-1001","autotestNo":"AT-2026-0001",
         "total":48,"pass":45,"fail":3,"duration":120,"finishedAt":"2026-05-27 14:30:12"}
        """;

    private static IntegrationConnector ztfConnector() {
        IntegrationConnector c = new IntegrationConnector();
        c.setId(7L);
        c.setConnectorType("ztf");
        c.setStatus("0");
        c.setWebhookSecret("ztf-secret");
        return c;
    }

    private static MockHttpServletRequest req() {
        MockHttpServletRequest r = new MockHttpServletRequest();
        r.setRemoteAddr("203.0.113.9");
        return r;
    }

    // ─── 验签通过 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("验签通过 → 落 event(status=0, verified=1) + 返 200")
    void validSignatureReturns200AndPersistsEvent() {
        IntegrationConnector c = ztfConnector();
        when(connectorService.selectConnectorById(7L)).thenReturn(c);
        when(ztfAdapter.verifyWebhookSignature(eq(c), eq("ztf-secret"), any(), any())).thenReturn(true);

        ResponseEntity<?> resp = controller.receive(7L, PAYLOAD, "ztf-secret", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(200);

        ArgumentCaptor<IntegrationWebhookEvent> captor = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
        verify(eventService, times(1)).receive(captor.capture());
        IntegrationWebhookEvent saved = captor.getValue();
        assertThat(saved.getConnectorId()).isEqualTo(7L);
        assertThat(saved.getEventType()).isEqualTo("ztf.run.completed");
        assertThat(saved.getSignatureVerified()).isEqualTo("1");
        assertThat(saved.getProcessStatus()).isEqualTo("0");
        // externalEventId 幂等键含 taskId + event(+ finishedAt)
        assertThat(saved.getExternalEventId()).contains("T-1001");
        assertThat(saved.getSourceIp()).isEqualTo("203.0.113.9");
    }

    // ─── 验签失败 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("验签失败 → 落 event(status=4, verified=0) + 返 401 + errorCode 815")
    void invalidSignatureReturns401WithStatus4() {
        IntegrationConnector c = ztfConnector();
        when(connectorService.selectConnectorById(7L)).thenReturn(c);
        when(ztfAdapter.verifyWebhookSignature(eq(c), eq("bad-token"), any(), any())).thenReturn(false);

        ResponseEntity<?> resp = controller.receive(7L, PAYLOAD, "bad-token", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(401);
        assertThat(resp.getBody().toString()).contains("815");

        // 验签失败仍落库审计,但 process_status=4(略)、verified=0
        ArgumentCaptor<IntegrationWebhookEvent> captor = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
        verify(eventService, times(1)).receive(captor.capture());
        IntegrationWebhookEvent saved = captor.getValue();
        assertThat(saved.getSignatureVerified()).isEqualTo("0");
        assertThat(saved.getProcessStatus()).isEqualTo("4");
    }

    // ─── connector 守门 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("connector 不存在 → 404 + 不落 event")
    void unknownConnectorReturns404() {
        when(connectorService.selectConnectorById(99L)).thenReturn(null);

        ResponseEntity<?> resp = controller.receive(99L, PAYLOAD, "x", req());

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        verify(eventService, never()).receive(any());
        verify(ztfAdapter, never()).verifyWebhookSignature(any(), any(), any(), any());
    }

    @Test
    @DisplayName("connector 类型非 ztf / 停用 → 404 + 不落 event")
    void wrongTypeOrDisabledReturns404() {
        // 类型非 ztf
        IntegrationConnector wrongType = ztfConnector();
        wrongType.setConnectorType("gitlab");
        when(connectorService.selectConnectorById(7L)).thenReturn(wrongType);
        assertThat(controller.receive(7L, PAYLOAD, "x", req()).getStatusCode().value()).isEqualTo(404);

        // 停用(status=1)
        IntegrationConnector disabled = ztfConnector();
        disabled.setStatus("1");
        when(connectorService.selectConnectorById(8L)).thenReturn(disabled);
        assertThat(controller.receive(8L, PAYLOAD, "x", req()).getStatusCode().value()).isEqualTo(404);

        verify(eventService, never()).receive(any());
    }

    // ─── 幂等(委托 service.receive) ─────────────────────────────────────────

    @Test
    @DisplayName("同 externalEventId 二次 receive:controller 每次都委托 eventService.receive(去重在 service 层)")
    void duplicateEventDelegatesReceiveEachTime() {
        IntegrationConnector c = ztfConnector();
        when(connectorService.selectConnectorById(7L)).thenReturn(c);
        when(ztfAdapter.verifyWebhookSignature(eq(c), eq("ztf-secret"), any(), any())).thenReturn(true);

        // 同一 payload(同 taskId+event+finishedAt → 同 externalEventId)发两次
        controller.receive(7L, PAYLOAD, "ztf-secret", req());
        controller.receive(7L, PAYLOAD, "ztf-secret", req());

        // controller 不自己去重 —— 两次都委托 service.receive,且 externalEventId 一致(service 层幂等)
        ArgumentCaptor<IntegrationWebhookEvent> captor = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
        verify(eventService, times(2)).receive(captor.capture());
        assertThat(captor.getAllValues().get(0).getExternalEventId())
            .isEqualTo(captor.getAllValues().get(1).getExternalEventId());
    }
}
