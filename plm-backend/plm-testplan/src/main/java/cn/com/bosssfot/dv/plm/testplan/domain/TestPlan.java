package cn.com.bosssfot.dv.plm.testplan.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/** 测试方案对象 tb_testplan (生成器脚手架,需补字段) */
public class TestPlan extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long testplanId;
    @Excel(name = "编号") private String testplanNo;
    @Excel(name = "项目ID") private Long projectId;
    @Excel(name = "标题") private String title;
    @Excel(name = "状态") private String status;
    private String delFlag;

    public Long getTestplanId() { return testplanId; }
    public void setTestplanId(Long v) { this.testplanId = v; }
    public String getTestplanNo() { return testplanNo; }
    public void setTestplanNo(String v) { this.testplanNo = v; }
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
            .append("testplanId", testplanId)
            .append("testplanNo", testplanNo)
            .append("projectId", projectId)
            .append("title", title)
            .append("status", status)
            .toString();
    }
}
