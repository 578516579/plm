package cn.com.bosssfot.dv.plm.mcp.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.mcp.domain.McpToolAudit;
import cn.com.bosssfot.dv.plm.mcp.mapper.McpToolAuditMapper;
import cn.com.bosssfot.dv.plm.mcp.service.IMcpToolAuditService;

/**
 * MCP 工具调用审计 Service 实现
 */
@Service
public class McpToolAuditServiceImpl implements IMcpToolAuditService
{
    /** 审计 result_brief 最大字节（截断，避免单行膨胀） */
    private static final int MAX_BRIEF_LEN = 2000;

    @Autowired
    private McpToolAuditMapper mcpToolAuditMapper;

    @Override
    public List<McpToolAudit> selectMcpToolAuditList(McpToolAudit audit) {
        return mcpToolAuditMapper.selectMcpToolAuditList(audit);
    }

    @Override
    public McpToolAudit selectMcpToolAuditById(Long id) {
        return mcpToolAuditMapper.selectMcpToolAuditById(id);
    }

    @Override
    public int recordAudit(McpToolAudit audit) {
        if (audit.getCallTime() == null) {
            audit.setCallTime(new Date());
        }
        if (StringUtils.isNotBlank(audit.getResultBrief()) && audit.getResultBrief().length() > MAX_BRIEF_LEN) {
            audit.setResultBrief(audit.getResultBrief().substring(0, MAX_BRIEF_LEN) + "...(truncated)");
        }
        return mcpToolAuditMapper.insertMcpToolAudit(audit);
    }
}
