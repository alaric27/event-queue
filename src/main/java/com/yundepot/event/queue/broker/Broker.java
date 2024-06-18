package com.yundepot.event.queue.broker;

import com.yundepot.event.queue.common.Sequence;
import com.yundepot.event.queue.consumer.waitstrategy.WaitStrategy;
import com.yundepot.event.queue.producer.Producer;

/**
 * @author zhaiyanan
 * @date 2024/6/17  12:54
 */
public interface Broker<T> {

    /**
     * 获取指定序列值
     */
    T get(long sequence);

    /**
     * 获取区间内已发布的最大sequence
     */
    long getHighestPublishedSequence(long lo, long hi);

    /**
     * 通知消费者可消费
     */
    void signalAllWhenBlocking();

    /**
     * 添加消费者进度
     */
    void addConsumerSequences(Sequence... consumerSequences);

    /**
     * 删除消费者进度
     */
    boolean removeConsumerSequence(Sequence sequence);

    /**
     * 获取消费进度
     */
    Sequence[] getConsumerSequences();

    /**
     * 获取等待策略
     */
    WaitStrategy getWaitStrategy();

    /**
     * 设置生产者
     */
    void setProducer(Producer<T> producer);

    /**
     * 获取生产者
     */
    Producer<T> getProducer();
}
