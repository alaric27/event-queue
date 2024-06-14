package com.yundepot.event.queue.consumer;

import com.yundepot.event.queue.Sequence;


/**
 * @author zhaiyanan
 * @date 2024/6/14  16:49
 */
public class DefaultConsumer implements Consumer {
    private final Sequence sequence = new Sequence(Sequence.INITIAL_VALUE);

    @Override
    public Sequence getSequence() {
        return sequence;
    }

    @Override
    public void start() {

    }

    private void run() {

    }
}
