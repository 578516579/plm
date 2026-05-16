package cn.com.bosssfot.dv.plm.testplan.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.testplan.domain.TestPlan;

public interface TestPlanMapper {
    List<TestPlan> selectTestPlanList(TestPlan testplan);
    TestPlan selectTestPlanById(Long testplanId);
    int insertTestPlan(TestPlan testplan);
    int updateTestPlan(TestPlan testplan);
    int deleteTestPlanByIds(Long[] testplanIds);
}
