package cn.com.bosssfot.dv.plm.mcp.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.mcp.domain.McpServer;

/**
 * MCP Server Mapper
 */
public interface McpServerMapper
{
    List<McpServer> selectMcpServerList(McpServer mcpServer);

    McpServer selectMcpServerById(Long id);

    McpServer selectMcpServerByCode(String serverCode);

    int insertMcpServer(McpServer mcpServer);

    int updateMcpServer(McpServer mcpServer);

    int deleteMcpServerByIds(Long[] ids);
}
