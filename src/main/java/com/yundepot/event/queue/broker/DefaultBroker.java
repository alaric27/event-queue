package com.yundepot.event.queue.broker;

import com.yundepot.event.queue.common.CapacityException;
import com.yundepot.event.queue.common.FixedSequenceGroup;
import com.yundepot.event.queue.common.Sequence;
import com.yundepot.event.queue.util.SequenceUtil;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.LockSupport;

/**
 * 消费者和生产者协调者
 * @author zhaiyanan
 * @date 2024/6/17  12:54
 */
public class DefaultBroker<T> implements Broker {

    /**
     * 数据存储
     */
    private final RingBuffer<T> ringBuffer;

    /**
     * 数据存储进度
     */
    protected final Sequence cursor = new Sequence(Sequence.INITIAL_VALUE);

    private final WaitStrategy waitStrategy;
    private Sequence dependentSequence;


    /**
     * 缓存消费进度，避免多次计算
     */
    private final Sequence consumerSequenceCache = new Sequence(Sequence.INITIAL_VALUE);

    /**
     * 跟踪每个RingBuffer的槽发布状态
     */
    private final int[] publishedBuffer;
    private static final VarHandle AVAILABLE_ARRAY = MethodHandles.arrayElementVarHandle(int[].class);

    public DefaultBroker(RingBuffer ringBuffer, WaitStrategy waitStrategy) {
        this.ringBuffer = ringBuffer;
        this.waitStrategy = waitStrategy;
        publishedBuffer = new int[ringBuffer.getBufferSize()];
        Arrays.fill(publishedBuffer, -1);
    }

    @Override
    public T get(long sequence) {
        return ringBuffer.get(sequence);
    }

    /**
     * 等待其他序列,返回最大的可消费队列
     */
    @Override
    public long waitFor(long sequence) throws Exception {
        long availableSequence = waitStrategy.waitFor(sequence, cursor, dependentSequence);
        if (availableSequence < sequence) {
            return availableSequence;
        }
        return getHighestPublishedSequence(sequence, availableSequence);
    }

    @Override
    public void signalAllWhenBlocking() {
        waitStrategy.signalAllWhenBlocking();
    }

    @Override
    public long getCursor() {
        return cursor.get();
    }

    @Override
    public long next() {
        return next(1);
    }

    @Override
    public long next(int n) {
        long current = cursor.getAndAdd(n);
        long nextSequence = current + n;
        while (!hasAvailableCapacity(nextSequence, current)) {
            LockSupport.parkNanos(1L);
        }
        return nextSequence;
    }

    @Override
    public long tryNext() throws Exception {
        return tryNext(1);
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
            if (!hasAvailableCapacity(n, current)) {
                throw new CapacityException();
            }
        } while (!cursor.compareAndSet(current, next));
        return next;
    }

    @Override
    public void publish(long sequence) {
        setPublished(sequence);
        waitStrategy.signalAllWhenBlocking();

    }

    @Override
    public void publish(long lo, long hi) {
        for (long i = lo; i <= hi; i++) {
            setPublished(i);
        }
        waitStrategy.signalAllWhenBlocking();
    }

    /**
     * 验证当前sequence 是否处于发布状态
     */
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

    @Override
    public void addConsumerSequences(Sequence... consumerSequences) {
        // 如果没设置依赖的消费者，则只依赖生产者进度
        if (Objects.isNull(consumerSequences) || consumerSequences.length == 0) {
            dependentSequence = cursor;
        } else {
            dependentSequence = new FixedSequenceGroup(consumerSequences);
        }
    }

    private boolean hasAvailableCapacity(long next, long current) {
        // 用于判断生产者的序号在环形数组中是否绕过了消费者最小的序号
        long wrapPoint = next - ringBuffer.getBufferSize();
        long cachedConsumerSequence = consumerSequenceCache.get();

        //  判断wrapPoint是否大于上一次计算时消费者的最小值, 如果大于则进行一次从新计算判断，否则直接后续赋值操作
        if (wrapPoint > cachedConsumerSequence) {
            long minSequence = SequenceUtil.getMinSequence(dependentSequence, current);
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
