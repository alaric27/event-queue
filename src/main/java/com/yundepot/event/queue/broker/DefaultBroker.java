package com.yundepot.event.queue.broker;

import com.yundepot.event.queue.broker.waitstrategy.WaitStrategy;
import com.yundepot.event.queue.common.Sequence;
import com.yundepot.event.queue.producer.Producer;
import com.yundepot.event.queue.util.SequenceUtil;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * 消费者和生产者协调者
 * @author zhaiyanan
 * @date 2024/6/17  12:54
 */
public class DefaultBroker<T> implements Broker {
    private volatile Producer<T> producer;
    private final WaitStrategy waitStrategy;
    private volatile Sequence[] consumerSequences = new Sequence[0];
    private static final AtomicReferenceFieldUpdater<DefaultBroker, Sequence[]> SEQUENCE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(DefaultBroker.class, Sequence[].class, "consumerSequences");

    public DefaultBroker(WaitStrategy waitStrategy) {
        this.waitStrategy = waitStrategy;
    }

    @Override
    public T get(long sequence) {
        return producer.get(sequence);
    }

    @Override
    public long getHighestPublishedSequence(long lo, long hi) {
        return producer.getHighestPublishedSequence(lo, hi);
    }

    @Override
    public void addConsumerSequences(Sequence... consumerSequences) {
        SequenceUtil.addSequences(this, SEQUENCE_UPDATER, producer.getCursor(), consumerSequences);
    }

    @Override
    public boolean removeConsumerSequence(Sequence sequence) {
        return SequenceUtil.removeSequence(this, SEQUENCE_UPDATER, sequence);
    }

    @Override
    public Sequence[] getConsumerSequences() {
        return consumerSequences;
    }

    @Override
    public WaitStrategy getWaitStrategy() {
        return this.waitStrategy;
    }

    @Override
    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    @Override
    public Producer getProducer() {
        return producer;
    }
}
