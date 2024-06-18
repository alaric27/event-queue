package com.yundepot.event.queue.broker;

import com.yundepot.event.queue.common.Sequence;
import com.yundepot.event.queue.consumer.waitstrategy.WaitStrategy;

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
     * 获取当序号
     */
    long getCursor();

    /**
     * 获取序号
     */
    Sequence getCursorSequence();

    /**
     * 获得下一个可用的生产者序列值, 获取不到会等待
     */
    long next();

    /**
     * 获取一个可用的生产者序列区间, 获取不到会等待
     */
    long next(int n);

    /**
     * 尝试获取下一个可用生产者序列值, 获取不到会抛异常
     */
    long tryNext() throws Exception;

    /**
     * 尝试获取下一个可用生产者序列区间, 获取不到会抛异常
     */
    long tryNext(int n) throws Exception;

    /**
     * 发布
     */
    void publish(long sequence);

    /**
     * 区间发布
     */
    void publish(long lo, long hi);

    /**
     * 判断某个序列是否已发布，可消费
     */
    public boolean canConsume(long sequence);

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
     * 获取等待策略
     */
    WaitStrategy getWaitStrategy();
}
