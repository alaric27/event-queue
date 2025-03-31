package com.yundepot.event.queue.producer;

import com.yundepot.event.queue.broker.Broker;

/**
 * @author zhaiyanan
 * @date 2024/6/18  14:06
 */
public class SingleProducer<T> extends AbstractProducer<T> {

    public SingleProducer(Broker<T> broker) {
        super(broker);
    }

    @Override
    public void publish(long sequence) {
        broker.publish(sequence);
    }
}
