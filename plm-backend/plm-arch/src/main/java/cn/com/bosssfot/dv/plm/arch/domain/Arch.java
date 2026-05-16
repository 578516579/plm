package cn.com.bosssfot.dv.plm.arch.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 架构设计 — PRD §F3.1
 */
public class Arch extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long archId;

    @Excel(name = "架构编号")
    private String archNo;

    @Excel(name = "项目ID")
    private Long projectId;

    @Excel(name = "架构方案标题")
    private String title;

    @Excel(name = "架构模式")
    private String archMode;

    @Excel(name = "技术语言栈")
    private String techStack;

    @Excel(name = "数据库方案")
    private String dbStack;

    @Excel(name = "AI编排方案")
    private String aiOrchestration;

    @Excel(name = "部署模式")
    private String deployMode;

    @Excel(name = "IoT协议")
    private String iotProtocol;

    private String archContent;

    private String c4Diagram;

    private String nfrContent;

    private String reviewReport;

    @Excel(name = "AI生成")
    private String aiGenerated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date aiGeneratedAt;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "作者用户ID")
    private Long authorUserId;

    private Long reviewerUserId;

    public Long getArchId() { return archId; }
    public void setArchId(Long archId) { this.archId = archId; }

    public String getArchNo() { return archNo; }
    public void setArchNo(String archNo) { this.archNo = archNo; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArchMode() { return archMode; }
    public void setArchMode(String archMode) { this.archMode = archMode; }

    public String getTechStack() { return techStack; }
    public void setTechStack(String techStack) { this.techStack = techStack; }

    public String getDbStack() { return dbStack; }
    public void setDbStack(String dbStack) { this.dbStack = dbStack; }

    public String getAiOrchestration() { return aiOrchestration; }
    public void setAiOrchestration(String aiOrchestration) { this.aiOrchestration = aiOrchestration; }

    public String getDeployMode() { return deployMode; }
    public void setDeployMode(String deployMode) { this.deployMode = deployMode; }

    public String getIotProtocol() { return iotProtocol; }
    public void setIotProtocol(String iotProtocol) { this.iotProtocol = iotProtocol; }

    public String getArchContent() { return archContent; }
    public void setArchContent(String archContent) { this.archContent = archContent; }

    public String getC4Diagram() { return c4Diagram; }
    public void setC4Diagram(String c4Diagram) { this.c4Diagram = c4Diagram; }

    public String getNfrContent() { return nfrContent; }
    public void setNfrContent(String nfrContent) { this.nfrContent = nfrContent; }

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
