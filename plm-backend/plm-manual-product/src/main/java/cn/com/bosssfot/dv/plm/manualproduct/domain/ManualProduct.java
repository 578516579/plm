package cn.com.bosssfot.dv.plm.manualproduct.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 产品手册对象 tb_manual_product — PRD §F5.1 + 原型 productmanual.html
 * AI 一键生成 + 截图上传自动描述 + 多格式导出 (word/pdf/html/h5)
 * 4 状态机: 00 草稿 → 01 生成中 → 02 已生成 → 03 已发布
 */
public class ManualProduct extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long manualproductId;
    @Excel(name = "手册编号") private String manualproductNo;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "手册标题") private String title;
    @Excel(name = "产品版本") private String productVersion;
    @Excel(name = "包含模块") private String includeModules;
    private String content;
    private String screenshotsUrls;
    private Integer screenshotsCount;
    @Excel(name = "导出格式") private String outputFormats;
    private String aiGenerated;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date generatedAt;
    @Excel(name = "状态") private String status;
    private Long authorUserId;
    private String delFlag;

    public Long getManualproductId() { return manualproductId; }
    public void setManualproductId(Long v) { this.manualproductId = v; }
    public String getManualproductNo() { return manualproductNo; }
    public void setManualproductNo(String v) { this.manualproductNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getProductVersion() { return productVersion; }
    public void setProductVersion(String v) { this.productVersion = v; }
    public String getIncludeModules() { return includeModules; }
    public void setIncludeModules(String v) { this.includeModules = v; }
    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }
    public String getScreenshotsUrls() { return screenshotsUrls; }
    public void setScreenshotsUrls(String v) { this.screenshotsUrls = v; }
    public Integer getScreenshotsCount() { return screenshotsCount; }
    public void setScreenshotsCount(Integer v) { this.screenshotsCount = v; }
    public String getOutputFormats() { return outputFormats; }
    public void setOutputFormats(String v) { this.outputFormats = v; }
    public String getAiGenerated() { return aiGenerated; }
    public void setAiGenerated(String v) { this.aiGenerated = v; }
    public Date getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Date v) { this.generatedAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Long getAuthorUserId() { return authorUserId; }
    public void setAuthorUserId(Long v) { this.authorUserId = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("manualproductId", manualproductId)
            .append("manualproductNo", manualproductNo)
            .append("title", title)
            .append("productVersion", productVersion)
            .append("status", status)
            .toString();
    }
}
