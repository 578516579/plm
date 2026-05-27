package cn.com.bosssfot.dv.plm.common.core.event;

/** plm-defect 模块发出的缺陷变更事件 */
public class DefectChangedEvent extends EntityChangedEvent {
    public DefectChangedEvent(Long defectId, Action action) { super(defectId, action); }
}
