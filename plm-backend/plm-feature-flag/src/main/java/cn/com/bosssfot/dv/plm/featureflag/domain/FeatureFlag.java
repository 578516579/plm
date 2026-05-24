package cn.com.bosssfot.dv.plm.featureflag.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * Feature Flag 对象 tb_feature_flag — DevOps 扩展 + 原型 featureflag.html
 * 灰度发布 / 紧急开关 / 环境隔离
 */
public class FeatureFlag extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long flagId;
    @Excel(name = "Flag编号")  private String flagNo;
    @Excel(name = "Flag Key")  private String flagKey;
    @Excel(name = "功能说明")  private String title;
    @Excel(name = "描述")      private String description;
    @Excel(name = "环境")      private String environment;
    @Excel(name = "灰度%")     private Integer rolloutPercentage;
    @Excel(name = "灰度策略")  private String rolloutStrategy;
    private String targetUserSegment;
    @Excel(name = "状态")      private String status;
    private Long authorUserId;
    private String delFlag;

    public Long getFlagId() { return flagId; }
    public void setFlagId(Long v) { this.flagId = v; }
    public String getFlagNo() { return flagNo; }
    public void setFlagNo(String v) { this.flagNo = v; }
    public String getFlagKey() { return flagKey; }
    public void setFlagKey(String v) { this.flagKey = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String v) { this.environment = v; }
    public Integer getRolloutPercentage() { return rolloutPercentage; }
    public void setRolloutPercentage(Integer v) { this.rolloutPercentage = v; }
    public String getRolloutStrategy() { return rolloutStrategy; }
    public void setRolloutStrategy(String v) { this.rolloutStrategy = v; }
    public String getTargetUserSegment() { return targetUserSegment; }
    public void setTargetUserSegment(String v) { this.targetUserSegment = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long v) { this.authorUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("flagId", flagId).append("flagNo", flagNo)
            .append("flagKey", flagKey).append("environment", environment)
            .append("rolloutPercentage", rolloutPercentage).append("status", status).toString();
    }
}
