package com.yundepot.event.queue.common;

//import jdk.internal.vm.annotation.Contended;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * 用于追踪生产/消费进度。
 * 可由AtomicLong代替
 * @author zhaiyanan
 * @date 2024/6/12  15:50
 */
public class Sequence {
    // 消除伪共享
//    @Contended
    private long value;

    public static final long INITIAL_VALUE = -1L;
    private static final VarHandle VALUE_FIELD;

    static {
        try {
            VALUE_FIELD = MethodHandles.lookup().findVarHandle(Sequence.class, "value", long.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Sequence() {
        this(INITIAL_VALUE);
    }

    public Sequence(long initialValue) {
        this.value = initialValue;
    }

    public long get() {
        long value = this.value;
        VarHandle.acquireFence();
        return value;
    }

    public void set(final long value) {
        VarHandle.releaseFence();
        this.value = value;
        VarHandle.fullFence();
    }

    public boolean compareAndSet(final long expectedValue, final long newValue) {
        return VALUE_FIELD.compareAndSet(this, expectedValue, newValue);
    }

    public long incrementAndGet() {
        return addAndGet(1);
    }

    public long addAndGet(final long increment) {
        return (long) VALUE_FIELD.getAndAdd(this, increment) + increment;
    }

    public long getAndAdd(final long increment) {
        return (long) VALUE_FIELD.getAndAdd(this, increment);
    }
}
