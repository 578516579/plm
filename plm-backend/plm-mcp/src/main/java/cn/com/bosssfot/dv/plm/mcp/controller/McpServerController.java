package cn.com.bosssfot.dv.plm.mcp.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import cn.com.bosssfot.dv.plm.common.annotation.Log;
import cn.com.bosssfot.dv.plm.common.core.controller.BaseController;
import cn.com.bosssfot.dv.plm.common.core.domain.AjaxResult;
import cn.com.bosssfot.dv.plm.common.core.page.TableDataInfo;
import cn.com.bosssfot.dv.plm.common.enums.BusinessType;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.common.utils.poi.ExcelUtil;
import cn.com.bosssfot.dv.plm.mcp.crypto.AesGcmCipher;
import cn.com.bosssfot.dv.plm.mcp.domain.McpServer;
import cn.com.bosssfot.dv.plm.mcp.service.IMcpServerService;

/**
 * MCP Server 配置 CRUD
 *
 * <p>路径前缀 {@code /business/mcp/server}，走 RuoYi 标准 JWT 鉴权 + @ss.hasPermi。
 */
@RestController
@RequestMapping("/business/mcp/server")
public class McpServerController extends BaseController
{
    @Autowired
    private IMcpServerService mcpServerService;

    @Autowired
    private AesGcmCipher cipher;

    @PreAuthorize("@ss.hasPermi('business:mcp:server:list')")
    @GetMapping("/list")
    public TableDataInfo list(McpServer mcpServer) {
        startPage();
        return getDataTable(mcpServerService.selectMcpServerList(mcpServer));
    }

    @PreAuthorize("@ss.hasPermi('business:mcp:server:export')")
    @Log(title = "MCP Server", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, McpServer mcpServer) {
        List<McpServer> list = mcpServerService.selectMcpServerList(mcpServer);
        ExcelUtil<McpServer> util = new ExcelUtil<>(McpServer.class);
        util.exportExcel(response, list, "MCP Server");
    }

    @PreAuthorize("@ss.hasPermi('business:mcp:server:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        McpServer server = mcpServerService.selectMcpServerById(id);
        if (server == null) {
            throw new ServiceException("MCP Server 不存在", 801);
        }
        return success(server);
    }

    /**
     * 新增 — 客户端在 body 里传明文 oauth_client_secret（字段名 oauthClientSecretPlain），
     * 后端加密后存 oauth_client_secret_enc。
     */
    @PreAuthorize("@ss.hasPermi('business:mcp:server:add')")
    @Log(title = "MCP Server", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody McpServerCreateRequest req) {
        McpServer entity = req.toEntity();
        if (StringUtils.isNotBlank(req.getOauthClientSecretPlain())) {
            entity.setOauthClientSecretEnc(cipher.encrypt(req.getOauthClientSecretPlain()));
        }
        entity.setCreateBy(SecurityUtils.getUsername());
        return toAjax(mcpServerService.insertMcpServer(entity));
    }

    @PreAuthorize("@ss.hasPermi('business:mcp:server:edit')")
    @Log(title = "MCP Server", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody McpServerCreateRequest req) {
        if (req.getId() == null) {
            throw new ServiceException("id 不能为空", 601);
        }
        McpServer entity = req.toEntity();
        if (StringUtils.isNotBlank(req.getOauthClientSecretPlain())) {
            entity.setOauthClientSecretEnc(cipher.encrypt(req.getOauthClientSecretPlain()));
        }
        return toAjax(mcpServerService.updateMcpServer(entity));
    }

    @PreAuthorize("@ss.hasPermi('business:mcp:server:remove')")
    @Log(title = "MCP Server", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(mcpServerService.deleteMcpServerByIds(ids));
    }

    /**
     * 入参 DTO —— 用 createRequest 把"明文 secret"和"加密后 secret"两个字段分开。
     * 列表/详情响应仍用实体 {@link McpServer}（其中 oauthClientSecretEnc 被 JsonIgnore 隐藏）。
     */
    public static class McpServerCreateRequest {
        private Long id;
        private String serverCode;
        private String serverName;
        private String protocol;
        private String endpoint;
        private String authType;
        private String oauthClientId;
        private String oauthClientSecretPlain;   // 明文，仅用于本次写入
        private String toolsJson;
        private String status;
        private String description;
        private String remark;

        public McpServer toEntity() {
            McpServer e = new McpServer();
            e.setId(id);
            e.setServerCode(serverCode);
            e.setServerName(serverName);
            e.setProtocol(protocol);
            e.setEndpoint(endpoint);
            e.setAuthType(authType);
            e.setOauthClientId(oauthClientId);
            e.setToolsJson(toolsJson);
            e.setStatus(status);
            e.setDescription(description);
            e.setRemark(remark);
            return e;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getServerCode() { return serverCode; }
        public void setServerCode(String serverCode) { this.serverCode = serverCode; }
        public String getServerName() { return serverName; }
        public void setServerName(String serverName) { this.serverName = serverName; }
        public String getProtocol() { return protocol; }
        public void setProtocol(String protocol) { this.protocol = protocol; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getAuthType() { return authType; }
        public void setAuthType(String authType) { this.authType = authType; }
        public String getOauthClientId() { return oauthClientId; }
        public void setOauthClientId(String oauthClientId) { this.oauthClientId = oauthClientId; }
        public String getOauthClientSecretPlain() { return oauthClientSecretPlain; }
        public void setOauthClientSecretPlain(String oauthClientSecretPlain) { this.oauthClientSecretPlain = oauthClientSecretPlain; }
        public String getToolsJson() { return toolsJson; }
        public void setToolsJson(String toolsJson) { this.toolsJson = toolsJson; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}
