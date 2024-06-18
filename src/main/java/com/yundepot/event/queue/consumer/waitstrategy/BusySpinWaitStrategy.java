package com.yundepot.event.queue.consumer.waitstrategy;

import com.yundepot.event.queue.common.Sequence;

/**
 * 自旋等待策略, 会导致较高的CPU消耗
 * 适用于高度并行的系统，其中事件生成频率非常高，几乎不需要等待, 但是，这会导致较高的CPU消耗。
 * 适用于等待时间很短，等待的资源很快就会变为可用
 * @author zhaiyanan
 * @date 2024/6/18  15:08
 */
public class BusySpinWaitStrategy implements WaitStrategy {
    @Override
    public long waitFor(long sequence, Sequence cursorSequence, Sequence dependentSequence) throws Exception {
        long availableSequence;
        while ((availableSequence = dependentSequence.get()) < sequence) {
            Thread.onSpinWait();
        }
        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking() {

    }
}
