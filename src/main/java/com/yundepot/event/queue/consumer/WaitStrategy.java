package com.yundepot.event.queue.consumer;


import com.yundepot.event.queue.Sequence;

/**
 * @author zhaiyanan
 * @date 2024/6/14  10:24
 */
public interface WaitStrategy {
    long waitFor(long sequence, Sequence producerSequence, Sequence dependentSequence, SequenceBarrier barrier) throws Exception;

    void signalAllWhenBlocking();
}
