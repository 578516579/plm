package cn.com.bosssfot.dv.plm.integration.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 集成用户映射 tb_integration_user_mapping
 *
 * <p>外部账号(如禅道 account) ↔ PLM {@code sys_user.user_id} 的对照表。
 * 用于双向同步时 reporter/assignee 字段的解析。
 * 容忍:缺映射时 user_id 留 null,业务端按规则降级(留空 / 默认 admin)。
 */
public class IntegrationUserMapping extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;

    /** FK→tb_integration_connector.id */
    private Long connectorId;

    /** 外部账号(禅道 account) */
    @Excel(name = "外部账号")
    private String externalAccount;

    /** PLM sys_user.user_id,null=未映射(容忍) */
    @Excel(name = "PLM 用户ID")
    private Long userId;

    /** 同步方向(字典 biz_integration_user_dir):inbound/outbound/both */
    @Excel(name = "同步方向", dictType = "biz_integration_user_dir")
    private String syncDirection;

    /** 最近一次使用 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最近使用", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastUsedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConnectorId() { return connectorId; }
    public void setConnectorId(Long connectorId) { this.connectorId = connectorId; }
    public String getExternalAccount() { return externalAccount; }
    public void setExternalAccount(String externalAccount) { this.externalAccount = externalAccount; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSyncDirection() { return syncDirection; }
    public void setSyncDirection(String syncDirection) { this.syncDirection = syncDirection; }
    public Date getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Date lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("connectorId", getConnectorId())
            .append("externalAccount", getExternalAccount())
            .append("userId", getUserId())
            .append("syncDirection", getSyncDirection())
            .append("lastUsedAt", getLastUsedAt())
            .toString();
    }
}
