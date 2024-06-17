package com.yundepot.event.queue.util;


import com.yundepot.event.queue.common.Sequence;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static java.util.Arrays.copyOf;

/**
 * @author zhaiyanan
 * @date 2024/6/14  10:00
 */
public class SequenceUtil {
    public static <T> void addSequences(final T holder, final AtomicReferenceFieldUpdater<T, Sequence[]> updater, final Sequence cursor, final Sequence... sequencesToAdd) {
        long cursorSequence;
        Sequence[] updatedSequences;
        Sequence[] currentSequences;
        do {
            currentSequences = updater.get(holder);
            updatedSequences = copyOf(currentSequences, currentSequences.length + sequencesToAdd.length);
            cursorSequence = cursor.get();

            int index = currentSequences.length;
            for (Sequence sequence : sequencesToAdd) {
                sequence.set(cursorSequence);
                updatedSequences[index++] = sequence;
            }
        } while (!updater.compareAndSet(holder, currentSequences, updatedSequences));

        cursorSequence = cursor.get();
        for (Sequence sequence : sequencesToAdd) {
            sequence.set(cursorSequence);
        }
    }

    public static <T> boolean removeSequence(T holder, AtomicReferenceFieldUpdater<T, Sequence[]> sequenceUpdater, Sequence sequence) {
        int numToRemove;
        Sequence[] oldSequences;
        Sequence[] newSequences;

        do {
            oldSequences = sequenceUpdater.get(holder);

            numToRemove = countMatching(oldSequences, sequence);

            if (0 == numToRemove) {
                break;
            }

            final int oldSize = oldSequences.length;
            newSequences = new Sequence[oldSize - numToRemove];

            for (int i = 0, pos = 0; i < oldSize; i++) {
                final Sequence testSequence = oldSequences[i];
                if (sequence != testSequence) {
                    newSequences[pos++] = testSequence;
                }
            }
        } while (!sequenceUpdater.compareAndSet(holder, oldSequences, newSequences));
        return numToRemove != 0;
    }

    private static <T> int countMatching(final T[] values, final T toMatch) {
        int numToRemove = 0;
        for (T value : values) {
            if (value == toMatch) {
                numToRemove++;
            }
        }
        return numToRemove;
    }

    /**
     * 获取一组序列的最小值
     */
    public static long getMinSequence(Sequence[] sequences, long defaultValue) {
        long minSequence = defaultValue;
        for (int i = 0, n = sequences.length; i < n; i++) {
            long value = sequences[i].get();
            minSequence = Math.min(minSequence, value);
        }
        return minSequence;
    }

    /**
     * 求2的对数
     */
    public static int log2(final int value) {
        if (value < 1) {
            throw new IllegalArgumentException("value must be a positive number");
        }
        return Integer.SIZE - Integer.numberOfLeadingZeros(value) - 1;
    }
}
