package cn.com.bosssfot.dv.plm.testreport.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.testreport.domain.TestReport;

public interface TestReportMapper {
    List<TestReport> selectTestReportList(TestReport testreport);
    TestReport selectTestReportById(Long testreportId);
    int insertTestReport(TestReport testreport);
    int updateTestReport(TestReport testreport);
    int deleteTestReportByIds(Long[] testreportIds);
}
