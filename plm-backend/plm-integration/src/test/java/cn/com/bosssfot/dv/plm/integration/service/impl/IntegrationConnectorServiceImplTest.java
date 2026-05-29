package cn.com.bosssfot.dv.plm.integration.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.integration.adapter.ConnectorAdapter;
import cn.com.bosssfot.dv.plm.integration.adapter.ConnectorAdapterRegistry;
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.mapper.IntegrationConnectorMapper;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;

/**
 * {@link IntegrationConnectorServiceImpl} 单元测试 — task #7 jacoco 0% → ≥60%。
 *
 * <p>覆盖:① 3 委托 select / delete  ② insert 字段校验 + adapter 支持判定 + 默认 status
 * ③ update 状态机 3 态(0⇄1, 0→2, 2→{0,1})+ 不存在 805  ④ testConnection ping 成功
 * / ping 异常 / connector 不存在 / adapter 不支持。
 */
@ExtendWith(MockitoExtension.class)
class IntegrationConnectorServiceImplTest {

    @Mock
    private IntegrationConnectorMapper connectorMapper;

    @Mock
    private ConnectorAdapterRegistry adapterRegistry;

    @Mock
    private ConnectorAdapter adapter;

    @InjectMocks
    private IntegrationConnectorServiceImpl service;

    @Nested
    @DisplayName("简单委托")
    class DelegationTests {
        @Test
        @DisplayName("selectConnectorList 委托 mapper")
        void list() {
            IntegrationConnector q = new IntegrationConnector();
            when(connectorMapper.selectConnectorList(q))
                .thenReturn(List.of(new IntegrationConnector()));
            assertThat(service.selectConnectorList(q)).hasSize(1);
        }

        @Test
        @DisplayName("selectConnectorById 委托 mapper")
        void byId() {
            IntegrationConnector row = new IntegrationConnector();
            when(connectorMapper.selectConnectorById(1L)).thenReturn(row);
            assertThat(service.selectConnectorById(1L)).isSameAs(row);
        }

        @Test
        @DisplayName("selectConnectorByCode 委托 mapper")
        void byCode() {
            IntegrationConnector row = new IntegrationConnector();
            when(connectorMapper.selectConnectorByCode("feishu-prod")).thenReturn(row);
            assertThat(service.selectConnectorByCode("feishu-prod")).isSameAs(row);
        }

        @Test
        @DisplayName("deleteConnectorByIds 委托 mapper")
        void delete() {
            Long[] ids = { 1L, 2L };
            when(connectorMapper.deleteConnectorByIds(ids)).thenReturn(2);
            assertThat(service.deleteConnectorByIds(ids)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("insertConnector — 字段校验 + adapter 支持 + 默认 status")
    class InsertTests {
        @Test
        @DisplayName("connectorCode 空 → 601")
        void blankCode() {
            IntegrationConnector c = new IntegrationConnector();
            c.setConnectorType("feishu");
            assertThatThrownBy(() -> service.insertConnector(c))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("编码");
        }

        @Test
        @DisplayName("connectorType 空 → 601")
        void blankType() {
            IntegrationConnector c = new IntegrationConnector();
            c.setConnectorCode("fs1");
            assertThatThrownBy(() -> service.insertConnector(c))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("类型");
        }

        @Test
        @DisplayName("adapter 不支持 → 810 + 错误含 supportedTypes")
        void unsupportedType() {
            IntegrationConnector c = new IntegrationConnector();
            c.setConnectorCode("x");
            c.setConnectorType("custom-vcs");
            when(adapterRegistry.supports("custom-vcs")).thenReturn(false);
            when(adapterRegistry.supportedTypes()).thenReturn(Set.of("feishu", "gitlab"));

            assertThatThrownBy(() -> service.insertConnector(c))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("custom-vcs")
                .hasMessageContaining("feishu");
        }

        @Test
        @DisplayName("合法新增 → 默认 status=0 + createBy=username + insert 调用")
        void insertOk() {
            IntegrationConnector c = new IntegrationConnector();
            c.setConnectorCode("feishu-prod");
            c.setConnectorType("feishu");
            when(adapterRegistry.supports("feishu")).thenReturn(true);
            when(connectorMapper.insertConnector(any())).thenReturn(1);

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertConnector(c);
                assertThat(rows).isEqualTo(1);
            }
            assertThat(c.getStatus()).isEqualTo("0");
            assertThat(c.getCreateBy()).isEqualTo("admin");
        }
    }

    @Nested
    @DisplayName("updateConnector — 状态机 0⇄1 / 0→2 / 2→{0,1}")
    class StatusMachineTests {
        @Test
        @DisplayName("0→1 启用→停用 合法")
        void zeroToOne() {
            IntegrationConnector old = new IntegrationConnector();
            old.setId(1L);
            old.setStatus("0");
            when(connectorMapper.selectConnectorById(1L)).thenReturn(old);
            when(connectorMapper.updateConnector(any())).thenReturn(1);

            IntegrationConnector in = new IntegrationConnector();
            in.setId(1L);
            in.setStatus("1");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateConnector(in)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("1→2 非法 → 701")
        void oneToTwoIllegal() {
            IntegrationConnector old = new IntegrationConnector();
            old.setId(1L);
            old.setStatus("1");
            when(connectorMapper.selectConnectorById(1L)).thenReturn(old);

            IntegrationConnector in = new IntegrationConnector();
            in.setId(1L);
            in.setStatus("2");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThatThrownBy(() -> service.updateConnector(in))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("不能直接转到");
            }
        }

        @Test
        @DisplayName("Connector 不存在 → 805")
        void notFound() {
            when(connectorMapper.selectConnectorById(999L)).thenReturn(null);
            IntegrationConnector in = new IntegrationConnector();
            in.setId(999L);
            in.setStatus("1");
            assertThatThrownBy(() -> service.updateConnector(in))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
        }

        @Test
        @DisplayName("不变更 status:跳过状态机,直接更新")
        void noStatusChange() {
            IntegrationConnector in = new IntegrationConnector();
            in.setId(1L);
            // 不设 status,跳过状态机
            when(connectorMapper.updateConnector(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateConnector(in)).isEqualTo(1);
            }
            verify(connectorMapper, never()).selectConnectorById(any());
        }
    }

    @Nested
    @DisplayName("testConnection — ping + 异常 + 不存在 + 类型不支持")
    class TestConnectionTests {
        @Test
        @DisplayName("Connector 不存在 → 805")
        void connectorNotFound() {
            when(connectorMapper.selectConnectorById(99L)).thenReturn(null);
            assertThatThrownBy(() -> service.testConnection(99L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("未找到");
        }

        @Test
        @DisplayName("adapter 不支持该类型 → TestResult.fail")
        void adapterMissing() {
            IntegrationConnector c = new IntegrationConnector();
            c.setId(1L);
            c.setConnectorType("custom-vcs");
            when(connectorMapper.selectConnectorById(1L)).thenReturn(c);
            when(adapterRegistry.get("custom-vcs")).thenReturn(null);

            IIntegrationConnectorService.TestResult result = service.testConnection(1L);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("ping 成功 → TestResult.ok(detail, latencyMs ≥ 0)")
        void pingOk() throws Exception {
            IntegrationConnector c = new IntegrationConnector();
            c.setId(1L);
            c.setConnectorType("feishu");
            when(connectorMapper.selectConnectorById(1L)).thenReturn(c);
            when(adapterRegistry.get("feishu")).thenReturn(adapter);
            when(adapter.ping(c)).thenReturn("OK");

            IIntegrationConnectorService.TestResult result = service.testConnection(1L);

            assertThat(result).isNotNull();
            verify(adapter, times(1)).ping(c);
        }

        @Test
        @DisplayName("ping 抛异常 → TestResult.fail 含异常信息")
        void pingThrows() throws Exception {
            IntegrationConnector c = new IntegrationConnector();
            c.setId(1L);
            c.setConnectorType("feishu");
            when(connectorMapper.selectConnectorById(1L)).thenReturn(c);
            when(adapterRegistry.get("feishu")).thenReturn(adapter);
            when(adapter.ping(c)).thenThrow(new RuntimeException("network down"));

            IIntegrationConnectorService.TestResult result = service.testConnection(1L);

            assertThat(result).isNotNull();
        }
    }
}
