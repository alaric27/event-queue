package com.yundepot.event.queue.producer;

import com.yundepot.event.queue.broker.Broker;
import com.yundepot.event.queue.common.Sequence;

/**
 * @author zhaiyanan
 * @date 2024/6/18  13:53
 */
public abstract class AbstractProducer<T> implements Producer<T> {
    /**
     * 数据存储
     */
    protected final RingBuffer<T> ringBuffer;

    /**
     * 数据存储进度
     */
    protected final Sequence cursor = new Sequence(Sequence.INITIAL_VALUE);

    /**
     * 协调者
     */
    protected Broker<T> broker;

    public AbstractProducer(RingBuffer<T> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    @Override
    public long next() {
        return next(1);
    }

    @Override
    public long tryNext() throws Exception {
        return tryNext(1);
    }

    @Override
    public T get(long sequence) {
        return ringBuffer.get(sequence);
    }

    @Override
    public Sequence getCursor() {
        return cursor;
    }

    @Override
    public void publishEvent(EventTranslator<T> translator) {
        final long sequence = next();
        try {
            translator.translateTo(get(sequence), sequence);
        } finally {
            publish(sequence);
        }
    }

    @Override
    public void publishEvent(EventTranslatorVarargs<T> translator, Object... args) {
        final long sequence = next();
        try {
            translator.translateTo(get(sequence), sequence, args);
        } finally {
            publish(sequence);
        }
    }

    @Override
    public void setBroker(Broker<T> broker) {
        this.broker = broker;
    }
}
