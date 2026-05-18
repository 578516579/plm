package cn.com.bosssfot.dv.plm.openspec.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * AI规范中心对象 tb_openspec
 *
 * 支持 OpenAPI 3.1 / AsyncAPI 3.0 / GraphQL / AI Function 规范管理
 * 状态机: 00=草稿 → 01=审核中 → 02=已发布 → 03=已废弃
 * AI增强: aiGenerate() 生成含 x-agrikb-ref 注解的 OpenAPI 3.1 YAML
 */
public class Openspec extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long openspecId;
    @Excel(name = "规范编号") private String openspecNo;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "规范名称") private String specName;
    @Excel(name = "规范类型") private String specType;
    @Excel(name = "版本") private String version;
    private String content;
    @Excel(name = "AI增强") private String aiEnhanced;
    @Excel(name = "农业知识库引用") private String agrikbRef;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    @Excel(name = "状态") private String status;
    private Long authorUserId;
    private Long reviewerUserId;
    private String delFlag;

    public Long getOpenspecId() { return openspecId; }
    public void setOpenspecId(Long v) { this.openspecId = v; }
    public String getOpenspecNo() { return openspecNo; }
    public void setOpenspecNo(String v) { this.openspecNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getSpecName() { return specName; }
    public void setSpecName(String v) { this.specName = v; }
    public String getSpecType() { return specType; }
    public void setSpecType(String v) { this.specType = v; }
    public String getVersion() { return version; }
    public void setVersion(String v) { this.version = v; }
    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }
    public String getAiEnhanced() { return aiEnhanced; }
    public void setAiEnhanced(String v) { this.aiEnhanced = v; }
    public String getAgrikbRef() { return agrikbRef; }
    public void setAgrikbRef(String v) { this.agrikbRef = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
    public Date getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(Date v) { this.aiGeneratedAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long v) { this.authorUserId = v; }
    public Long getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(Long v) { this.reviewerUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("openspecId", openspecId)
            .append("openspecNo", openspecNo)
            .append("specName", specName)
            .append("specType", specType)
            .append("status", status)
            .toString();
    }
}
