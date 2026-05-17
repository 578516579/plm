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

    // 4 个 NFR 子项 (原型右栏 NFR 卡片 1:1) — 2026-05-17 drift 修复
    /** NFR 性能 (原型: API P99<200ms, IoT 并发 10万设备) */
    private String nfrPerformance;
    /** NFR 可用性 (原型: SLA 99.9%) */
    private String nfrAvailability;
    /** NFR 安全 (原型: TLS 1.3, 数据加密) */
    private String nfrSecurity;
    /** NFR 扩展性 (原型: 支持 5 年 10 倍增长) */
    private String nfrScalability;

    /**
     * AI 设计 timeline 4 步骤 JSON (原型 genArchDesign 的 4 个 timeline items)
     * 格式: [{step:1, name:'架构模式', status:'pass', description}, ...]
     */
    private String aiTimelineJson;

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

    public String getNfrPerformance() { return nfrPerformance; }
    public void setNfrPerformance(String v) { this.nfrPerformance = v; }
    public String getNfrAvailability() { return nfrAvailability; }
    public void setNfrAvailability(String v) { this.nfrAvailability = v; }
    public String getNfrSecurity() { return nfrSecurity; }
    public void setNfrSecurity(String v) { this.nfrSecurity = v; }
    public String getNfrScalability() { return nfrScalability; }
    public void setNfrScalability(String v) { this.nfrScalability = v; }
    public String getAiTimelineJson() { return aiTimelineJson; }
    public void setAiTimelineJson(String v) { this.aiTimelineJson = v; }

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
