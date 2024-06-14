package com.yundepot.event.queue.producer;

import com.yundepot.event.queue.RingBuffer;
import com.yundepot.event.queue.Sequence;
import com.yundepot.event.queue.consumer.WaitStrategy;
import com.yundepot.event.queue.util.SequenceUtil;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author zhaiyanan
 * @date 2024/6/13  17:02
 */
public abstract class AbstractProducer<T> implements Producer<T> {
    protected RingBuffer<T> ringBuffer;

    // 生产者序列号
    protected final Sequence producerSequence = new Sequence(Sequence.INITIAL_VALUE);

    // 消费者进度集合
    protected volatile Sequence[] consumerSequences = new Sequence[0];
    // todo 和消费进度合并为消费者
    protected WaitStrategy waitStrategy;


    private static final AtomicReferenceFieldUpdater<AbstractProducer, Sequence[]> SEQUENCE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(AbstractProducer.class, Sequence[].class, "consumerSequences");

    public AbstractProducer(RingBuffer ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    @Override
    public int getBufferSize() {
        return ringBuffer.getBufferSize();
    }

    @Override
    public T get(long sequence) {
        return ringBuffer.get(sequence);
    }

    @Override
    public void addConsumerSequences(Sequence... consumerSequences) {
        SequenceUtil.addSequences(this, SEQUENCE_UPDATER, producerSequence.get(), consumerSequences);
    }

    @Override
    public boolean removeConsumerSequence(Sequence sequence) {
        return SequenceUtil.removeSequence(this, SEQUENCE_UPDATER, sequence);
    }

    @Override
    public long getMinConsumerSequence() {
        return SequenceUtil.getMinSequence(consumerSequences, producerSequence.get());
    }
}
