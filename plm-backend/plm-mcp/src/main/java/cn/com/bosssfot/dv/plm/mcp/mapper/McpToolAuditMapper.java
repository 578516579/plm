package cn.com.bosssfot.dv.plm.mcp.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.mcp.domain.McpToolAudit;

/**
 * MCP 工具调用审计 Mapper
 */
public interface McpToolAuditMapper
{
    List<McpToolAudit> selectMcpToolAuditList(McpToolAudit audit);

    McpToolAudit selectMcpToolAuditById(Long id);

    int insertMcpToolAudit(McpToolAudit audit);
}
