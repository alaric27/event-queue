package com.yundepot.event.queue.broker;


import com.yundepot.event.queue.common.Sequence;

/**
 * @author zhaiyanan
 * @date 2024/6/14  10:24
 */
public interface WaitStrategy {
    long waitFor(long sequence, Sequence cursorSequence, Sequence dependentSequence) throws Exception;

    void signalAllWhenBlocking();
}
