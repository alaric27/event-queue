package com.yundepot.event.queue.consumer;

import com.yundepot.event.queue.common.Sequence;

/**
 * @author zhaiyanan
 * @date 2024/6/17  15:37
 */
public class BlockingWaitStrategy implements WaitStrategy {

    private final Object mutex = new Object();

    @Override
    public long waitFor(long sequence, Sequence cursorSequence, Sequence dependentSequence) throws Exception {
        long availableSequence;
        // 如果还没生产到该个sequence，则等待
        if (cursorSequence.get() < sequence) {
            synchronized (mutex) {
                while (cursorSequence.get() < sequence) {
                    mutex.wait();
                }
            }
        }

        //
        while ((availableSequence = dependentSequence.get()) < sequence) {
            Thread.onSpinWait();
        }
        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking() {
        synchronized (mutex) {
            mutex.notifyAll();
        }
    }
}
