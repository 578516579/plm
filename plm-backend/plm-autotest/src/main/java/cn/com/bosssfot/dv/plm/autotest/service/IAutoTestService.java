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

    /** PRD §F4.5 立即执行 + AI 根因分析 — mock 执行 + 失败时自动调 LLM 出根因 Top N */
    AutoTest runAutoTest(Long autotestId);
}
