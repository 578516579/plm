package cn.com.bosssfot.dv.plm.apidoc.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/** API 文档对象 tb_apidoc (生成器脚手架,需补字段) */
public class ApiDoc extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long apidocId;
    @Excel(name = "编号") private String apidocNo;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "标题") private String title;
    @Excel(name = "状态") private String status;
    private String delFlag;

    public Long getApidocId() { return apidocId; }
    public void setApidocId(Long v) { this.apidocId = v; }
    public String getApidocNo() { return apidocNo; }
    public void setApidocNo(String v) { this.apidocNo = v; }
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
            .append("apidocId", apidocId)
            .append("apidocNo", apidocNo)
            .append("projectId", projectId)
            .append("title", title)
            .append("status", status)
            .toString();
    }
}
