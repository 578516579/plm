package cn.com.bosssfot.dv.plm.testreport.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.testreport.domain.TestReport;
import cn.com.bosssfot.dv.plm.testreport.mapper.TestReportMapper;
import cn.com.bosssfot.dv.plm.testreport.service.ITestReportService;

/** 测试报告 Service - 脚手架,需补业务规则 (状态机 / FK 校验 / 编号生成) */
@Service
public class TestReportServiceImpl implements ITestReportService {

    @Autowired private TestReportMapper testreportMapper;

    @Override public List<TestReport> selectTestReportList(TestReport t) { return testreportMapper.selectTestReportList(t); }
    @Override public TestReport selectTestReportById(Long id) { return testreportMapper.selectTestReportById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTestReport(TestReport t) {
        // TODO: 加字段校验 + FK 校验 + 编号生成 + 状态约束
        t.setCreateBy(SecurityUtils.getUsername());
        return testreportMapper.insertTestReport(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTestReport(TestReport t) {
        // TODO: 加状态机 + FK re-check
        t.setUpdateBy(SecurityUtils.getUsername());
        return testreportMapper.updateTestReport(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTestReportByIds(Long[] ids) {
        return testreportMapper.deleteTestReportByIds(ids);
    }
}
