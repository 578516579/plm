package cn.com.bosssfot.dv.plm.testplan.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.testplan.domain.TestPlan;

public interface TestPlanMapper {
    List<TestPlan> selectTestPlanList(TestPlan testplan);
    TestPlan selectTestPlanById(Long testplanId);
    int insertTestPlan(TestPlan testplan);
    int updateTestPlan(TestPlan testplan);
    int deleteTestPlanByIds(Long[] testplanIds);

    /** ADR: 查"以 prefix 开头的 testplan_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
