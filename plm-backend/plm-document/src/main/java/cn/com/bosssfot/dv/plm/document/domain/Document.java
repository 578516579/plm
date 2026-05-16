package cn.com.bosssfot.dv.plm.document.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 文档对象 tb_document
 * 合并 5 stub: prd/arch/dbdesign/apidesign/proposal 入单表 + doc_type 字段
 * ADR-0007: DOC-<TYPE>-YYYY-NNNN
 * 4×4 状态机含反向边 01→00 (打回) + 02→01 (重新评审)
 */
public class Document extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long documentId;
    @Excel(name = "文档编号") private String documentNo;
    @Excel(name = "项目ID") private Long projectId;
    private String relatedEntityType;
    private Long relatedEntityId;
    @Excel(name = "类型", dictType = "biz_doc_type") private String docType;
    @Excel(name = "标题") private String title;
    private String content;
    @Excel(name = "版本") private String version;
    @Excel(name = "状态", dictType = "biz_doc_status") private String status;
    @Excel(name = "作者") private Long authorUserId;
    @Excel(name = "审核人") private Long reviewerUserId;
    @Excel(name = "标签") private String tags;
    private String delFlag;

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long v) { this.documentId = v; }
    public String getDocumentNo() { return documentNo; }
    public void setDocumentNo(String v) { this.documentNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String v) { this.relatedEntityType = v; }
    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long v) { this.relatedEntityId = v; }
    public String getDocType() { return docType; }
    public void setDocType(String v) { this.docType = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }
    public String getVersion() { return version; }
    public void setVersion(String v) { this.version = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long v) { this.authorUserId = v; }
    public Long getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(Long v) { this.reviewerUserId = v; }
    public String getTags() { return tags; }
    public void setTags(String v) { this.tags = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("documentId", documentId).append("documentNo", documentNo)
            .append("projectId", projectId).append("docType", docType)
            .append("title", title).append("version", version)
            .append("status", status).append("authorUserId", authorUserId)
            .append("reviewerUserId", reviewerUserId).append("tags", tags)
            .toString();
    }
}
