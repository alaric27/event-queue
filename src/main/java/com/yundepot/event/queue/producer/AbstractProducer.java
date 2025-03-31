package com.yundepot.event.queue.producer;

import com.yundepot.event.queue.broker.Broker;
import com.yundepot.event.queue.common.Sequence;

import java.util.concurrent.locks.LockSupport;

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

    /**
     * 缓存消费进度，避免多次计算
     */
    private final Sequence consumerSequenceCache = new Sequence(Sequence.INITIAL_VALUE);


    public AbstractProducer(RingBuffer<T> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    @Override
    public long next() {
        return next(1);
    }

    @Override
    public long next(int n) {
        if (n < 1 || n > ringBuffer.getBufferSize()) {
            throw new IllegalArgumentException("n must be > 0 and < bufferSize");
        }

        long nextSequence = cursor.addAndGet(n);
        while (!hasAvailableCapacity(nextSequence)) {
            LockSupport.parkNanos(1L);
        }
        return nextSequence;
    }


    private boolean hasAvailableCapacity(long next) {
        // 用于判断生产者的序号在环形数组中是否绕过了消费者最小的序号
        long wrapPoint = next - ringBuffer.getBufferSize();
        long cachedConsumerSequence = consumerSequenceCache.get();

        //  判断wrapPoint是否大于上一次计算时消费者的最小值, 如果大于则进行一次从新计算判断，否则直接后续赋值操作
        if (wrapPoint > cachedConsumerSequence) {
            // 消费者最小序号, 不可能比生产者序号大
            long minSequence = broker.getMinConsumerSequence();
            consumerSequenceCache.set(minSequence);
            if (wrapPoint > minSequence) {
                return false;
            }
        }
        return true;
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
