package cn.com.bosssfot.dv.plm.autotest.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.autotest.domain.AutoTest;

public interface AutoTestMapper {
    List<AutoTest> selectAutoTestList(AutoTest autotest);
    AutoTest selectAutoTestById(Long autotestId);
    int insertAutoTest(AutoTest autotest);
    int updateAutoTest(AutoTest autotest);
    int deleteAutoTestByIds(Long[] autotestIds);

    Integer selectMaxSeqOfYear(String prefix);
}
