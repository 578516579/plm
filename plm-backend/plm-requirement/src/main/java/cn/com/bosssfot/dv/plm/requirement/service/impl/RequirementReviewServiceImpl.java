package cn.com.bosssfot.dv.plm.requirement.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.exception.ServiceException;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.common.utils.StringUtils;
import cn.com.bosssfot.dv.plm.requirement.domain.Requirement;
import cn.com.bosssfot.dv.plm.requirement.domain.RequirementReview;
import cn.com.bosssfot.dv.plm.requirement.mapper.RequirementMapper;
import cn.com.bosssfot.dv.plm.requirement.mapper.RequirementReviewMapper;
import cn.com.bosssfot.dv.plm.requirement.service.IRequirementReviewService;

/**
 * 需求评审记录 Service 实现
 *
 * 落地：
 * - PRD §F2.4 需求评审管理
 * - 状态机 00→01 前置 (RequirementServiceImpl 注入本 Service 调用 hasPassedReview)
 * - 错误码: 602 (参数为空) / 604 (业务规则冲突) / 704 (需求不存在)
 */
@Service
public class RequirementReviewServiceImpl implements IRequirementReviewService
{
    /** 评审结果白名单：00=通过 01=打回 */
    private static final Set<String> ALLOWED_RESULTS = Set.of("00", "01");

    @Autowired
    private RequirementReviewMapper requirementReviewMapper;

    @Autowired
    private RequirementMapper requirementMapper;

    @Override
    public List<RequirementReview> selectRequirementReviewList(RequirementReview review) {
        return requirementReviewMapper.selectRequirementReviewList(review);
    }

    @Override
    public RequirementReview selectRequirementReviewById(Long reviewId) {
        return requirementReviewMapper.selectRequirementReviewById(reviewId);
    }

    @Override
    public List<RequirementReview> selectByRequirementId(Long requirementId) {
        return requirementReviewMapper.selectByRequirementId(requirementId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitReview(Long requirementId, RequirementReview review) {
        // 参数与 FK 校验
        if (requirementId == null) {
            throw new ServiceException("需求 ID 不能为空", 602);
        }
        if (review == null) {
            throw new ServiceException("评审对象不能为空", 602);
        }
        Requirement req = requirementMapper.selectRequirementById(requirementId);
        if (req == null) {
            throw new ServiceException("关联需求不存在", 704);
        }

        if (StringUtils.isBlank(review.getReviewResult())) {
            throw new ServiceException("评审结果不能为空", 602);
        }
        if (!ALLOWED_RESULTS.contains(review.getReviewResult())) {
            throw new ServiceException(
                "评审结果非法(允许 00=通过 / 01=打回): " + review.getReviewResult(),
                604
            );
        }
        if ("01".equals(review.getReviewResult())
                && StringUtils.isBlank(review.getReviewComment())) {
            throw new ServiceException("打回评审必须填写意见", 604);
        }
        if (review.getReviewerUserId() == null) {
            // 兜底: 当前登录用户作为评审人
            Long currentUserId = SecurityUtils.getUserId();
            if (currentUserId == null) {
                throw new ServiceException("评审人不能为空", 602);
            }
            review.setReviewerUserId(currentUserId);
        }

        // 默认值
        review.setRequirementId(requirementId);
        if (review.getReviewAt() == null) {
            review.setReviewAt(new Date());
        }
        review.setCreateBy(SecurityUtils.getUsername());
        review.setDelFlag("0");

        return requirementReviewMapper.insertRequirementReview(review);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRequirementReviewByIds(Long[] reviewIds) {
        if (reviewIds == null || reviewIds.length == 0) {
            return 0;
        }
        return requirementReviewMapper.deleteRequirementReviewByIds(reviewIds);
    }

    @Override
    public boolean hasPassedReview(Long requirementId) {
        if (requirementId == null) {
            return false;
        }
        return requirementReviewMapper.countPassedReviewsByRequirementId(requirementId) > 0;
    }
}
