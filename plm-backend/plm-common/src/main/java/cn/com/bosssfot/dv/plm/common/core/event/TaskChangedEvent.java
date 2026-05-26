package cn.com.bosssfot.dv.plm.common.core.event;

/** plm-task 模块发出的任务变更事件 */
public class TaskChangedEvent extends EntityChangedEvent {
    public TaskChangedEvent(Long taskId, Action action) { super(taskId, action); }
}
