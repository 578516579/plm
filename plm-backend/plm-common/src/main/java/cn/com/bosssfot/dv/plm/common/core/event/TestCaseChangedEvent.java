package cn.com.bosssfot.dv.plm.common.core.event;

/** plm-testcase 模块发出的测试用例变更事件 */
public class TestCaseChangedEvent extends EntityChangedEvent {
    public TestCaseChangedEvent(Long testCaseId, Action action) { super(testCaseId, action); }
}
