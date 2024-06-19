package com.yundepot.event.queue;

import com.yundepot.event.queue.common.Sequence;
import com.yundepot.event.queue.consumer.EventHandler;

/**
 * @author zhaiyanan
 * @date 2024/6/17  11:37
 */
public class EventHandlerGroup<T> {
    private final Sequence[] sequences;
    private final EventQueue<T> eventQueue;

    public EventHandlerGroup(EventQueue<T> eventQueue, Sequence[] sequences) {
        this.eventQueue = eventQueue;
        this.sequences = sequences;
    }

    /**
     * 合并其他的EventHandlerGroup
     */
    public EventHandlerGroup<T> and(final EventHandlerGroup<T> otherHandlerGroup) {
        final Sequence[] combinedSequences = new Sequence[this.sequences.length + otherHandlerGroup.sequences.length];
        System.arraycopy(this.sequences, 0, combinedSequences, 0, this.sequences.length);
        System.arraycopy(otherHandlerGroup.sequences, 0, combinedSequences, this.sequences.length, otherHandlerGroup.sequences.length);
        return new EventHandlerGroup<>(eventQueue, combinedSequences);
    }

    /**
     * 和前一个EventHandlerGroup串行，组内并行
     */
    public EventHandlerGroup<T> then(final EventHandler<? super T>... handlers) {
        return eventQueue.addConsumers(handlers, sequences);
    }
}
