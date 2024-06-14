package com.yundepot.event.queue.producer;

import com.yundepot.event.queue.CapacityException;
import com.yundepot.event.queue.RingBuffer;
import com.yundepot.event.queue.Sequence;
import com.yundepot.event.queue.util.SequenceUtil;

import java.util.concurrent.locks.LockSupport;

/**
 * @author zhaiyanan
 * @date 2024/6/13  17:19
 */
public class SingleProducer<T> extends AbstractProducer<T> {

    // 生产者已发布序列号
    protected final Sequence publishedSequence = new Sequence(Sequence.INITIAL_VALUE);

    // 消费者最小的序号缓存
    private long cachedValue = Sequence.INITIAL_VALUE;

    public SingleProducer(RingBuffer ringBuffer) {
        super(ringBuffer);
    }

    @Override
    public long next() {
        return next(1);
    }

    @Override
    public long next(int n) {
        if (n < 1 || n > getBufferSize()) {
            throw new IllegalArgumentException("n must be > 0 and < bufferSize");
        }

        // 当前序号的值
        long current = producerSequence.get();
        // 新的序号的值
        long next = current + n;

        // 如果没有可用的空间等待
        while (!hasAvailableCapacity(next, current)) {
            // 这里让生产者等待1纳秒，后续有可能使用等待策略
            LockSupport.parkNanos(1L);
        }
        producerSequence.set(next);
        // 返回新的序号值
        return next;
    }

    @Override
    public long tryNext() throws Exception {
        return tryNext(1);
    }

    @Override
    public long tryNext(int n) throws Exception {
        if (n < 1) {
            throw new IllegalArgumentException("n must be > 0");
        }
        long current = producerSequence.get();
        if (!hasAvailableCapacity(current + n, current)) {
            throw new CapacityException();
        }
        return publishedSequence.addAndGet(n);
    }

    private boolean hasAvailableCapacity(long next, long current) {
        long wrapPoint = next - ringBuffer.getBufferSize();
        long cachedGatingSequence = this.cachedValue;
        if (wrapPoint > cachedGatingSequence) {
            long minSequence = SequenceUtil.getMinSequence(consumerSequences, current);
            this.cachedValue = minSequence;
            if (wrapPoint > minSequence) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void publish(long sequence) {
        //修改生产者已发布序列号，消费者就可以进行消费
        publishedSequence.set(sequence);
        // 根据不同的等待策略唤醒消费线程
        waitStrategy.signalAllWhenBlocking();
    }

    @Override
    public void publish(long lo, long hi) {
        publish(hi);
    }

    @Override
    public boolean canConsume(long sequence) {
        long currentSequence = publishedSequence.get();
        return sequence <= currentSequence && sequence > currentSequence - ringBuffer.getBufferSize();
    }

    @Override
    public long getHighestPublishedSequence(long lo, long hi) {
        return hi;
    }
}
