package cn.com.bosssfot.dv.plm.testreport.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.testreport.domain.TestReport;

public interface ITestReportService {
    List<TestReport> selectTestReportList(TestReport testreport);
    TestReport selectTestReportById(Long testreportId);
    int insertTestReport(TestReport testreport);
    int updateTestReport(TestReport testreport);
    int deleteTestReportByIds(Long[] testreportIds);
}
