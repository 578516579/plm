package cn.com.bosssfot.dv.plm.testdata.mapper;

import cn.com.bosssfot.dv.plm.testdata.domain.Testdata;
import java.util.List;

public interface TestdataMapper {
    List<Testdata> selectTestdataList(Testdata testdata);
    Testdata selectTestdataById(Long testdataId);
    int insertTestdata(Testdata testdata);
    int updateTestdata(Testdata testdata);
    int deleteTestdataByIds(Long[] testdataIds);
    Integer selectMaxSeqOfYear(String prefix);
}
