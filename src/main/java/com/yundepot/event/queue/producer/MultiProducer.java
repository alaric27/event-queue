package com.yundepot.event.queue.producer;

import com.yundepot.event.queue.common.Sequence;

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
