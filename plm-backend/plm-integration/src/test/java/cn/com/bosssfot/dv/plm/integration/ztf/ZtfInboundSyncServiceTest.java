package cn.com.bosssfot.dv.plm.integration.ztf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationWebhookEvent;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationWebhookEventService;
import cn.com.bosssfot.dv.plm.integration.service.impl.IntegrationWebhookEventServiceImpl.WebhookReceived;
import cn.com.bosssfot.dv.plm.integration.sync.ZtfInboundSyncService;

/**
 * {@link ZtfInboundSyncService} 入站同步测试(单向 Inbound;设计 §6/§14)。
 *
 * <p>覆盖:
 * <ul>
 *   <li>onWebhookReceived 仅处理 {@code ztf.} 前缀;非 ztf / null 直接跳过</li>
 *   <li>connector 不匹配(被删/类型变)→ markProcessed(4)</li>
 *   <li>无匹配 autotest(external_id + autotest_no 都查不到)→ markProcessed(4, 含 821)</li>
 *   <li>stale:finishedAt ≤ last_executed_at → markProcessed(4, 含 819)</li>
 *   <li>正常:JdbcTemplate.update 被调 + markProcessed(2)</li>
 *   <li>首次绑定:external_id 命中失败但 autotest_no 命中 → 回填 external_id=taskId</li>
 * </ul>
 *
 * <p><b>JdbcTemplate.queryForObject(String, Class, Object...) 是 varargs 重载</b> —— Mockito 用
 * matcher 桩两处不同 requiredType 的调用会因 varargs 展开错绑(strict-stubbing 误报)。这里改用
 * 单个 {@code thenAnswer},在 Answer 内按 SQL 文本 + requiredType 路由,避开 varargs matcher 歧义;
 * 配合 {@link Strictness#LENIENT}(裸 JDBC 多桩,部分用例只走其中一支)。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ZtfInboundSyncServiceTest {

    @Mock
    private JdbcTemplate jdbc;

    @Mock
    private IIntegrationConnectorService connectorService;

    @Mock
    private IIntegrationWebhookEventService eventService;

    @InjectMocks
    private ZtfInboundSyncService service;

    private static IntegrationConnector ztfConnector() {
        IntegrationConnector c = new IntegrationConnector();
        c.setId(7L);
        c.setConnectorType("ztf");
        c.setStatus("0");
        return c;
    }

    private static IntegrationWebhookEvent event(long id, String eventType, String payload) {
        IntegrationWebhookEvent e = new IntegrationWebhookEvent();
        e.setId(id);
        e.setConnectorId(7L);
        e.setEventType(eventType);
        e.setPayloadJson(payload);
        return e;
    }

    private static WebhookReceived wrap(IntegrationWebhookEvent e) {
        return new WebhookReceived(e);
    }

    private static final String PAYLOAD = """
        {"event":"completed","taskId":"T-1001","autotestNo":"AT-2026-0001",
         "total":48,"pass":45,"fail":3,"duration":120,"finishedAt":"2026-05-27 14:30:12"}
        """;

    /**
     * 统一桩 JdbcTemplate.queryForObject(String, Class, Object...):
     * 按 SQL 文本(external_id / autotest_no)+ requiredType(Long / Timestamp)路由。
     *
     * @param externalIdHit  byExternalId 查询结果(null=未命中,走 EmptyResultDataAccessException)
     * @param noHit          byNo 查询结果(null=未命中)
     * @param plmLastExecuted FOR UPDATE 锁行返回的 last_executed_at(可 null)
     */
    private void stubQueries(Long externalIdHit, Long noHit, Timestamp plmLastExecuted) {
        when(jdbc.queryForObject(anyString(), any(Class.class), any(Object[].class)))
            .thenAnswer((InvocationOnMock inv) -> {
                String sql = inv.getArgument(0);
                Class<?> type = inv.getArgument(1);
                if (type == Timestamp.class) {
                    return plmLastExecuted; // SELECT last_executed_at ... FOR UPDATE
                }
                // requiredType=Long.class:两种匹配查询
                if (sql.contains("external_id=?")) {
                    if (externalIdHit == null) throw new EmptyResultDataAccessException(1);
                    return externalIdHit;
                }
                if (sql.contains("autotest_no=?")) {
                    if (noHit == null) throw new EmptyResultDataAccessException(1);
                    return noHit;
                }
                return null;
            });
    }

    // ─── 前缀过滤 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("非 ztf 前缀事件 → 直接跳过(不查 connector / 不 markProcessed)")
    void nonZtfPrefixSkipped() {
        service.onWebhookReceived(wrap(event(1L, "gitlab.merge_request", PAYLOAD)));
        service.onWebhookReceived(wrap(event(2L, "zentao.bug", PAYLOAD)));

        verify(connectorService, never()).selectConnectorById(anyLong());
        verify(eventService, never()).markProcessed(anyLong(), anyString(), any());
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    @Test
    @DisplayName("event 为 null / eventType 为 null → 直接跳过")
    void nullEventOrTypeSkipped() {
        service.onWebhookReceived(wrap(null));
        service.onWebhookReceived(wrap(event(3L, null, PAYLOAD)));

        verify(connectorService, never()).selectConnectorById(anyLong());
        verify(eventService, never()).markProcessed(anyLong(), anyString(), any());
    }

    // ─── connector 守门 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("connector 被删 / 类型非 ztf → markProcessed(4) 不匹配")
    void connectorMismatchMarks4() {
        when(connectorService.selectConnectorById(7L)).thenReturn(null);
        service.onWebhookReceived(wrap(event(10L, "ztf.run.completed", PAYLOAD)));
        verify(eventService).markProcessed(eq(10L), eq("4"), anyString());
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    // ─── 无匹配 821 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("external_id + autotest_no 都查不到 → markProcessed(4, 含 errorCode 821)")
    void noMatchMarks4With821() {
        when(connectorService.selectConnectorById(7L)).thenReturn(ztfConnector());
        // 两查询都未命中 → NoMatchException
        stubQueries(null, null, null);

        service.onWebhookReceived(wrap(event(11L, "ztf.run.completed", PAYLOAD)));

        ArgumentCaptor<String> err = ArgumentCaptor.forClass(String.class);
        verify(eventService).markProcessed(eq(11L), eq("4"), err.capture());
        assertThat(err.getValue()).contains("821");
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    // ─── stale 819 ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("finishedAt ≤ last_executed_at → markProcessed(4, 含 errorCode 819 stale),不 update")
    void staleMarks4With819() {
        when(connectorService.selectConnectorById(7L)).thenReturn(ztfConnector());
        // external_id 命中 autotest_id=500;FOR UPDATE 返回比 ZTF finishedAt 更新的时间 → stale
        stubQueries(500L, null, Timestamp.valueOf("2026-05-28 09:00:00"));

        service.onWebhookReceived(wrap(event(12L, "ztf.run.completed", PAYLOAD)));

        ArgumentCaptor<String> err = ArgumentCaptor.forClass(String.class);
        verify(eventService).markProcessed(eq(12L), eq("4"), err.capture());
        assertThat(err.getValue()).contains("819");
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    // ─── 正常路径 ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("正常:external_id 命中 + finishedAt 较新 → jdbc.update 被调 + markProcessed(2)")
    void happyPathUpdatesAndMarks2() {
        when(connectorService.selectConnectorById(7L)).thenReturn(ztfConnector());
        // PLM 现有 last_executed_at 比 ZTF finishedAt 旧 → 覆盖
        stubQueries(500L, null, Timestamp.valueOf("2026-05-01 00:00:00"));
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);

        service.onWebhookReceived(wrap(event(13L, "ztf.run.completed", PAYLOAD)));

        ArgumentCaptor<String> sqlCap = ArgumentCaptor.forClass(String.class);
        verify(jdbc, times(1)).update(sqlCap.capture(), any(Object[].class));
        assertThat(sqlCap.getValue()).contains("UPDATE tb_autotest");
        verify(eventService).markProcessed(eq(13L), eq("2"), any());
    }

    @Test
    @DisplayName("last_executed_at 为 NULL(从未执行)→ 仍覆盖(jdbc.update 被调)")
    void plmNullLastExecutedStillUpdates() {
        when(connectorService.selectConnectorById(7L)).thenReturn(ztfConnector());
        stubQueries(500L, null, null); // PLM last_executed_at NULL
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);

        service.onWebhookReceived(wrap(event(14L, "ztf.run.completed", PAYLOAD)));

        verify(jdbc, times(1)).update(anyString(), any(Object[].class));
        verify(eventService).markProcessed(eq(14L), eq("2"), any());
    }

    // ─── 首次绑定回填 external_id ─────────────────────────────────────────────

    @Test
    @DisplayName("external_id 未命中但 autotest_no 命中 → 首次绑定回填 external_id=taskId(update 参数含 taskId)")
    void firstBindBackfillsExternalId() {
        when(connectorService.selectConnectorById(7L)).thenReturn(ztfConnector());
        // byExternalId 未命中(null),byNo 命中 500
        stubQueries(null, 500L, null);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);

        service.onWebhookReceived(wrap(event(15L, "ztf.run.completed", PAYLOAD)));

        ArgumentCaptor<Object[]> argsCap = ArgumentCaptor.forClass(Object[].class);
        verify(jdbc).update(anyString(), argsCap.capture());
        Object[] args = argsCap.getValue();
        assertThat(args).contains("ztf");     // external_source
        assertThat(args).contains("T-1001");  // 回填的 external_id
        verify(eventService).markProcessed(eq(15L), eq("2"), any());
    }

    // ─── payload 缺关键键 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("payload 缺 taskId 与 autotestNo → markProcessed(4),不查库")
    void payloadMissingKeysMarks4() {
        when(connectorService.selectConnectorById(7L)).thenReturn(ztfConnector());
        String noKeys = "{\"event\":\"completed\",\"total\":1,\"pass\":1}";

        service.onWebhookReceived(wrap(event(16L, "ztf.run.completed", noKeys)));

        verify(eventService).markProcessed(eq(16L), eq("4"), anyString());
        verify(jdbc, never()).queryForObject(anyString(), any(Class.class), any(Object[].class));
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }
}
