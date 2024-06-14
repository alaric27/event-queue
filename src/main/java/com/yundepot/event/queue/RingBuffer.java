package com.yundepot.event.queue;

import com.yundepot.event.queue.util.SequenceUtil;

/**
 * @author zhaiyanan
 * @date 2024/6/12  16:40
 */
public class RingBuffer<T> {

    private final long indexMask;
    private final T[] entries;
    private final int bufferSize;
    private final int indexShift;

    public RingBuffer(EventFactory<T> eventFactory, int bufferSize) {
        if (bufferSize < 1 || Integer.bitCount(bufferSize) != 1) {
            throw new IllegalArgumentException("bufferSize must be a positive power of 2");
        }

        this.bufferSize = bufferSize;
        this.indexMask = bufferSize - 1;
        this.indexShift = SequenceUtil.log2(bufferSize);
        this.entries = (T[]) new Object[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            entries[i] = eventFactory.newInstance();
        }
    }

    public T get(long sequence) {
        return entries[calculateIndex(sequence)];
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public long getIndexMask() {
        return this.indexMask;
    }

    public int getIndexShift() {
        return this.indexShift;
    }

    public int calculateIndex(final long sequence) {
        return (int) (sequence & indexMask);
    }
}
