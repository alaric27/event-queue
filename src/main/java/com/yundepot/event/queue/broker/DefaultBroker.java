package com.yundepot.event.queue.broker;

import com.yundepot.event.queue.broker.waitstrategy.WaitStrategy;
import com.yundepot.event.queue.common.Sequence;
import com.yundepot.event.queue.util.SequenceUtil;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.LockSupport;

/**
 * 消费者和生产者协调者
 * @author zhaiyanan
 * @date 2024/6/17  12:54
 */
public class DefaultBroker<T> implements Broker {

    /**
     * 数据存储
     */
    private final RingBuffer<T> ringBuffer;

    /**
     * 数据存储进度
     */
    private final Sequence cursor = new Sequence(Sequence.INITIAL_VALUE);

    /**
     * 已发布序列号
     */
    private final Sequence publishedSequence = new Sequence(Sequence.INITIAL_VALUE);

    /**
     * 缓存消费进度，避免多次计算
     */
    private final Sequence consumerSequenceCache = new Sequence(Sequence.INITIAL_VALUE);


    private final WaitStrategy waitStrategy;
    private volatile Sequence[] consumerSequences = new Sequence[0];
    private static final AtomicReferenceFieldUpdater<DefaultBroker, Sequence[]> SEQUENCE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(DefaultBroker.class, Sequence[].class, "consumerSequences");

    public DefaultBroker(RingBuffer<T> ringBuffer, WaitStrategy waitStrategy) {
        this.ringBuffer = ringBuffer;
        this.waitStrategy = waitStrategy;
    }

    @Override
    public long next(int n) {
        if (n < 1 || n > ringBuffer.getBufferSize()) {
            throw new IllegalArgumentException("n must be > 0 and < bufferSize");
        }

        long nextSequence = cursor.addAndGet(n);
        while (!hasAvailableCapacity(nextSequence)) {
            LockSupport.parkNanos(1L);
        }
        return nextSequence;
    }

    private boolean hasAvailableCapacity(long next) {
        // 用于判断生产者的序号在环形数组中是否绕过了消费者最小的序号
        long wrapPoint = next - ringBuffer.getBufferSize();
        long cachedConsumerSequence = consumerSequenceCache.get();

        //  判断wrapPoint是否大于上一次计算时消费者的最小值, 如果大于则进行一次从新计算判断，否则直接后续赋值操作
        if (wrapPoint > cachedConsumerSequence) {
            // 消费者最小序号, 不可能比生产者序号大
            long minSequence = SequenceUtil.getMinSequence(consumerSequences);
            consumerSequenceCache.set(minSequence);
            if (wrapPoint > minSequence) {
                return false;
            }
        }
        return true;
    }


    @Override
    public T get(long sequence) {
        return ringBuffer.get(sequence);
    }

    @Override
    public void publish(long sequence) {
        while (true) {
            long cur = publishedSequence.get();
            if (cur >= sequence) {
                return;
            }

            //修改生产者已发布序列号，消费者就可以进行消费
            if (publishedSequence.compareAndSet(cur, sequence)) {
                // 根据不同的等待策略唤醒消费线程
                waitStrategy.signalAllWhenBlocking();
                return;
            }
        }

    }

    @Override
    public void addConsumerSequences(Sequence... consumerSequences) {
        SequenceUtil.addSequences(this, SEQUENCE_UPDATER, this.cursor, consumerSequences);
    }

    @Override
    public boolean removeConsumerSequence(Sequence sequence) {
        return SequenceUtil.removeSequence(this, SEQUENCE_UPDATER, sequence);
    }

    @Override
    public WaitStrategy getWaitStrategy() {
        return this.waitStrategy;
    }

    @Override
    public Sequence getCursor() {
        return this.cursor;
    }

    @Override
    public RingBuffer<T> getRingBuffer() {
        return this.ringBuffer;
    }

    @Override
    public Sequence getPublishedSequence() {
        return this.publishedSequence;
    }
}
