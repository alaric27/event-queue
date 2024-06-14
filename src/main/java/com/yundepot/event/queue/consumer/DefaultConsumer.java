package com.yundepot.event.queue.consumer;

import com.yundepot.event.queue.Sequence;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author zhaiyanan
 * @date 2024/6/14  16:49
 */
public class DefaultConsumer implements Consumer {
    private final Sequence sequence = new Sequence(Sequence.INITIAL_VALUE);
    private ThreadFactory threadFactory;

    private static final int IDLE = 0;
    private static final int HALTED = IDLE + 1;
    private static final int RUNNING = HALTED + 1;

    private final AtomicInteger running = new AtomicInteger(IDLE);

    @Override
    public Sequence getSequence() {
        return sequence;
    }

    @Override
    public void start() {
        threadFactory.newThread(() -> run()).start();
    }

    private void run() {
        int witnessValue = running.compareAndExchange(IDLE, RUNNING);
        if (witnessValue == IDLE) {
            sequenceBarrier.clearAlert();
            notifyStart();
            try {
                if (running.get() == RUNNING) {
                    processEvents();
                }
            } finally {
                notifyShutdown();
                running.set(IDLE);
            }
        }
        else {
            if (witnessValue == RUNNING) {
                throw new IllegalStateException("Thread is already running");
            } else {
                earlyExit();
            }
        }
    }
}
