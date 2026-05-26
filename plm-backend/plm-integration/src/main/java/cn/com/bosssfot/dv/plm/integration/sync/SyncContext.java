package cn.com.bosssfot.dv.plm.integration.sync;

/**
 * 同步上下文(ThreadLocal)— 用于双向同步防循环。
 *
 * <p>语义:
 * <ul>
 *   <li>{@code inbound=true} 表示当前线程正在做 <b>入站同步</b>(外部 webhook → PLM),
 *       业务模块的 ApplicationEvent 应被 {@code OutboundSyncService} 忽略,
 *       避免"外部改动 → PLM 写 → ApplicationEvent → 反推外部"无限循环</li>
 * </ul>
 *
 * <p>使用范式:
 * <pre>
 * try {
 *     SyncContext.setInbound(true);
 *     defectMapper.update(d);     // 触发 ApplicationEvent,但被忽略
 * } finally {
 *     SyncContext.clear();
 * }
 * </pre>
 *
 * @see <a href="../../../99-跨阶段/proposals/0014-zentao-bidirectional-sync.md">Proposal 0014</a>
 */
public final class SyncContext {

    private static final ThreadLocal<Boolean> INBOUND = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private SyncContext() {}

    public static void setInbound(boolean v) {
        INBOUND.set(v);
    }

    public static boolean isInbound() {
        return INBOUND.get();
    }

    public static void clear() {
        INBOUND.remove();
    }
}
