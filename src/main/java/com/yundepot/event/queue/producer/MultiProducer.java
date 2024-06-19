package com.yundepot.event.queue.producer;

import com.yundepot.event.queue.common.CapacityException;
import com.yundepot.event.queue.common.Sequence;
import com.yundepot.event.queue.util.SequenceUtil;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

/**
 * @author zhaiyanan
 * @date 2024/6/18  13:57
 */
public class MultiProducer<T> extends AbstractProducer<T> {

    /**
     * 缓存消费进度，避免多次计算
     */
    private final Sequence consumerSequenceCache = new Sequence(Sequence.INITIAL_VALUE);

    /**
     * 跟踪每个RingBuffer的槽发布状态
     */
    private final int[] publishedBuffer;
    private static final VarHandle AVAILABLE_ARRAY = MethodHandles.arrayElementVarHandle(int[].class);


    public MultiProducer(RingBuffer<T> ringBuffer) {
        super(ringBuffer);
        publishedBuffer = new int[ringBuffer.getBufferSize()];
        Arrays.fill(publishedBuffer, -1);
    }

    @Override
    public long next(int n) {
        long nextSequence = cursor.addAndGet(n);
        while (!hasAvailableCapacity(nextSequence)) {
            LockSupport.parkNanos(1L);
        }
        return nextSequence;
    }

    @Override
    public long tryNext(int n) throws Exception {
        if (n < 1) {
            throw new IllegalArgumentException("n must be > 0");
        }
        long current;
        long next;
        do {
            current = cursor.get();
            next = current + n;
            if (!hasAvailableCapacity(next)) {
                throw new CapacityException();
            }
        } while (!cursor.compareAndSet(current, next));
        return next;
    }

    @Override
    public void publish(long sequence) {
        setPublished(sequence);
        broker.getWaitStrategy().signalAllWhenBlocking();
    }

    @Override
    public void publish(long lo, long hi) {
        for (long i = lo; i <= hi; i++) {
            setPublished(i);
        }
        broker.getWaitStrategy().signalAllWhenBlocking();
    }

    @Override
    public boolean canConsume(long sequence) {
        int index = ringBuffer.calculateIndex(sequence);
        int flag = calculateAvailableFlag(sequence);
        return (int) AVAILABLE_ARRAY.getAcquire(publishedBuffer, index) == flag;
    }

    @Override
    public long getHighestPublishedSequence(long lo, long hi) {
        for (long sequence = lo; sequence <= hi; sequence++) {
            if (!canConsume(sequence)) {
                return sequence - 1;
            }
        }
        return hi;
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

    private void setPublished(final long sequence) {
        AVAILABLE_ARRAY.setRelease(publishedBuffer, ringBuffer.calculateIndex(sequence), calculateAvailableFlag(sequence));
    }

    /**
     * 无符号右移， 类似求sequence / bufferSize
     * 这样处理的好处是该方法的返回值，bufferSize个一组，一直递增
     * 判断是否可用，只需要判断availableBuffer中对应索引的值，是否为sequence对应flag即可
     */
    private int calculateAvailableFlag(final long sequence) {
        return (int) (sequence >>> ringBuffer.getIndexShift());
    }
}
