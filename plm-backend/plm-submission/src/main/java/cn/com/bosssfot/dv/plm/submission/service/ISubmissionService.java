package cn.com.bosssfot.dv.plm.submission.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.submission.domain.Submission;

public interface ISubmissionService {
    List<Submission> selectSubmissionList(Submission submission);
    Submission selectSubmissionById(Long submissionId);
    int insertSubmission(Submission submission);
    int updateSubmission(Submission submission);
    int deleteSubmissionByIds(Long[] submissionIds);
}
