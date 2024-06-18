package com.yundepot.event.queue.consumer.waitstrategy;

import com.yundepot.event.queue.common.Sequence;
import com.yundepot.event.queue.util.ThreadHints;

/**
 * 阻塞等待策略
 * 如果没有可用事件，它将阻塞直到生产者添加新事件
 * 适用于对延迟不敏感场景， 由于其阻塞性质，它在高并发环境下效率较低
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

        // 等待其他消费者
        while ((availableSequence = dependentSequence.get()) < sequence) {
            ThreadHints.onSpinWait();
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
