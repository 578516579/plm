package cn.com.bosssfot.dv.plm.common.core.event;

/** plm-requirement 模块发出的需求变更事件 */
public class RequirementChangedEvent extends EntityChangedEvent {
    public RequirementChangedEvent(Long requirementId, Action action) { super(requirementId, action); }
}
