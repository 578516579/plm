package cn.com.bosssfot.dv.plm.mcp.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.mcp.domain.McpServer;

/**
 * MCP Server Service
 */
public interface IMcpServerService
{
    List<McpServer> selectMcpServerList(McpServer mcpServer);

    McpServer selectMcpServerById(Long id);

    /** 按编码查（用于 OAuth 鉴权 + tools 注册查找） */
    McpServer selectMcpServerByCode(String serverCode);

    int insertMcpServer(McpServer mcpServer);

    int updateMcpServer(McpServer mcpServer);

    int deleteMcpServerByIds(Long[] ids);
}
