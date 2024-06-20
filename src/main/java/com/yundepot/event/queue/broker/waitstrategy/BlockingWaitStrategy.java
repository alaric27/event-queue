package com.yundepot.event.queue.broker.waitstrategy;

import com.yundepot.event.queue.common.Sequence;
import com.yundepot.event.queue.util.ThreadHints;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 阻塞等待策略
 * 如果没有可用事件，它将阻塞直到生产者添加新事件
 * 适用于对延迟不敏感场景， 由于其阻塞性质，它在高并发环境下效率较低
 * @author zhaiyanan
 * @date 2024/6/17  15:37
 */
public class BlockingWaitStrategy implements WaitStrategy {

    private final Lock lock = new ReentrantLock();
    private final Condition processorNotifyCondition = lock.newCondition();

    @Override
    public long waitFor(long sequence, Sequence cursorSequence, Sequence dependentSequence) throws Exception {
        long availableSequence;
        if (cursorSequence.get() < sequence) {
            lock.lock();
            try {
                while (cursorSequence.get() < sequence) {
                    processorNotifyCondition.await();
                }
            } finally {
                lock.unlock();
            }
        }

        while ((availableSequence = dependentSequence.get()) < sequence) {
            ThreadHints.onSpinWait();
        }
        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking()
    {
        lock.lock();
        try {
            processorNotifyCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
