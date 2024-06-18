package com.yundepot.event.queue.consumer;

import com.yundepot.event.queue.broker.Broker;
import com.yundepot.event.queue.common.FixedSequenceGroup;
import com.yundepot.event.queue.common.Sequence;
import java.util.Objects;

/**
 * 消费者消费屏障, 控制消费进度
 * @author zhaiyanan
 * @date 2024/6/17  16:49
 */
public class SequenceBarrier {
    private final Sequence dependentSequence;
    private final Sequence cursorSequence;
    private final Broker broker;

    public SequenceBarrier(final Broker broker, final Sequence[] dependentSequences) {
        this.cursorSequence = broker.getCursorSequence();
        this.broker = broker;
        if (Objects.isNull(dependentSequences) || dependentSequences.length == 0) {
            dependentSequence = cursorSequence;
        } else {
            dependentSequence = new FixedSequenceGroup(dependentSequences);
        }
    }

    /**
     * 等待其他序列,返回最大的可消费序列号
     * return: 比sequence大的可消费序列号
     */
    public long waitFor(long sequence) throws Exception {
        long availableSequence = broker.getWaitStrategy().waitFor(sequence, cursorSequence, dependentSequence);
        if (availableSequence < sequence) {
            return availableSequence;
        }
        return broker.getHighestPublishedSequence(sequence, availableSequence);
    }
}
