package com.yundepot.event.queue.producer;

import com.yundepot.event.queue.broker.Broker;
import com.yundepot.event.queue.common.Sequence;

/**
 * 生产者
 * @author zhaiyanan
 * @date 2024/6/13  16:37
 */
public interface Producer<T> {

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
     * 获取指定序列值
     */
    T get(long sequence);

    /**
     * 直接发布事件
     */
    void publishEvent(final EventTranslator<T> translator);

    /**
     * 直接发布事件
     */
    void publishEvent(final EventTranslatorVarargs<T> translator, Object... args);

    /**
     * 获取生产者序列
     */
    Sequence getCursor();

    /**
     * 序号是否能消费
     */
    boolean canConsume(long sequence);

    /**
     * 获取区间内已发布的最大sequence
     */
    long getHighestPublishedSequence(long lo, long hi);

    /**
     * 设置协调者
     */
    void setBroker(Broker<T> broker);
}
