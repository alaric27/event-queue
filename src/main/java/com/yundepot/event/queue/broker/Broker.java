package com.yundepot.event.queue.broker;

import com.yundepot.event.queue.broker.waitstrategy.WaitStrategy;
import com.yundepot.event.queue.common.Sequence;

/**
 * @author zhaiyanan
 * @date 2024/6/17  12:54
 */
public interface Broker<T> {

    /**
     * 获取一个可用的生产者序列区间, 获取不到会等待
     */
    long next(int n);

    /**
     * 获取指定序列值
     */
    T get(long sequence);

    /**
     * 发布
     */
    void publish(long sequence);

    /**
     * 添加消费者进度
     */
    void addConsumerSequences(Sequence... consumerSequences);

    /**
     * 删除消费者进度
     */
    boolean removeConsumerSequence(Sequence sequence);

    /**
     * 获取等待策略
     */
    WaitStrategy getWaitStrategy();

    /**
     * 获取生产者序列号
     */
    Sequence getCursor();

    RingBuffer<T> getRingBuffer();

    /**
     * 获取已发布的最大序列号
     */
    Sequence getPublishedSequence();
}
