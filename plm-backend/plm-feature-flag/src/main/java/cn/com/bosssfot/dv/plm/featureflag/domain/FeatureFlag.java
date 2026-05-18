package cn.com.bosssfot.dv.plm.featureflag.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 功能开关对象 tb_feature_flag
 *
 * 唯一标识: flagKey（无自动编号）
 * 启用/禁用: enabled Y/N (无 status 字段)
 * 灰度策略: rolloutStrategy + rolloutPercentage
 */
public class FeatureFlag extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long flagId;
    @Excel(name = "开关Key") private String flagKey;
    @Excel(name = "功能名称") private String flagName;
    private String description;
    @Excel(name = "环境") private String environment;
    @Excel(name = "灰度策略") private String rolloutStrategy;
    @Excel(name = "灰度百分比") private Integer rolloutPercentage;
    private String userWhitelist;
    @Excel(name = "是否开启") private String enabled;
    @Excel(name = "项目ID") private Long projectId;
    private Long authorUserId;
    private String delFlag;

    public Long getFlagId() { return flagId; }
    public void setFlagId(Long v) { this.flagId = v; }
    public String getFlagKey() { return flagKey; }
    public void setFlagKey(String v) { this.flagKey = v; }
    public String getFlagName() { return flagName; }
    public void setFlagName(String v) { this.flagName = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String v) { this.environment = v; }
    public String getRolloutStrategy() { return rolloutStrategy; }
    public void setRolloutStrategy(String v) { this.rolloutStrategy = v; }
    public Integer getRolloutPercentage() { return rolloutPercentage; }
    public void setRolloutPercentage(Integer v) { this.rolloutPercentage = v; }
    public String getUserWhitelist() { return userWhitelist; }
    public void setUserWhitelist(String v) { this.userWhitelist = v; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String v) { this.enabled = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long v) { this.authorUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("flagId", flagId)
            .append("flagKey", flagKey)
            .append("flagName", flagName)
            .append("environment", environment)
            .append("enabled", enabled)
            .toString();
    }
}
