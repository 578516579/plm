package cn.com.bosssfot.dv.plm.mcp.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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
import cn.com.bosssfot.dv.plm.mcp.domain.McpServer;
import cn.com.bosssfot.dv.plm.mcp.mapper.McpServerMapper;

/**
 * {@link McpServerServiceImpl} 单元测试 — task #4 jacoco 0% → ≥60%。
 *
 * <p>覆盖:3 委托方法 + 字段校验 + 3 态状态机(0⇄1, 0→2, 2→{0,1}) + statusLabel。
 */
@ExtendWith(MockitoExtension.class)
class McpServerServiceImplTest {

    @Mock
    private McpServerMapper mapper;

    @InjectMocks
    private McpServerServiceImpl service;

    @Nested
    @DisplayName("简单委托")
    class DelegationTests {
        @Test
        @DisplayName("selectMcpServerList 委托 mapper")
        void selectList() {
            McpServer q = new McpServer();
            when(mapper.selectMcpServerList(q)).thenReturn(List.of(new McpServer()));
            assertThat(service.selectMcpServerList(q)).hasSize(1);
        }

        @Test
        @DisplayName("selectMcpServerById 委托 mapper")
        void selectById() {
            McpServer row = new McpServer();
            when(mapper.selectMcpServerById(1L)).thenReturn(row);
            assertThat(service.selectMcpServerById(1L)).isSameAs(row);
        }

        @Test
        @DisplayName("selectMcpServerByCode 委托 mapper")
        void selectByCode() {
            McpServer row = new McpServer();
            when(mapper.selectMcpServerByCode("filesystem")).thenReturn(row);
            assertThat(service.selectMcpServerByCode("filesystem")).isSameAs(row);
        }

        @Test
        @DisplayName("deleteMcpServerByIds 委托 mapper")
        void delete() {
            Long[] ids = { 1L, 2L };
            when(mapper.deleteMcpServerByIds(ids)).thenReturn(2);
            assertThat(service.deleteMcpServerByIds(ids)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("insertMcpServer 字段校验 + 默认值")
    class InsertTests {
        @Test
        @DisplayName("serverCode 空 → 601")
        void blankCode() {
            McpServer s = new McpServer();
            s.setServerName("X");
            assertThatThrownBy(() -> service.insertMcpServer(s))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Server 编码");
        }

        @Test
        @DisplayName("serverName 空 → 601")
        void blankName() {
            McpServer s = new McpServer();
            s.setServerCode("fs");
            assertThatThrownBy(() -> service.insertMcpServer(s))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Server 名称");
        }

        @Test
        @DisplayName("合法新增 → 默认 status=0, createBy=username, mapper.insert 调用")
        void insertOk() {
            McpServer s = new McpServer();
            s.setServerCode("filesystem");
            s.setServerName("文件系统");
            when(mapper.insertMcpServer(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                int rows = service.insertMcpServer(s);
                assertThat(rows).isEqualTo(1);
            }
            assertThat(s.getStatus()).isEqualTo("0");
            assertThat(s.getCreateBy()).isEqualTo("admin");
            verify(mapper, times(1)).insertMcpServer(s);
        }
    }

    @Nested
    @DisplayName("updateMcpServer 状态机 0⇄1 / 0→2 / 2→{0,1}")
    class StatusMachineTests {
        @Test
        @DisplayName("0→1 启用→停用 合法")
        void zeroToOne() {
            McpServer old = new McpServer();
            old.setId(1L);
            old.setStatus("0");
            when(mapper.selectMcpServerById(1L)).thenReturn(old);
            when(mapper.updateMcpServer(any())).thenReturn(1);

            McpServer in = new McpServer();
            in.setId(1L);
            in.setStatus("1");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateMcpServer(in)).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("1→2 非法 → 701")
        void oneToTwoIllegal() {
            McpServer old = new McpServer();
            old.setId(1L);
            old.setStatus("1");
            when(mapper.selectMcpServerById(1L)).thenReturn(old);

            McpServer in = new McpServer();
            in.setId(1L);
            in.setStatus("2");
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThatThrownBy(() -> service.updateMcpServer(in))
                    .isInstanceOf(ServiceException.class)
                    .hasMessageContaining("不能直接转到");
            }
        }

        @Test
        @DisplayName("Server 不存在 → 801")
        void notFound() {
            when(mapper.selectMcpServerById(999L)).thenReturn(null);
            McpServer in = new McpServer();
            in.setId(999L);
            in.setStatus("1");
            assertThatThrownBy(() -> service.updateMcpServer(in))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不存在");
        }

        @Test
        @DisplayName("不变更 status:跳过状态机,直接更新")
        void noStatusChange() {
            McpServer in = new McpServer();
            in.setId(1L);
            in.setServerName("rename");   // 不设 status
            when(mapper.updateMcpServer(any())).thenReturn(1);
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getUsername).thenReturn("admin");
                assertThat(service.updateMcpServer(in)).isEqualTo(1);
            }
        }
    }
}
