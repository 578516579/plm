package cn.com.bosssfot.dv.plm.integration.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import cn.com.bosssfot.dv.plm.integration.domain.IntegrationConnector;
import cn.com.bosssfot.dv.plm.integration.service.IIntegrationConnectorService;
import cn.com.bosssfot.dv.plm.mcp.crypto.AesGcmCipher;

/**
 * 集成连接器 CRUD + 连通性测试
 *
 * <p>路径 {@code /business/integration/connector}，走 JWT 鉴权。
 */
@RestController
@RequestMapping("/business/integration/connector")
public class IntegrationConnectorController extends BaseController
{
    @Autowired
    private IIntegrationConnectorService connectorService;

    @Autowired
    private AesGcmCipher cipher;

    @PreAuthorize("@ss.hasPermi('business:integration:connector:list')")
    @GetMapping("/list")
    public TableDataInfo list(IntegrationConnector connector) {
        startPage();
        return getDataTable(connectorService.selectConnectorList(connector));
    }

    @PreAuthorize("@ss.hasPermi('business:integration:connector:export')")
    @Log(title = "集成连接器", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, IntegrationConnector connector) {
        List<IntegrationConnector> list = connectorService.selectConnectorList(connector);
        ExcelUtil<IntegrationConnector> util = new ExcelUtil<>(IntegrationConnector.class);
        util.exportExcel(response, list, "集成连接器");
    }

    @PreAuthorize("@ss.hasPermi('business:integration:connector:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        IntegrationConnector c = connectorService.selectConnectorById(id);
        if (c == null) {
            throw new ServiceException("Connector 不存在", 805);
        }
        return success(c);
    }

    @PreAuthorize("@ss.hasPermi('business:integration:connector:add')")
    @Log(title = "集成连接器", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ConnectorRequest req) {
        IntegrationConnector entity = req.toEntity();
        if (StringUtils.isNotBlank(req.getCredentialJsonPlain())) {
            entity.setCredentialEnc(cipher.encrypt(req.getCredentialJsonPlain()));
        }
        entity.setCreateBy(SecurityUtils.getUsername());
        return toAjax(connectorService.insertConnector(entity));
    }

    @PreAuthorize("@ss.hasPermi('business:integration:connector:edit')")
    @Log(title = "集成连接器", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ConnectorRequest req) {
        if (req.getId() == null) {
            throw new ServiceException("id 不能为空", 601);
        }
        IntegrationConnector entity = req.toEntity();
        if (StringUtils.isNotBlank(req.getCredentialJsonPlain())) {
            entity.setCredentialEnc(cipher.encrypt(req.getCredentialJsonPlain()));
        }
        return toAjax(connectorService.updateConnector(entity));
    }

    @PreAuthorize("@ss.hasPermi('business:integration:connector:remove')")
    @Log(title = "集成连接器", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(connectorService.deleteConnectorByIds(ids));
    }

    /** 连通性 + 鉴权测试 */
    @PreAuthorize("@ss.hasPermi('business:integration:connector:test')")
    @Log(title = "集成连接器测试", businessType = BusinessType.OTHER)
    @PostMapping("/{id}/test")
    public AjaxResult test(@PathVariable("id") Long id) {
        var r = connectorService.testConnection(id);
        Map<String, Object> data = new HashMap<>();
        data.put("ok", r.ok);
        data.put("detail", r.detail);
        data.put("latencyMs", r.latencyMs);
        return r.ok ? success(data) : AjaxResult.error("连通失败: " + r.detail, data);
    }

    /** 入参 DTO（credentialJsonPlain 仅本次写入，不入库明文） */
    public static class ConnectorRequest {
        private Long id;
        private String connectorCode;
        private String connectorName;
        private String connectorType;
        private String endpoint;
        private String authType;
        private String credentialJsonPlain;   // 明文 JSON，写入时加密
        private String webhookSecret;
        private String configJson;
        private String status;
        private String remark;

        public IntegrationConnector toEntity() {
            IntegrationConnector e = new IntegrationConnector();
            e.setId(id);
            e.setConnectorCode(connectorCode);
            e.setConnectorName(connectorName);
            e.setConnectorType(connectorType);
            e.setEndpoint(endpoint);
            e.setAuthType(authType);
            e.setWebhookSecret(webhookSecret);
            e.setConfigJson(configJson);
            e.setStatus(status);
            e.setRemark(remark);
            return e;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getConnectorCode() { return connectorCode; }
        public void setConnectorCode(String connectorCode) { this.connectorCode = connectorCode; }
        public String getConnectorName() { return connectorName; }
        public void setConnectorName(String connectorName) { this.connectorName = connectorName; }
        public String getConnectorType() { return connectorType; }
        public void setConnectorType(String connectorType) { this.connectorType = connectorType; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getAuthType() { return authType; }
        public void setAuthType(String authType) { this.authType = authType; }
        public String getCredentialJsonPlain() { return credentialJsonPlain; }
        public void setCredentialJsonPlain(String credentialJsonPlain) { this.credentialJsonPlain = credentialJsonPlain; }
        public String getWebhookSecret() { return webhookSecret; }
        public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
        public String getConfigJson() { return configJson; }
        public void setConfigJson(String configJson) { this.configJson = configJson; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}
