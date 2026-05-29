package cn.com.bosssfot.dv.plm.integration.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;
import cn.com.bosssfot.dv.plm.integration.mapper.IntegrationWebhookEventMapper;

/**
 * {@link IntegrationWebhookEventServiceImpl} 单元测试 — task #7 jacoco 0% → ≥60%。
 *
 * <p>覆盖:① 委托 select  ② receive 字段校验 + 幂等去重 + 验签通过/失败的事件发布
 * ③ markProcessed 时间戳 + 字段透传  ④ retry 状态校验 + 重试计数 + 事件重发布。
 */
@ExtendWith(MockitoExtension.class)
class IntegrationWebhookEventServiceImplTest {

    @Mock
    private IntegrationWebhookEventMapper eventMapper;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private IntegrationWebhookEventServiceImpl service;

    @Nested
    @DisplayName("简单委托")
    class DelegationTests {
        @Test
        @DisplayName("selectEventList 委托 mapper")
        void list() {
            IntegrationWebhookEvent q = new IntegrationWebhookEvent();
            when(eventMapper.selectEventList(q)).thenReturn(List.of(new IntegrationWebhookEvent()));
            assertThat(service.selectEventList(q)).hasSize(1);
        }

        @Test
        @DisplayName("selectEventById 委托 mapper")
        void byId() {
            IntegrationWebhookEvent row = new IntegrationWebhookEvent();
            when(eventMapper.selectEventById(1L)).thenReturn(row);
            assertThat(service.selectEventById(1L)).isSameAs(row);
        }
    }

    @Nested
    @DisplayName("receive — 入站事件落库 + 幂等 + 验签后事件发布")
    class ReceiveTests {
        @Test
        @DisplayName("connectorId 为 null → 601")
        void connectorIdNull() {
            IntegrationWebhookEvent e = new IntegrationWebhookEvent();
            e.setExternalEventId("ext-1");
            assertThatThrownBy(() -> service.receive(e))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不能为空");
        }

        @Test
        @DisplayName("externalEventId 为 null → 601")
        void externalEventIdNull() {
            IntegrationWebhookEvent e = new IntegrationWebhookEvent();
            e.setConnectorId(7L);
            assertThatThrownBy(() -> service.receive(e))
                .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("幂等去重:同 externalEventId 二次 receive 返已存在 + 不重复 INSERT")
        void idempotent() {
            IntegrationWebhookEvent existing = new IntegrationWebhookEvent();
            existing.setId(100L);
            when(eventMapper.selectByConnectorAndExternalId(7L, "ext-1")).thenReturn(existing);

            IntegrationWebhookEvent in = new IntegrationWebhookEvent();
            in.setConnectorId(7L);
            in.setExternalEventId("ext-1");

            IntegrationWebhookEvent result = service.receive(in);

            assertThat(result).isSameAs(existing);
            verify(eventMapper, never()).insertEvent(any());
            verify(publisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("验签通过:processStatus=0 默认 + retryCount=0 默认 + 落库 + 发布 WebhookReceived")
        void verifiedTrue() {
            when(eventMapper.selectByConnectorAndExternalId(7L, "ext-1")).thenReturn(null);

            IntegrationWebhookEvent in = new IntegrationWebhookEvent();
            in.setConnectorId(7L);
            in.setExternalEventId("ext-1");
            in.setSignatureVerified("1");

            IntegrationWebhookEvent result = service.receive(in);

            assertThat(result.getProcessStatus()).isEqualTo("0");
            assertThat(result.getRetryCount()).isEqualTo(0);
            verify(eventMapper).insertEvent(in);
            verify(publisher, times(1)).publishEvent(any(
                IntegrationWebhookEventServiceImpl.WebhookReceived.class));
        }

        @Test
        @DisplayName("验签失败:仍落库审计但不发布业务事件")
        void verifiedFalse() {
            when(eventMapper.selectByConnectorAndExternalId(7L, "ext-1")).thenReturn(null);

            IntegrationWebhookEvent in = new IntegrationWebhookEvent();
            in.setConnectorId(7L);
            in.setExternalEventId("ext-1");
            in.setSignatureVerified("0");

            service.receive(in);

            verify(eventMapper).insertEvent(in);
            verify(publisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("markProcessed — 标记处理结果")
    class MarkProcessedTests {
        @Test
        @DisplayName("processStatus + error + processTime 透传到 mapper.updateEvent")
        void markUpdates() {
            when(eventMapper.updateEvent(any())).thenReturn(1);

            int rows = service.markProcessed(99L, "2", null);

            assertThat(rows).isEqualTo(1);
            ArgumentCaptor<IntegrationWebhookEvent> cap = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
            verify(eventMapper).updateEvent(cap.capture());
            IntegrationWebhookEvent up = cap.getValue();
            assertThat(up.getId()).isEqualTo(99L);
            assertThat(up.getProcessStatus()).isEqualTo("2");
            assertThat(up.getProcessTime()).isNotNull();
        }
    }

    @Nested
    @DisplayName("retry — 失败事件重试")
    class RetryTests {
        @Test
        @DisplayName("事件不存在 → 805")
        void notFound() {
            when(eventMapper.selectEventById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.retry(99L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
        }

        @Test
        @DisplayName("非失败状态 → 701")
        void wrongStatus() {
            IntegrationWebhookEvent e = new IntegrationWebhookEvent();
            e.setId(99L);
            e.setProcessStatus("0");   // 不是 3 失败
            when(eventMapper.selectEventById(99L)).thenReturn(e);

            assertThatThrownBy(() -> service.retry(99L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("仅失败");
        }

        @Test
        @DisplayName("3 失败 → 重置为 1 + retryCount+1 + 重发 WebhookReceived")
        void retryOk() {
            IntegrationWebhookEvent e = new IntegrationWebhookEvent();
            e.setId(99L);
            e.setProcessStatus("3");
            e.setRetryCount(2);
            when(eventMapper.selectEventById(99L)).thenReturn(e);
            when(eventMapper.updateEvent(any())).thenReturn(1);

            int rows = service.retry(99L);

            assertThat(rows).isEqualTo(1);
            ArgumentCaptor<IntegrationWebhookEvent> cap = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
            verify(eventMapper).updateEvent(cap.capture());
            assertThat(cap.getValue().getProcessStatus()).isEqualTo("1");
            assertThat(cap.getValue().getRetryCount()).isEqualTo(3);
            verify(publisher).publishEvent(any(
                IntegrationWebhookEventServiceImpl.WebhookReceived.class));
        }

        @Test
        @DisplayName("retryCount 为 null 时兜底为 0+1=1")
        void retryWithNullCount() {
            IntegrationWebhookEvent e = new IntegrationWebhookEvent();
            e.setId(99L);
            e.setProcessStatus("3");
            // retryCount 不设 → null
            when(eventMapper.selectEventById(99L)).thenReturn(e);
            when(eventMapper.updateEvent(any())).thenReturn(1);

            service.retry(99L);

            ArgumentCaptor<IntegrationWebhookEvent> cap = ArgumentCaptor.forClass(IntegrationWebhookEvent.class);
            verify(eventMapper).updateEvent(cap.capture());
            assertThat(cap.getValue().getRetryCount()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("WebhookReceived 内部类:event getter 返原对象")
    void webhookReceivedGetter() {
        IntegrationWebhookEvent e = new IntegrationWebhookEvent();
        e.setId(7L);
        IntegrationWebhookEventServiceImpl.WebhookReceived wr =
            new IntegrationWebhookEventServiceImpl.WebhookReceived(e);
        assertThat(wr.getEvent()).isSameAs(e);
    }
}
