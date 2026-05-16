package cn.com.bosssfot.dv.plm.autotest.service;

import java.util.List;
import cn.com.bosssfot.dv.plm.autotest.domain.AutoTest;

public interface IAutoTestService {
    List<AutoTest> selectAutoTestList(AutoTest autotest);
    AutoTest selectAutoTestById(Long autotestId);
    int insertAutoTest(AutoTest autotest);
    int updateAutoTest(AutoTest autotest);
    int deleteAutoTestByIds(Long[] autotestIds);

    /** PRD §F4.5 AI 生成测试脚本 — auto-test-flow Dify 占位 */
    AutoTest aiGenerate(Long autotestId);
}
