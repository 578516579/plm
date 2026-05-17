package cn.com.bosssfot.dv.plm.mcp.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.mcp.domain.McpToolAudit;

/**
 * MCP 工具调用审计 Service
 */
public interface IMcpToolAuditService
{
    List<McpToolAudit> selectMcpToolAuditList(McpToolAudit audit);

    McpToolAudit selectMcpToolAuditById(Long id);

    /** 落审计行；不允许业务调用方阻塞，但本期同步写以确保不丢 */
    int recordAudit(McpToolAudit audit);
}
