package com.yundepot.event.queue.producer;

import com.yundepot.event.queue.broker.Broker;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;

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


    public MultiProducer(Broker<T> broker) {
        super(broker);
        publishedBuffer = new int[broker.getRingBuffer().getBufferSize()];
        Arrays.fill(publishedBuffer, -1);
    }

    @Override
    public void publish(long sequence) {
        setPublished(sequence);
        long highestPublishedSequence = getHighestPublishedSequence(broker.getPublishedSequence().get(), sequence);
        broker.getPublishedSequence().set(highestPublishedSequence);
        broker.getWaitStrategy().signalAllWhenBlocking();
    }


    private long getHighestPublishedSequence(long lo, long hi) {
        for (long sequence = lo; sequence <= hi; sequence++) {
            if (!canConsume(sequence)) {
                return sequence - 1;
            }
        }
        return lo;
    }

    private boolean canConsume(long sequence) {
        int index = broker.getRingBuffer().calculateIndex(sequence);
        int flag = calculateAvailableFlag(sequence);
        return (int) AVAILABLE_ARRAY.getAcquire(publishedBuffer, index) == flag;
    }

    private void setPublished(final long sequence) {
        AVAILABLE_ARRAY.setRelease(publishedBuffer, broker.getRingBuffer().calculateIndex(sequence), calculateAvailableFlag(sequence));
    }

    /**
     * 无符号右移， 类似求sequence / bufferSize
     * 这样处理的好处是该方法的返回值，bufferSize个一组，一直递增
     * 判断是否可用，只需要判断availableBuffer中对应索引的值，是否为sequence对应flag即可
     */
    private int calculateAvailableFlag(final long sequence) {
        return (int) (sequence >>> broker.getRingBuffer().getIndexShift());
    }
}
