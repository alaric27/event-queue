package com.yundepot.event.queue.common;

import com.yundepot.event.queue.util.SequenceUtil;

import java.util.Arrays;

/**
 * @author zhaiyanan
 * @date 2024/6/17  11:10
 */
public class FixedSequenceGroup extends Sequence {
    private final Sequence[] sequences;

    public FixedSequenceGroup(Sequence... sequences) {
        this.sequences = Arrays.copyOf(sequences, sequences.length);
    }

    @Override
    public long get() {
        return SequenceUtil.getMinSequence(sequences);
    }

    @Override
    public void set(long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean compareAndSet(long expectedValue, long newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long incrementAndGet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long addAndGet(long increment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getAndAdd(long increment) {
        throw new UnsupportedOperationException();
    }
}
