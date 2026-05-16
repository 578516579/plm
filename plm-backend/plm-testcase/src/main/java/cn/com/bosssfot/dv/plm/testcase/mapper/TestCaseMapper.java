package cn.com.bosssfot.dv.plm.testcase.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.testcase.domain.TestCase;

public interface TestCaseMapper {
    List<TestCase> selectTestCaseList(TestCase testcase);
    TestCase selectTestCaseById(Long testcaseId);
    int insertTestCase(TestCase testcase);
    int updateTestCase(TestCase testcase);
    int deleteTestCaseByIds(Long[] testcaseIds);
    /** ADR-0006: TC-YYYY-NNNN 最大流水号 */
    Integer selectMaxSeqOfYear(String prefix);
}
