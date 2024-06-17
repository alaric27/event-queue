package com.yundepot.event.queue;

import com.yundepot.event.queue.broker.Broker;
import com.yundepot.event.queue.broker.DefaultBroker;
import com.yundepot.event.queue.broker.RingBuffer;
import com.yundepot.event.queue.broker.WaitStrategy;
import com.yundepot.event.queue.consumer.*;
import com.yundepot.event.queue.producer.DefaultProducer;
import com.yundepot.event.queue.producer.EventTranslator;
import com.yundepot.event.queue.producer.Producer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhaiyanan
 * @date 2024/6/12  17:17
 */
public class EventQueue<T> {
    private Producer<T> producer;
    private List<Consumer> consumerList;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private Broker<T> broker;


    public EventQueue(RingBuffer<T> ringBuffer, WaitStrategy waitStrategy) {
        // todo broker consumer producer直接的初始化关系
        this.broker = new DefaultBroker<>(ringBuffer, waitStrategy);
        this.producer = new DefaultProducer<>();
        consumerList = new ArrayList<>();
    }

    public final EventHandlerGroup<T> handleEvents(final EventHandler<? super T>... handlers) {
        return createConsumers(handlers);
    }

    private EventHandlerGroup<T> createConsumers(EventHandler<? super T>[] handlers) {
        checkNotStarted();
        for (int i = 0; i < handlers.length; i++) {
            consumerList.add(new DefaultConsumer<T>(handlers[i], broker));
        }
        return null;
    }

    public void start() {
        // 确保只启动一次
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("EventQueue.start() must only be called once.");
        }
        // todo 消费者序列号依赖如何正确添加
//        broker.addConsumerSequences(consumerList.stream().map(Consumer::getSequence).toArray(Sequence[]::new));
        broker.addConsumerSequences(null);
        producer.setBroker(broker);
        consumerList.forEach(consumer -> consumer.start());
    }

    public void shutdown() {
        consumerList.forEach(consumer -> consumer.showdown());
    }

    /**
     * 确保未启动
     */
    private void checkNotStarted() {
        if (started.get()) {
            throw new IllegalStateException("All event handlers must be added before calling starts.");
        }
    }

    public void publishEvent(final EventTranslator<T> eventTranslator) {
        producer.publishEvent(eventTranslator);
    }
}
