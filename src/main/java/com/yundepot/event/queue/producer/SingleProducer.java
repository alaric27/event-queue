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
        //修改生产者已发布序列号，消费者就可以进行消费
        broker.getPublishedSequence().set(sequence);
        // 根据不同的等待策略唤醒消费线程
        broker.getWaitStrategy().signalAllWhenBlocking();
    }
}
