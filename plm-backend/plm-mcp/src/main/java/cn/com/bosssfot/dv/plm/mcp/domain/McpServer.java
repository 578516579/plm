package cn.com.bosssfot.dv.plm.mcp.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * MCP Server 注册对象 tb_mcp_server
 *
 * 落地 PRD §2.5 工具集 / §3.4 实现 / §4.1 信息架构
 */
public class McpServer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** Server 编码（唯一） */
    @Excel(name = "编码")
    private String serverCode;

    /** Server 名称 */
    @Excel(name = "名称")
    private String serverName;

    /** 协议类型（字典 biz_mcp_protocol：stdio/sse/http） */
    @Excel(name = "协议", dictType = "biz_mcp_protocol")
    private String protocol;

    /** 访问端点（http 模式） */
    private String endpoint;

    /** 鉴权类型（字典 biz_mcp_auth：none/token/oauth2） */
    @Excel(name = "鉴权", dictType = "biz_mcp_auth")
    private String authType;

    /** OAuth 客户端 ID */
    private String oauthClientId;

    /**
     * OAuth 客户端密钥（AES-256-GCM 加密后存储）。
     * 通过 JsonIgnore 防止序列化到响应；仅 Service 层访问。
     */
    @JsonIgnore
    private String oauthClientSecretEnc;

    /** 暴露的工具列表（JSON Schema 数组，原始 JSON 字符串） */
    private String toolsJson;

    /** 状态（字典 biz_mcp_status：0=启用 1=停用 2=异常） */
    @Excel(name = "状态", dictType = "biz_mcp_status")
    private String status;

    /** 最后心跳时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后心跳", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastHealthAt;

    /** 描述 */
    private String description;

    /** 删除标志（0=正常, 2=删除） */
    private String delFlag;

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

    public String getOauthClientSecretEnc() { return oauthClientSecretEnc; }
    public void setOauthClientSecretEnc(String oauthClientSecretEnc) { this.oauthClientSecretEnc = oauthClientSecretEnc; }

    public String getToolsJson() { return toolsJson; }
    public void setToolsJson(String toolsJson) { this.toolsJson = toolsJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getLastHealthAt() { return lastHealthAt; }
    public void setLastHealthAt(Date lastHealthAt) { this.lastHealthAt = lastHealthAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("serverCode", getServerCode())
            .append("serverName", getServerName())
            .append("protocol", getProtocol())
            .append("endpoint", getEndpoint())
            .append("authType", getAuthType())
            .append("status", getStatus())
            .append("lastHealthAt", getLastHealthAt())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
