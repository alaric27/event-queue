package com.yundepot.event.queue.producer;

import com.yundepot.event.queue.broker.Broker;

/**
 * @author zhaiyanan
 * @date 2024/6/18  13:53
 */
public abstract class AbstractProducer<T> implements Producer<T> {

    /**
     * 协调者
     */
    protected Broker<T> broker;

    public AbstractProducer(Broker<T> broker) {
        this.broker = broker;
    }

    @Override
    public long next() {
        return next(1);
    }

    @Override
    public long next(int n) {
        return broker.next(n);
    }

    @Override
    public T get(long sequence) {
        return broker.get(sequence);
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
}
