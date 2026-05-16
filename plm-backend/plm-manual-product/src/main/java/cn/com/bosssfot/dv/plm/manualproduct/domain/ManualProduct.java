package cn.com.bosssfot.dv.plm.manualproduct.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/** 产品手册对象 tb_manual_product (生成器脚手架,需补字段) */
public class ManualProduct extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long manualproductId;
    @Excel(name = "编号") private String manualproductNo;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "标题") private String title;
    @Excel(name = "状态") private String status;
    private String delFlag;

    public Long getManualproductId() { return manualproductId; }
    public void setManualproductId(Long v) { this.manualproductId = v; }
    public String getManualproductNo() { return manualproductNo; }
    public void setManualproductNo(String v) { this.manualproductNo = v; }
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
            .append("manualproductId", manualproductId)
            .append("manualproductNo", manualproductNo)
            .append("projectId", projectId)
            .append("title", title)
            .append("status", status)
            .toString();
    }
}
