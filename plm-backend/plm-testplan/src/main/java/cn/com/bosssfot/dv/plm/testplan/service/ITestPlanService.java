package cn.com.bosssfot.dv.plm.testplan.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.testplan.domain.TestPlan;

public interface ITestPlanService {
    List<TestPlan> selectTestPlanList(TestPlan testplan);
    TestPlan selectTestPlanById(Long testplanId);
    int insertTestPlan(TestPlan testplan);
    int updateTestPlan(TestPlan testplan);
    int deleteTestPlanByIds(Long[] testplanIds);

    /** AI 生成测试策略 + 范围 + 推荐工具 + 资源分配 + 风险评估 (PRD §F4.1 - test-plan-flow);本期 mock */
    TestPlan aiGenerate(Long testplanId);
}
