package cn.com.bosssfot.dv.plm.integration.sync;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 {@link SyncContext} ThreadLocal 行为 — 关键于防循环正确性。
 */
class SyncContextTest {

    @AfterEach
    void cleanup() {
        SyncContext.clear();
    }

    @Test
    void defaultIsFalse() {
        assertFalse(SyncContext.isInbound());
    }

    @Test
    void setAndRead() {
        SyncContext.setInbound(true);
        assertTrue(SyncContext.isInbound());
        SyncContext.setInbound(false);
        assertFalse(SyncContext.isInbound());
    }

    @Test
    void clearRestoresDefault() {
        SyncContext.setInbound(true);
        SyncContext.clear();
        assertFalse(SyncContext.isInbound());
    }

    @Test
    void threadLocal_isolatedBetweenThreads() throws Exception {
        SyncContext.setInbound(true);
        AtomicBoolean otherThreadSawTrue = new AtomicBoolean(true);
        CountDownLatch latch = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            otherThreadSawTrue.set(SyncContext.isInbound());
            latch.countDown();
        });
        t.start();
        latch.await();
        // 另一个线程默认应该是 false(ThreadLocal 隔离)
        assertFalse(otherThreadSawTrue.get(),
            "SyncContext should NOT leak across threads —防循环正确性关键");
        // 主线程仍然是 true
        assertTrue(SyncContext.isInbound());
    }
}
