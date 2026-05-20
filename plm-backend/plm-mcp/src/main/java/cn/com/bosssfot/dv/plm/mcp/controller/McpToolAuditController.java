package cn.com.bosssfot.dv.plm.mcp.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.com.bosssfot.dv.plm.common.annotation.Log;
import cn.com.bosssfot.dv.plm.common.core.controller.BaseController;
import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.core.page.TableDataInfo;
import cn.com.bosssfot.dv.plm.common.enums.BusinessType;
import cn.com.bosssfot.dv.plm.common.utils.poi.ExcelUtil;
import cn.com.bosssfot.dv.plm.mcp.domain.McpToolAudit;
import cn.com.bosssfot.dv.plm.mcp.service.IMcpToolAuditService;

/**
 * MCP 工具调用审计 - 只读列表
 */
@RestController
@RequestMapping("/business/mcp/audit")
public class McpToolAuditController extends BaseController
{
    @Autowired
    private IMcpToolAuditService auditService;

    @PreAuthorize("@ss.hasPermi('business:mcp:audit:list')")
    @GetMapping("/list")
    public TableDataInfo list(McpToolAudit audit) {
        startPage();
        return getDataTable(auditService.selectMcpToolAuditList(audit));
    }

    @PreAuthorize("@ss.hasPermi('business:mcp:audit:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(auditService.selectMcpToolAuditById(id));
    }

    @PreAuthorize("@ss.hasPermi('business:mcp:audit:export')")
    @Log(title = "MCP 工具审计", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, McpToolAudit audit) {
        List<McpToolAudit> list = auditService.selectMcpToolAuditList(audit);
        ExcelUtil<McpToolAudit> util = new ExcelUtil<>(McpToolAudit.class);
        util.exportExcel(response, list, "MCP 工具审计");
    }
}
