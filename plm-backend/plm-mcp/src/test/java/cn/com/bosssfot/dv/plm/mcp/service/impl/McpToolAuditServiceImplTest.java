package cn.com.bosssfot.dv.plm.mcp.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cn.com.bosssfot.dv.plm.mcp.domain.McpToolAudit;
import cn.com.bosssfot.dv.plm.mcp.mapper.McpToolAuditMapper;

/**
 * {@link McpToolAuditServiceImpl} 单元测试 — task #4 jacoco 0% → 全覆盖。
 */
@ExtendWith(MockitoExtension.class)
class McpToolAuditServiceImplTest {

    @Mock
    private McpToolAuditMapper mapper;

    @InjectMocks
    private McpToolAuditServiceImpl service;

    @Test
    @DisplayName("selectMcpToolAuditList 委托 mapper")
    void selectList() {
        McpToolAudit q = new McpToolAudit();
        when(mapper.selectMcpToolAuditList(q)).thenReturn(List.of(new McpToolAudit()));
        assertThat(service.selectMcpToolAuditList(q)).hasSize(1);
        verify(mapper).selectMcpToolAuditList(q);
    }

    @Test
    @DisplayName("selectMcpToolAuditById 委托 mapper")
    void selectById() {
        McpToolAudit row = new McpToolAudit();
        when(mapper.selectMcpToolAuditById(1L)).thenReturn(row);
        assertThat(service.selectMcpToolAuditById(1L)).isSameAs(row);
    }

    @Test
    @DisplayName("recordAudit:不传 callTime 自动补 NOW(),mapper.insert 被调用")
    void recordAuditFillsCallTime() {
        McpToolAudit audit = new McpToolAudit();
        when(mapper.insertMcpToolAudit(any())).thenReturn(1);

        int rows = service.recordAudit(audit);

        assertThat(rows).isEqualTo(1);
        assertThat(audit.getCallTime()).isNotNull();
        verify(mapper, times(1)).insertMcpToolAudit(audit);
    }

    @Test
    @DisplayName("recordAudit:已有 callTime 不覆盖")
    void recordAuditKeepsExistingCallTime() {
        McpToolAudit audit = new McpToolAudit();
        Date fixed = new Date(1_700_000_000_000L);
        audit.setCallTime(fixed);
        when(mapper.insertMcpToolAudit(any())).thenReturn(1);

        service.recordAudit(audit);

        assertThat(audit.getCallTime()).isEqualTo(fixed);
    }

    @Test
    @DisplayName("recordAudit:resultBrief > 2000 字符被截断 + '...(truncated)' 后缀")
    void recordAuditTruncatesLongResultBrief() {
        McpToolAudit audit = new McpToolAudit();
        audit.setResultBrief("Z".repeat(3000));
        when(mapper.insertMcpToolAudit(any())).thenReturn(1);

        service.recordAudit(audit);

        assertThat(audit.getResultBrief()).hasSize(2000 + "...(truncated)".length());
        assertThat(audit.getResultBrief()).endsWith("...(truncated)");
    }
}
