package com.yundepot.event.queue.producer;

import com.yundepot.event.queue.common.Sequence;

/**
 * @author zhaiyanan
 * @date 2024/6/18  14:06
 */
public class SingleProducer<T> extends AbstractProducer<T> {

    /**
     * 已发布序列号
     */
    private final Sequence publishedSequence = new Sequence(Sequence.INITIAL_VALUE);


    public SingleProducer(RingBuffer<T> ringBuffer) {
        super(ringBuffer);
    }


    @Override
    public void publish(long sequence) {
        //修改生产者已发布序列号，消费者就可以进行消费
        publishedSequence.set(sequence);
        // 根据不同的等待策略唤醒消费线程
        broker.getWaitStrategy().signalAllWhenBlocking();
    }

    @Override
    public void publish(long lo, long hi) {
        publish(hi);
    }

    @Override
    public boolean canConsume(long sequence) {
        long currentSequence = publishedSequence.get();
        return sequence <= currentSequence && sequence > currentSequence - ringBuffer.getBufferSize();
    }

    @Override
    public long getHighestPublishedSequence(long lo, long hi) {
        return hi;
    }
}
