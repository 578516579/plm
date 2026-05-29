package cn.com.bosssfot.dv.plm.submission.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.submission.domain.Submission;

public interface ISubmissionService {
    List<Submission> selectSubmissionList(Submission submission);
    Submission selectSubmissionById(Long submissionId);
    int insertSubmission(Submission submission);
    int updateSubmission(Submission submission);
    int deleteSubmissionByIds(Long[] submissionIds);

    /**
     * Proposal 0028 P0-2 — 提测拉起测试方案(主线贯通:研发 → 测试)
     * 把 testplanId 写到现有 submission,复用 updateSubmission 里 P0-1 已加的
     * {@code validateTestplanFk} 同 projectId 强约束(目标存在 + 同 projectId,否则抛 702)。
     *
     * @param submissionId 提测 ID(不存在 → 404)
     * @param testplanId   关联的测试方案 ID(必须存在 + 同 projectId)
     */
    void attachTestplan(Long submissionId, Long testplanId);
}
