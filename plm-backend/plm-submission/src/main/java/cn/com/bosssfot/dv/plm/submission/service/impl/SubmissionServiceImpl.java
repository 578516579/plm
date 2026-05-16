package cn.com.bosssfot.dv.plm.submission.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.submission.domain.Submission;
import cn.com.bosssfot.dv.plm.submission.mapper.SubmissionMapper;
import cn.com.bosssfot.dv.plm.submission.service.ISubmissionService;

/** 提测管理 Service - 脚手架,需补业务规则 (状态机 / FK 校验 / 编号生成) */
@Service
public class SubmissionServiceImpl implements ISubmissionService {

    @Autowired private SubmissionMapper submissionMapper;

    @Override public List<Submission> selectSubmissionList(Submission t) { return submissionMapper.selectSubmissionList(t); }
    @Override public Submission selectSubmissionById(Long id) { return submissionMapper.selectSubmissionById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertSubmission(Submission t) {
        // TODO: 加字段校验 + FK 校验 + 编号生成 + 状态约束
        t.setCreateBy(SecurityUtils.getUsername());
        return submissionMapper.insertSubmission(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSubmission(Submission t) {
        // TODO: 加状态机 + FK re-check
        t.setUpdateBy(SecurityUtils.getUsername());
        return submissionMapper.updateSubmission(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSubmissionByIds(Long[] ids) {
        return submissionMapper.deleteSubmissionByIds(ids);
    }
}
