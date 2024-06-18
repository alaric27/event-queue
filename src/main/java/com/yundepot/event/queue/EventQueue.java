package com.yundepot.event.queue;

import com.yundepot.event.queue.broker.Broker;
import com.yundepot.event.queue.broker.DefaultBroker;
import com.yundepot.event.queue.broker.EventFactory;
import com.yundepot.event.queue.broker.RingBuffer;
import com.yundepot.event.queue.consumer.waitstrategy.BlockingWaitStrategy;
import com.yundepot.event.queue.consumer.waitstrategy.WaitStrategy;
import com.yundepot.event.queue.common.Sequence;
import com.yundepot.event.queue.consumer.*;
import com.yundepot.event.queue.producer.DefaultProducer;
import com.yundepot.event.queue.producer.EventTranslator;
import com.yundepot.event.queue.producer.Producer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhaiyanan
 * @date 2024/6/12  17:17
 */
public class EventQueue<T> {
    private final Producer<T> producer;
    private final ConsumerRepository consumerRepository;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Broker<T> broker;

    public EventQueue(EventFactory<T> eventFactory, int bufferSize) {
        this(new RingBuffer<>(eventFactory, bufferSize));
    }

    private EventQueue(RingBuffer<T> ringBuffer) {
        this(ringBuffer, new BlockingWaitStrategy());
    }

    public EventQueue(EventFactory<T> eventFactory, int bufferSize, WaitStrategy waitStrategy) {
        this(new RingBuffer<>(eventFactory, bufferSize), waitStrategy);
    }

    private EventQueue(RingBuffer<T> ringBuffer, WaitStrategy waitStrategy) {
        this.broker = new DefaultBroker<>(ringBuffer, waitStrategy);
        this.producer = new DefaultProducer<>(broker);
        consumerRepository = new ConsumerRepository();
    }

    public EventHandlerGroup<T> handleEventsWith(final EventHandler<? super T>... handlers) {
        return createConsumers(handlers);
    }

    EventHandlerGroup<T> createConsumers(EventHandler<? super T>[] handlers, Sequence... dependentSequences) {
        checkNotStarted();
        final Sequence[] consumerSequences = new Sequence[handlers.length];
        for (int i = 0; i < handlers.length; i++) {
            DefaultConsumer<T> consumer = new DefaultConsumer<>(handlers[i], broker, new SequenceBarrier(broker, dependentSequences));
            consumerSequences[i] = consumer.getSequence();
            consumerRepository.add(consumer);
        }
        return new EventHandlerGroup<>(this, consumerSequences);
    }

    public EventHandlerGroup<T> after(final EventHandler<? super T>... handlers) {
        final Sequence[] sequences = new Sequence[handlers.length];
        for (int i = 0, handlersLength = handlers.length; i < handlersLength; i++) {
            sequences[i] = consumerRepository.getSequenceFor(handlers[i]);
        }
        return new EventHandlerGroup<>(this, sequences);
    }

    public void start() {
        // 确保只启动一次
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("EventQueue.start() must only be called once.");
        }
        // 启动的时候添加消费者和生产者到协调者
        broker.addConsumerSequences(consumerRepository.getAllConsumer().stream().map(Consumer::getSequence).toArray(Sequence[]::new));
        consumerRepository.startAll();
    }

    public void shutdown() {
        consumerRepository.shutdownAll();
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
