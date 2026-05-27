package cn.com.bosssfot.dv.plm.requirement.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.requirement.domain.RequirementReview;

/**
 * 需求评审记录 Service 接口
 *
 * 关联：PRD §F2.4 需求评审管理
 */
public interface IRequirementReviewService
{
    /** 查询评审记录列表 */
    public List<RequirementReview> selectRequirementReviewList(RequirementReview review);

    /** 查询单条评审 */
    public RequirementReview selectRequirementReviewById(Long reviewId);

    /** 查询某需求全部评审历史 */
    public List<RequirementReview> selectByRequirementId(Long requirementId);

    /**
     * 提交评审 — 给某需求增加 1 条评审记录。
     * <ul>
     *   <li>reviewResult 必填(00=通过 / 01=打回)</li>
     *   <li>打回(01)时 reviewComment 必填</li>
     *   <li>需求必须存在(704)</li>
     * </ul>
     */
    public int submitReview(Long requirementId, RequirementReview review);

    /** 撤回评审（逻辑删除） */
    public int deleteRequirementReviewByIds(Long[] reviewIds);

    /**
     * 校验：某需求是否存在"通过"评审 (状态机 00→01 前置)。
     * @return true=有通过评审 false=无
     */
    public boolean hasPassedReview(Long requirementId);
}
