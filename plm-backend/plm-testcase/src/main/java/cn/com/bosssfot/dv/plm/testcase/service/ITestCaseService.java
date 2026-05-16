package cn.com.bosssfot.dv.plm.testcase.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.testcase.domain.TestCase;

public interface ITestCaseService
{
    List<TestCase> selectTestCaseList(TestCase testcase);
    TestCase selectTestCaseById(Long testcaseId);
    int insertTestCase(TestCase testcase);
    int updateTestCase(TestCase testcase);
    int deleteTestCaseByIds(Long[] testcaseIds);

    /** 执行用例: 必须当前 status='02',推到 03/04 + execution_count+1 + last_executed_at=now */
    int executeTestCase(Long testcaseId, String newStatus, String actualResult);
}
