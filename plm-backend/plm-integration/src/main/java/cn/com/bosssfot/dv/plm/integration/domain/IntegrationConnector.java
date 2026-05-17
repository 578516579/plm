package cn.com.bosssfot.dv.plm.integration.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 集成连接器配置 tb_integration_connector
 */
public class IntegrationConnector extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 连接器编码（唯一） */
    @Excel(name = "编码")
    private String connectorCode;

    /** 名称 */
    @Excel(name = "名称")
    private String connectorName;

    /** 类型（字典 biz_integration_type：feishu/gitlab/dingtalk/jira/figma/zentao/ztf） */
    @Excel(name = "类型", dictType = "biz_integration_type")
    private String connectorType;

    /** 外部系统基址 */
    private String endpoint;

    /** 鉴权类型（字典 biz_integration_auth） */
    @Excel(name = "鉴权", dictType = "biz_integration_auth")
    private String authType;

    /** 凭据 AES-256-GCM 密文（JSON 加密后） */
    @JsonIgnore
    private String credentialEnc;

    /** Webhook 验签 secret（HMAC） */
    @JsonIgnore
    private String webhookSecret;

    /** 配置 JSON（类型特定） */
    private String configJson;

    /** 状态（字典 biz_integration_status：0=启用 1=停用 2=异常） */
    @Excel(name = "状态", dictType = "biz_integration_status")
    private String status;

    /** 最后同步时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后同步", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastSyncAt;

    /** 删除标志 */
    private String delFlag;

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

    public String getCredentialEnc() { return credentialEnc; }
    public void setCredentialEnc(String credentialEnc) { this.credentialEnc = credentialEnc; }

    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }

    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(Date lastSyncAt) { this.lastSyncAt = lastSyncAt; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("connectorCode", getConnectorCode())
            .append("connectorName", getConnectorName())
            .append("connectorType", getConnectorType())
            .append("endpoint", getEndpoint())
            .append("authType", getAuthType())
            .append("status", getStatus())
            .append("lastSyncAt", getLastSyncAt())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
