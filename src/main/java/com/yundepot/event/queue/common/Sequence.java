package com.yundepot.event.queue.common;

//import jdk.internal.vm.annotation.Contended;
import com.yundepot.event.queue.util.UnsafeUtil;
import sun.misc.Unsafe;

/**
 * 用于追踪RingBuffer和EventProcessor的进度，表示生产/消费进度。
 * 可由AtomicLong代替
 * @author zhaiyanan
 * @date 2024/6/12  15:50
 */
public class Sequence {
    // 消除伪共享
//    @Contended
    private volatile long value;

    public static final long INITIAL_VALUE = -1L;
    private static final Unsafe UNSAFE;
    private static final long VALUE_OFFSET;

    static {
        UNSAFE = UnsafeUtil.getUnsafe();
        try {
            VALUE_OFFSET = UNSAFE.objectFieldOffset(Sequence.class.getDeclaredField("value"));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Sequence() {
        this(INITIAL_VALUE);
    }

    public Sequence(long initialValue) {
        UNSAFE.putOrderedLong(this, VALUE_OFFSET, initialValue);
    }

    public long get() {
        return value;
    }

    public void set(final long value) {
        UNSAFE.putLongVolatile(this, VALUE_OFFSET, value);
    }

    public boolean compareAndSet(final long expectedValue, final long newValue) {
        return UNSAFE.compareAndSwapLong(this, VALUE_OFFSET, expectedValue, newValue);
    }

    public long incrementAndGet() {
        return addAndGet(1);
    }

    public long addAndGet(final long increment) {
        long currentValue;
        long newValue;
        do {
            currentValue = get();
            newValue = currentValue + increment;
        } while (!compareAndSet(currentValue, newValue));
        return newValue;
    }

    public long getAndAdd(final long increment) {
        long currentValue;
        long newValue;
        do {
            currentValue = get();
            newValue = currentValue + increment;
        } while (!compareAndSet(currentValue, newValue));
        return currentValue;
    }
}
