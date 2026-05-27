package cn.com.bosssfot.dv.plm.common.core.event;

/**
 * 业务实体变更事件基类。
 *
 * <p>4 个业务模块(plm-defect / plm-requirement / plm-task / plm-testcase)的
 * ServiceImpl 在 insert/update/delete 成功后 publishEvent 各自的子类型,
 * plm-integration 模块下的 OutboundSyncService 用
 * {@code @TransactionalEventListener(phase=AFTER_COMMIT)} 监听,做反向推送。
 *
 * <p>事件 payload 故意只携带 {@code entityId + action},不携带完整实体,
 * 避免:
 * <ul>
 *   <li>实体类序列化耦合</li>
 *   <li>事件触发与实际数据库状态不一致(尤其涉及 update_time 的冲突合并)</li>
 * </ul>
 * 监听端按需自己 SELECT 重读最新行。
 */
public abstract class EntityChangedEvent {

    public enum Action { INSERT, UPDATE, DELETE }

    private final Long entityId;
    private final Action action;

    protected EntityChangedEvent(Long entityId, Action action) {
        this.entityId = entityId;
        this.action = action;
    }

    public Long getEntityId() { return entityId; }
    public Action getAction()  { return action;  }
}
