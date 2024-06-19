package com.yundepot.event.queue.broker.waitstrategy;

import com.yundepot.event.queue.common.Sequence;

import java.util.concurrent.locks.LockSupport;

/**
 * 睡眠等待策略
 * 当没有事件可消费时，该策略会让线程休眠一定时间，然后再次检查是否有新的事件
 * 适用于低CPU资源消耗的需求，因为它减少了CPU的空转。但是，这会增加事件处理的延迟
 * @author zhaiyanan
 * @date 2024/6/18  15:19
 */
public class SleepingWaitStrategy implements WaitStrategy {
    private static final int SPIN_THRESHOLD = 100;
    private static final int DEFAULT_RETRIES = 200;
    private static final long DEFAULT_SLEEP = 100;

    private final int retries;
    private final long sleepTimeNs;
    public SleepingWaitStrategy() {
        this(DEFAULT_RETRIES, DEFAULT_SLEEP);
    }

    public SleepingWaitStrategy(final int retries) {
        this(retries, DEFAULT_SLEEP);
    }

    public SleepingWaitStrategy(final int retries, final long sleepTimeNs) {
        this.retries = retries;
        this.sleepTimeNs = sleepTimeNs;
    }

    @Override
    public long waitFor(long sequence, Sequence cursorSequence, Sequence dependentSequence) throws Exception {
        long availableSequence;
        int counter = retries;

        while ((availableSequence = dependentSequence.get()) < sequence) {
            counter = applyWaitMethod(counter);
        }
        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking() {

    }

    private int applyWaitMethod(final int counter) {
        if (counter > SPIN_THRESHOLD) {
            return counter - 1;
        } else if (counter > 0) {
            Thread.yield();
            return counter - 1;
        } else {
            LockSupport.parkNanos(sleepTimeNs);
        }
        return counter;
    }
}
