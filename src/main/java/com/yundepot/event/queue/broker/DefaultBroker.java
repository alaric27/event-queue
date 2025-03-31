package com.yundepot.event.queue.broker;

import com.yundepot.event.queue.broker.waitstrategy.WaitStrategy;
import com.yundepot.event.queue.common.Sequence;
import com.yundepot.event.queue.util.SequenceUtil;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

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


    private final WaitStrategy waitStrategy;
    private volatile Sequence[] consumerSequences = new Sequence[0];
    private static final AtomicReferenceFieldUpdater<DefaultBroker, Sequence[]> SEQUENCE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(DefaultBroker.class, Sequence[].class, "consumerSequences");

    public DefaultBroker(RingBuffer<T> ringBuffer, WaitStrategy waitStrategy) {
        this.ringBuffer = ringBuffer;
        this.waitStrategy = waitStrategy;
    }

    @Override
    public T get(long sequence) {
        return ringBuffer.get(sequence);
    }

    @Override
    public long getHighestPublishedSequence(Long hi) {
        long ps = this.publishedSequence.get();
        if (Objects.isNull(hi)) {
            return ps;
        }
        return hi > ps ? ps : hi;
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
    public long getMinConsumerSequence() {
        return SequenceUtil.getMinSequence(consumerSequences);
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
