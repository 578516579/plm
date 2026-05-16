package cn.com.bosssfot.dv.plm.autotest.service;

import cn.com.bosssfot.dv.plm.autotest.domain.Autotest;
import java.util.List;

public interface IAutotestService {
    List<Autotest> selectAutotestList(Autotest autotest);
    Autotest selectAutotestById(Long autotestId);
    int insertAutotest(Autotest autotest);
    int updateAutotest(Autotest autotest);
    int deleteAutotestByIds(Long[] autotestIds);
    Autotest aiScript(Long autotestId);
}
