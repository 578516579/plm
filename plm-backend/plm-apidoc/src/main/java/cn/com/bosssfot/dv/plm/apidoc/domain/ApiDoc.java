package cn.com.bosssfot.dv.plm.apidoc.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * API 文档对象 tb_apidoc — PRD §F5.4 + 原型 apidoc.html
 * 从代码注释自动提取 + OpenAPI 规范 + 在线调试
 * 3 状态机: 00 草稿 → 01 已发布 → 02 已废弃
 * 唯一键: (http_method, path, version)
 */
public class ApiDoc extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long apidocId;
    @Excel(name = "API编号") private String apidocNo;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "接口标题") private String title;
    @Excel(name = "HTTP方法") private String httpMethod;
    @Excel(name = "接口路径") private String path;
    private String description;
    private String requestSchema;
    private String responseSchema;
    private String openapiSpec;
    private String sourceClass;
    private String sourceMethod;
    @Excel(name = "版本") private String version;
    @Excel(name = "状态") private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date lastSyncedAt;
    private String autoExtracted;
    private String delFlag;

    public Long getApidocId() { return apidocId; }
    public void setApidocId(Long v) { this.apidocId = v; }
    public String getApidocNo() { return apidocNo; }
    public void setApidocNo(String v) { this.apidocNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
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
    public String getSourceClass() { return sourceClass; }
    public void setSourceClass(String v) { this.sourceClass = v; }
    public String getSourceMethod() { return sourceMethod; }
    public void setSourceMethod(String v) { this.sourceMethod = v; }
    public String getVersion() { return version; }
    public void setVersion(String v) { this.version = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Date getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(Date v) { this.lastSyncedAt = v; }
    public String getAutoExtracted() { return autoExtracted; }
    public void setAutoExtracted(String v) { this.autoExtracted = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("apidocId", apidocId)
            .append("apidocNo", apidocNo)
            .append("httpMethod", httpMethod)
            .append("path", path)
            .append("version", version)
            .append("status", status)
            .toString();
    }
}
