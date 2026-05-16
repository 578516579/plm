package cn.com.bosssfot.dv.plm.release.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/** 发布管理对象 tb_release (生成器脚手架,需补字段) */
public class Release extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long releaseId;
    @Excel(name = "编号") private String releaseNo;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "标题") private String title;
    @Excel(name = "状态") private String status;
    private String delFlag;

    public Long getReleaseId() { return releaseId; }
    public void setReleaseId(Long v) { this.releaseId = v; }
    public String getReleaseNo() { return releaseNo; }
    public void setReleaseNo(String v) { this.releaseNo = v; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long v) { this.projectId = v; }
    public String getTitle() { return title; }
    public void setTitle(String v) { this.title = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String v) { this.delFlag = v; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("releaseId", releaseId)
            .append("releaseNo", releaseNo)
            .append("projectId", projectId)
            .append("title", title)
            .append("status", status)
            .toString();
    }
}
