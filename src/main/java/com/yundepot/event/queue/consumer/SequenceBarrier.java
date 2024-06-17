package com.yundepot.event.queue.consumer;

import com.yundepot.event.queue.broker.Broker;
import com.yundepot.event.queue.broker.WaitStrategy;
import com.yundepot.event.queue.common.FixedSequenceGroup;
import com.yundepot.event.queue.common.Sequence;

import java.util.Objects;

/**
 * @author zhaiyanan
 * @date 2024/6/17  16:49
 */
public class SequenceBarrier {
    private final WaitStrategy waitStrategy;
    private final Sequence dependentSequence;
    private final Sequence cursorSequence;
    private final Broker broker;

    public SequenceBarrier(final Broker broker, final Sequence[] dependentSequences) {
        this.waitStrategy = broker.getWaitStrategy();
        this.cursorSequence = broker.getCursorSequence();
        this.broker = broker;
        if (Objects.isNull(dependentSequences) || dependentSequences.length == 0) {
            dependentSequence = cursorSequence;
        } else {
            dependentSequence = new FixedSequenceGroup(dependentSequences);
        }
    }

    /**
     * 等待其他序列,返回最大的可消费队列
     */
    public long waitFor(long sequence) throws Exception {
        long availableSequence = waitStrategy.waitFor(sequence, cursorSequence, dependentSequence);
        if (availableSequence < sequence) {
            return availableSequence;
        }
        return broker.getHighestPublishedSequence(sequence, availableSequence);
    }
}
