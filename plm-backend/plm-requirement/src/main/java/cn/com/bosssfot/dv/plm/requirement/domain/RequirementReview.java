package cn.com.bosssfot.dv.plm.requirement.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import cn.com.bosssfot.dv.plm.common.annotation.Excel;
import cn.com.bosssfot.dv.plm.common.core.domain.BaseEntity;

/**
 * 需求评审记录 tb_requirement_review
 *
 * 关联：
 * - PRD §F2.4 需求评审管理
 * - PRD-MAPPING.md §2 tb_requirement_review (2026-05-25 新增)
 * - 状态机 00→01 前置依赖：必须存在 review_result=00 通过的有效评审
 */
public class RequirementReview extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 评审 ID */
    private Long reviewId;

    /** 需求 ID（FK→tb_requirement.requirement_id） */
    @Excel(name = "需求ID")
    private Long requirementId;

    /** 评审人（FK→sys_user.user_id） */
    @Excel(name = "评审人")
    private Long reviewerUserId;

    /** 评审结果（字典 biz_req_review_result：00=通过 01=打回） */
    @Excel(name = "评审结果", dictType = "biz_req_review_result")
    private String reviewResult;

    /** 评审意见正文（打回必填） */
    private String reviewComment;

    /** 评审时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "评审时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date reviewAt;

    /** 删除标志（0=正常, 2=删除） */
    private String delFlag;

    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
    public Long getReviewId() { return reviewId; }

    public void setRequirementId(Long requirementId) { this.requirementId = requirementId; }
    public Long getRequirementId() { return requirementId; }

    public void setReviewerUserId(Long reviewerUserId) { this.reviewerUserId = reviewerUserId; }
    public Long getReviewerUserId() { return reviewerUserId; }

    public void setReviewResult(String reviewResult) { this.reviewResult = reviewResult; }
    public String getReviewResult() { return reviewResult; }

    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public String getReviewComment() { return reviewComment; }

    public void setReviewAt(Date reviewAt) { this.reviewAt = reviewAt; }
    public Date getReviewAt() { return reviewAt; }

    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("reviewId", getReviewId())
            .append("requirementId", getRequirementId())
            .append("reviewerUserId", getReviewerUserId())
            .append("reviewResult", getReviewResult())
            .append("reviewComment", getReviewComment())
            .append("reviewAt", getReviewAt())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
