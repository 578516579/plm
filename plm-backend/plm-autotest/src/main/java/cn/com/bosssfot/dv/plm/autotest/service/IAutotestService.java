package cn.com.bosssfot.dv.plm.autotest.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.autotest.domain.Autotest;

public interface IAutotestService {
    List<Autotest> selectAutotestList(Autotest autotest);
    Autotest selectAutotestById(Long autotestId);
    int insertAutotest(Autotest autotest);
    int updateAutotest(Autotest autotest);
    int deleteAutotestByIds(Long[] autotestIds);

    /** AI 生成自动化测试脚本 (PRD §F4.5 - autotest-gen-flow);本期 mock */
    Autotest generate(Long autotestId);
}
