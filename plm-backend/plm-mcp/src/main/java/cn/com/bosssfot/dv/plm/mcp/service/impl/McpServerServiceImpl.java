package cn.com.bosssfot.dv.plm.mcp.service.impl;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.mcp.domain.McpServer;
import cn.com.bosssfot.dv.plm.mcp.mapper.McpServerMapper;
import cn.com.bosssfot.dv.plm.mcp.service.IMcpServerService;

/**
 * MCP Server Service 实现
 *
 * <p>状态机（PRD-MAPPING §32 §3）：
 *   0 启用 ⇄ 1 停用
 *   0 启用 → 2 异常
 *   2 异常 → 0 启用 / 1 停用
 */
@Service
public class McpServerServiceImpl implements IMcpServerService
{
    private static final Map<String, Set<String>> STATUS_TRANSITIONS = new HashMap<>();
    static {
        STATUS_TRANSITIONS.put("0", Set.of("1", "2"));
        STATUS_TRANSITIONS.put("1", Set.of("0"));
        STATUS_TRANSITIONS.put("2", Set.of("0", "1"));
    }

    @Autowired
    private McpServerMapper mcpServerMapper;

    @Override
    public List<McpServer> selectMcpServerList(McpServer mcpServer) {
        return mcpServerMapper.selectMcpServerList(mcpServer);
    }

    @Override
    public McpServer selectMcpServerById(Long id) {
        return mcpServerMapper.selectMcpServerById(id);
    }

    @Override
    public McpServer selectMcpServerByCode(String serverCode) {
        return mcpServerMapper.selectMcpServerByCode(serverCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMcpServer(McpServer mcpServer) {
        if (StringUtils.isBlank(mcpServer.getServerCode())) {
            throw new ServiceException("Server 编码不能为空", 601);
        }
        if (StringUtils.isBlank(mcpServer.getServerName())) {
            throw new ServiceException("Server 名称不能为空", 601);
        }
        if (StringUtils.isBlank(mcpServer.getStatus())) {
            mcpServer.setStatus("0");
        }
        mcpServer.setCreateBy(SecurityUtils.getUsername());
        return mcpServerMapper.insertMcpServer(mcpServer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMcpServer(McpServer mcpServer) {
        if (StringUtils.isNotBlank(mcpServer.getStatus())) {
            McpServer old = mcpServerMapper.selectMcpServerById(mcpServer.getId());
            if (old == null) {
                throw new ServiceException("MCP Server 不存在", 801);
            }
            String oldStatus = old.getStatus();
            String newStatus = mcpServer.getStatus();
            if (!oldStatus.equals(newStatus)) {
                Set<String> allowed = STATUS_TRANSITIONS.getOrDefault(oldStatus, Set.of());
                if (!allowed.contains(newStatus)) {
                    throw new ServiceException(
                        "状态 " + statusLabel(oldStatus) + " 不能直接转到 " + statusLabel(newStatus), 701);
                }
            }
        }
        mcpServer.setUpdateBy(SecurityUtils.getUsername());
        return mcpServerMapper.updateMcpServer(mcpServer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMcpServerByIds(Long[] ids) {
        return mcpServerMapper.deleteMcpServerByIds(ids);
    }

    private static String statusLabel(String s) {
        switch (s) {
            case "0": return "启用";
            case "1": return "停用";
            case "2": return "异常";
            default:  return "未知(" + s + ")";
        }
    }
}
