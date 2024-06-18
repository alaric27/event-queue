package com.yundepot.event.queue.consumer.waitstrategy;

import com.yundepot.event.queue.common.Sequence;

/**
 * 类似 SleepingWaitStrategy 它使用线程的yield方法而不是睡眠
 * 适用于多线程环境，可以稍微提高CPU的利用率，但仍然会引入较高的延迟。
 * @author zhaiyanan
 * @date 2024/6/18  15:05
 */
public class YieldingWaitStrategy implements WaitStrategy {
    private static final int SPIN_TRIES = 100;

    @Override
    public long waitFor(long sequence, Sequence cursorSequence, Sequence dependentSequence) throws Exception {
        long availableSequence;
        int counter = SPIN_TRIES;
        while ((availableSequence = dependentSequence.get()) < sequence) {
            counter = applyWaitMethod(counter);
        }
        return availableSequence;
    }

    private int applyWaitMethod(final int counter) {
        if (0 == counter) {
            Thread.yield();
        } else {
            return counter - 1;
        }
        return counter;
    }

    @Override
    public void signalAllWhenBlocking() {

    }
}
