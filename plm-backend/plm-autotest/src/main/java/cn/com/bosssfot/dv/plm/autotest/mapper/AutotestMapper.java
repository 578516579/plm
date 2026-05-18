package cn.com.bosssfot.dv.plm.autotest.mapper;

import java.util.List;
import cn.com.bosssfot.dv.plm.autotest.domain.Autotest;

public interface AutotestMapper {
    List<Autotest> selectAutotestList(Autotest autotest);
    Autotest selectAutotestById(Long autotestId);
    int insertAutotest(Autotest autotest);
    int updateAutotest(Autotest autotest);
    int deleteAutotestByIds(Long[] autotestIds);

    /** ADR: 查"以 prefix 开头的 autotest_no 中"最大流水号；无则 null */
    Integer selectMaxSeqOfYear(String prefix);
}
