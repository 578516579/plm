package cn.com.bosssfot.dv.plm.apidesign.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 接口详细设计 — PRD §F3.3
 */
public class Apidesign extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long apidesignId;

    @Excel(name = "接口设计编号")
    private String apidesignNo;

    @Excel(name = "项目ID")
    private Long projectId;

    @Excel(name = "接口设计标题")
    private String title;

    @Excel(name = "HTTP方法")
    private String httpMethod;

    @Excel(name = "接口路径")
    private String apiPath;

    @Excel(name = "接口描述")
    private String description;

    private String requestSchema;

    private String responseSchema;

    private String errorCodes;

    private String openapiContent;

    @Excel(name = "Mock开关")
    private String mockEnabled;

    @Excel(name = "版本")
    private String version;

    private String reviewReport;

    @Excel(name = "AI生成")
    private String aiGenerated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date aiGeneratedAt;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "设计者用户ID")
    private Long authorUserId;

    private Long reviewerUserId;

    public Long getApidesignId() { return apidesignId; }
    public void setApidesignId(Long apidesignId) { this.apidesignId = apidesignId; }

    public String getApidesignNo() { return apidesignNo; }
    public void setApidesignNo(String apidesignNo) { this.apidesignNo = apidesignNo; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getApiPath() { return apiPath; }
    public void setApiPath(String apiPath) { this.apiPath = apiPath; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequestSchema() { return requestSchema; }
    public void setRequestSchema(String requestSchema) { this.requestSchema = requestSchema; }

    public String getResponseSchema() { return responseSchema; }
    public void setResponseSchema(String responseSchema) { this.responseSchema = responseSchema; }

    public String getErrorCodes() { return errorCodes; }
    public void setErrorCodes(String errorCodes) { this.errorCodes = errorCodes; }

    public String getOpenapiContent() { return openapiContent; }
    public void setOpenapiContent(String openapiContent) { this.openapiContent = openapiContent; }

    public String getMockEnabled() { return mockEnabled; }
    public void setMockEnabled(String mockEnabled) { this.mockEnabled = mockEnabled; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getReviewReport() { return reviewReport; }
    public void setReviewReport(String reviewReport) { this.reviewReport = reviewReport; }

    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String aiGenerated) { this.aiGenerated = aiGenerated; }

    public Date getAiGeneratedAt() { return aiGeneratedAt; }
    public void setAiGeneratedAt(Date aiGeneratedAt) { this.aiGeneratedAt = aiGeneratedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long authorUserId) { this.authorUserId = authorUserId; }

    public Long getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(Long reviewerUserId) { this.reviewerUserId = reviewerUserId; }
}
