package cn.com.bosssfot.dv.plm.openspec.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * AI OpenSpec 对象 tb_openspec — PRD §F3.5 + 原型 aispec.html
 * Spec as Code: OpenAPI 3.1 / AsyncAPI 3.0 / AI Function / GraphQL
 */
public class Openspec extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long openspecId;
    @Excel(name = "规范编号")   private String openspecNo;
    @Excel(name = "规范名称")   private String specName;
    @Excel(name = "规范类型")   private String specType;
    @Excel(name = "描述")       private String description;
    private String specContent;
    @Excel(name = "版本")       private String version;
    @Excel(name = "AgriKB引用") private String agriKbRef;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    @Excel(name = "状态")       private String status;
    private Long authorUserId;
    private String delFlag;

    public Long getOpenspecId() { return openspecId; }
    public void setOpenspecId(Long v) { this.openspecId = v; }
    public String getOpenspecNo() { return openspecNo; }
    public void setOpenspecNo(String v) { this.openspecNo = v; }
    public String getSpecName() { return specName; }
    public void setSpecName(String v) { this.specName = v; }
    public String getSpecType() { return specType; }
    public void setSpecType(String v) { this.specType = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getSpecContent() { return specContent; }
    public void setSpecContent(String v) { this.specContent = v; }
    public String getVersion() { return version; }
    public void setVersion(String v) { this.version = v; }
    public String getAgriKbRef() { return agriKbRef; }
    public void setAgriKbRef(String v) { this.agriKbRef = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
    public Date getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(Date v) { this.aiGeneratedAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long v) { this.authorUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("openspecId", openspecId).append("openspecNo", openspecNo)
            .append("specName", specName).append("specType", specType)
            .append("version", version).append("status", status).toString();
    }
}
