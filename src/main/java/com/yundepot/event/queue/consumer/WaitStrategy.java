package com.yundepot.event.queue.consumer;


import com.yundepot.event.queue.common.Sequence;

/**
 * 消费者等待策略
 * @author zhaiyanan
 * @date 2024/6/14  10:24
 */
public interface WaitStrategy {
    /**
     * @return 最大可见的序列, 不一定可消费
     */
    long waitFor(long sequence, Sequence cursorSequence, Sequence dependentSequence) throws Exception;

    void signalAllWhenBlocking();
}
