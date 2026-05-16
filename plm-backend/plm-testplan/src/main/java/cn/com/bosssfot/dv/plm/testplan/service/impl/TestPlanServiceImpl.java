package cn.com.bosssfot.dv.plm.testplan.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.com.bosssfot.dv.plm.common.utils.SecurityUtils;
import cn.com.bosssfot.dv.plm.testplan.domain.TestPlan;
import cn.com.bosssfot.dv.plm.testplan.mapper.TestPlanMapper;
import cn.com.bosssfot.dv.plm.testplan.service.ITestPlanService;

/** 测试方案 Service - 脚手架,需补业务规则 (状态机 / FK 校验 / 编号生成) */
@Service
public class TestPlanServiceImpl implements ITestPlanService {

    @Autowired private TestPlanMapper testplanMapper;

    @Override public List<TestPlan> selectTestPlanList(TestPlan t) { return testplanMapper.selectTestPlanList(t); }
    @Override public TestPlan selectTestPlanById(Long id) { return testplanMapper.selectTestPlanById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTestPlan(TestPlan t) {
        // TODO: 加字段校验 + FK 校验 + 编号生成 + 状态约束
        t.setCreateBy(SecurityUtils.getUsername());
        return testplanMapper.insertTestPlan(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTestPlan(TestPlan t) {
        // TODO: 加状态机 + FK re-check
        t.setUpdateBy(SecurityUtils.getUsername());
        return testplanMapper.updateTestPlan(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTestPlanByIds(Long[] ids) {
        return testplanMapper.deleteTestPlanByIds(ids);
    }
}
