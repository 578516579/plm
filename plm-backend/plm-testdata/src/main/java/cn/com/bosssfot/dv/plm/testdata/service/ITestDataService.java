package cn.com.bosssfot.dv.plm.testdata.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.testdata.domain.TestData;

public interface ITestDataService {
    List<TestData> selectTestDataList(TestData testdata);
    TestData selectTestDataById(Long testdataId);
    int insertTestData(TestData testdata);
    int updateTestData(TestData testdata);
    int deleteTestDataByIds(Long[] testdataIds);

    /** AI 生成测试数据 (PRD §F4.3 - data-gen-flow);本期 mock */
    TestData generate(Long testdataId);
}
