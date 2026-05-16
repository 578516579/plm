package cn.com.bosssfot.dv.plm.submission.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.submission.domain.Submission;

public interface SubmissionMapper {
    List<Submission> selectSubmissionList(Submission submission);
    Submission selectSubmissionById(Long submissionId);
    int insertSubmission(Submission submission);
    int updateSubmission(Submission submission);
    int deleteSubmissionByIds(Long[] submissionIds);

    /** ADR: 查"以 prefix 开头的 submission_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
