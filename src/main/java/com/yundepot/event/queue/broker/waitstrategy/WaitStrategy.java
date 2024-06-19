package com.yundepot.event.queue.broker.waitstrategy;


import com.yundepot.event.queue.common.Sequence;

/**
 * 消费者等待策略
 * @author zhaiyanan
 * @date 2024/6/14  10:24
 */
public interface WaitStrategy {

    /**
     *
     * @param sequence 期待的序列起点
     * @param cursorSequence broker的序列
     * @param dependentSequence 依赖的序列
     * @return 最大可见的序列, 不一定可消费
     * @throws Exception
     */
    long waitFor(long sequence, Sequence cursorSequence, Sequence dependentSequence) throws Exception;

    void signalAllWhenBlocking();
}
