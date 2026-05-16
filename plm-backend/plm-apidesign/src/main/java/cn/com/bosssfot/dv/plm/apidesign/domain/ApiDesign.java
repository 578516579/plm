package cn.com.bosssfot.dv.plm.apidesign.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * LLD 接口详细设计对象 tb_apidesign — PRD §F3.3 + 原型 apidesign.html
 *
 * AI 生成 OpenAPI 3.0 规范 + Mock 响应,支持联调 F3.6 Mock 服务
 * 与 tb_apidoc (F5.4 发布交付期) 区分: 本表是「设计期」产物
 * 4 状态机 (含反向边 01→00): 00→01→{00,02}→03 已废弃
 * 唯一键: (project_id, http_method, path) → 701
 */
public class ApiDesign extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long apidesignId;
    @Excel(name = "接口设计编号") private String apidesignNo;
    @Excel(name = "项目ID") private Long projectId;
    private Long archId;
    @Excel(name = "接口标题") private String title;
    @Excel(name = "HTTP方法") private String httpMethod;
    @Excel(name = "接口路径") private String path;
    private String description;
    private String requestSchema;
    private String responseSchema;
    private String openapiSpec;
    @Excel(name = "Mock开启") private String mockEnabled;
    private String mockResponse;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date aiGeneratedAt;
    @Excel(name = "状态") private String status;
    private Long authorUserId;
    private Long reviewerUserId;
    private String delFlag;

    public Long getApidesignId() { return apidesignId; }
    public void setApidesignId(Long v) { this.apidesignId = v; }
    public String getApidesignNo() { return apidesignNo; }
    public void setApidesignNo(String v) { this.apidesignNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public Long getArchId() { return archId; }
    public void setArchId(Long v) { this.archId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String v) { this.httpMethod = v; }
    public String getPath() { return path; }
    public void setPath(String v) { this.path = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getRequestSchema() { return requestSchema; }
    public void setRequestSchema(String v) { this.requestSchema = v; }
    public String getResponseSchema() { return responseSchema; }
    public void setResponseSchema(String v) { this.responseSchema = v; }
    public String getOpenapiSpec() { return openapiSpec; }
    public void setOpenapiSpec(String v) { this.openapiSpec = v; }
    public String getMockEnabled() { return mockEnabled; }
    public void setMockEnabled(String v) { this.mockEnabled = v; }
    public String getMockResponse() { return mockResponse; }
    public void setMockResponse(String v) { this.mockResponse = v; }
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
            .append("apidesignId", apidesignId)
            .append("apidesignNo", apidesignNo)
            .append("httpMethod", httpMethod)
            .append("path", path)
            .append("status", status)
            .toString();
    }
}
