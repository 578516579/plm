package cn.com.bosssfot.dv.plm.testreport.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.testreport.domain.TestReport;

public interface ITestReportService {
    List<TestReport> selectTestReportList(TestReport testreport);
    TestReport selectTestReportById(Long testreportId);
    int insertTestReport(TestReport testreport);
    int updateTestReport(TestReport testreport);
    int deleteTestReportByIds(Long[] testreportIds);

    /**
     * Proposal 0028 P0-3A — 按 testplanId 实时聚合 testcase + defect,产出
     * totalCases / passedCases / failedCases / p0Defects / p1Defects / coverageRate,
     * 写回 testreport 字段并设 is_aggregated='Y', aggregated_at=now()。
     *
     * 注意:本期 testcase 表无 testplan_id 字段(未来加 testplan_id 到 testcase 后改为按方案),
     *      所以按 report.projectId 维度聚合 testcase 与 defect。
     * 跳过条件:is_manual_override='Y' 时直接返回原值不动,不写库。
     *
     * @param testreportId 测试报告主键
     * @return 聚合后的 testreport (snapshot);不存在抛 ServiceException 702
     */
    TestReport aggregateFromTestplan(Long testreportId);
}
