package com.yundepot.event.queue.producer;

import com.yundepot.event.queue.broker.Broker;

/**
 * @author zhaiyanan
 * @date 2024/6/17  15:15
 */
public class DefaultProducer<T> implements Producer<T> {

    private Broker<T> broker;

    // todo 功能差分到broker后过于薄弱， 单生产者如何实现

    @Override
    public long next() {
        return broker.next();
    }

    @Override
    public long next(int n) {
        return broker.next(n);
    }

    @Override
    public long tryNext() throws Exception {
        return broker.tryNext();
    }

    @Override
    public long tryNext(int n) throws Exception {
        return broker.tryNext(n);
    }

    @Override
    public void publish(long sequence) {
        broker.publish(sequence);
    }

    @Override
    public void publish(long lo, long hi) {
        broker.publish(lo, hi);
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
    public void setBroker(Broker<T> broker) {
        this.broker = broker;
    }
}
