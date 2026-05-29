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

    /**
     * AI 生成用例要素(PRD §F3.5 / §2.3)— 基于标题 + 描述,用 AiService 生成
     * 前置条件 / 测试步骤 / 预期结果并落库。真 provider 失败抛 708;
     * mock / 解析失败时退回确定性骨架,保证 dev/CI 始终可用。
     */
    TestCase aiGenerate(Long testcaseId);
}
