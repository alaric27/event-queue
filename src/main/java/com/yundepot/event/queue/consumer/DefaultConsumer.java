package com.yundepot.event.queue.consumer;

import com.yundepot.event.queue.broker.Broker;
import com.yundepot.event.queue.common.Sequence;
import com.yundepot.event.queue.common.DaemonThreadFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.min;


/**
 * @author zhaiyanan
 * @date 2024/6/14  16:49
 */
public class DefaultConsumer<T> implements Consumer {
    private static final int IDLE = 0;
    private static final int RUNNING = IDLE + 1;
    private static final int SHUTDOWN = RUNNING + 1;

    // 消费进度
    private final Sequence sequence = new Sequence(Sequence.INITIAL_VALUE);
    private final EventHandler eventHandler;

    // 每次处理的事件数量,为了设置停止标识时及时发现
    private final int batchSize = 1000;
    private final AtomicInteger running = new AtomicInteger(IDLE);
    private ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;
    private final Broker<T> broker;
    private SequenceBarrier sequenceBarrier;

    // 异常处理器
    private final ExceptionHandler<T> exceptionHandler = new DefaultExceptionHandler<>();

    public DefaultConsumer(EventHandler eventHandler, Broker broker, SequenceBarrier sequenceBarrier) {
        this.eventHandler = eventHandler;
        this.broker = broker;
        this.sequenceBarrier = sequenceBarrier;
    }

    @Override
    public Sequence getSequence() {
        return sequence;
    }

    @Override
    public void start() {
        threadFactory.newThread(() -> run()).start();
    }

    @Override
    public void shutdown() {
        running.set(SHUTDOWN);
        broker.signalAllWhenBlocking();
    }

    @Override
    public EventHandler getEventHandler() {
        return this.eventHandler;
    }

    private void run() {
        if (!running.compareAndSet(IDLE, RUNNING)) {
            throw new IllegalStateException("Thread is already running");
        }

        notifyStart();
        try {
            if (running.get() == RUNNING) {
                processEvents();
            }
        } finally {
            notifyShutdown();
            running.set(IDLE);
        }
    }

    /**
     * 处理event
     */
    private void processEvents() {
        T event = null;
        long nextSequence = sequence.get() + 1L;
        while (true) {
            try {
                if (running.get() != RUNNING) {
                    return;
                }

                final long availableSequence = sequenceBarrier.waitFor(nextSequence);
                final long endOfBatchSequence = min(nextSequence + batchSize, availableSequence);
                // 循环处理事件
                while (nextSequence <= endOfBatchSequence) {
                    event = broker.get(nextSequence);
                    eventHandler.onEvent(event, nextSequence);
                    nextSequence++;
                }
                sequence.set(endOfBatchSequence);
            } catch (final Throwable ex) {
                // 默认停止消费者; 如果需要重试或者忽略，需要用户在eventHandler.onEvent 中自己实现
                exceptionHandler.handleEventException(ex, nextSequence, event);
                sequence.set(nextSequence);
                nextSequence++;
            }
        }
    }

    private void notifyStart() {
        try {
            eventHandler.onStart();
        } catch (final Throwable ex) {
            exceptionHandler.handleOnStartException(ex);
        }
    }

    private void notifyShutdown() {
        try {
            eventHandler.onShutdown();
        } catch (final Throwable ex) {
            exceptionHandler.handleOnShutdownException(ex);
        }
    }
}
