package cn.com.bosssfot.dv.plm.requirement.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.requirement.domain.RequirementReview;

/**
 * 需求评审记录 Mapper 接口
 *
 * 关联：PRD §F2.4 需求评审管理
 */
public interface RequirementReviewMapper
{
    /** 查询评审记录列表（支持按 requirementId / reviewerUserId / reviewResult 过滤） */
    public List<RequirementReview> selectRequirementReviewList(RequirementReview review);

    /** 根据 ID 查询单条评审 */
    public RequirementReview selectRequirementReviewById(Long reviewId);

    /** 查询某需求的全部评审历史（按时间倒序） */
    public List<RequirementReview> selectByRequirementId(Long requirementId);

    /** 计数：某需求"通过"评审记录数（状态机 00→01 前置） */
    public int countPassedReviewsByRequirementId(Long requirementId);

    /** 新增评审 */
    public int insertRequirementReview(RequirementReview review);

    /** 修改评审（仅支持修改 remark / reviewComment，状态/时间不可改） */
    public int updateRequirementReview(RequirementReview review);

    /** 批量逻辑删除（撤回评审） */
    public int deleteRequirementReviewByIds(Long[] reviewIds);
}
