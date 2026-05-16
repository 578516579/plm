package cn.com.bosssfot.dv.plm.testdata.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.testdata.domain.TestData;

public interface TestDataMapper {
    List<TestData> selectTestDataList(TestData testdata);
    TestData selectTestDataById(Long testdataId);
    int insertTestData(TestData testdata);
    int updateTestData(TestData testdata);
    int deleteTestDataByIds(Long[] testdataIds);

    /** ADR: 查"以 prefix 开头的 testdata_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
