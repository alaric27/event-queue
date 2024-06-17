package com.yundepot.event.queue.quickstart;

import com.yundepot.event.queue.EventQueue;
import com.yundepot.event.queue.consumer.BlockingWaitStrategy;
import com.yundepot.event.queue.broker.RingBuffer;

import java.util.concurrent.locks.LockSupport;

/**
 * @author zhaiyanan
 * @date 2024/6/17  16:05
 */
public class OrderEventTest {
    public static void main(String[] args) throws Exception {
        EventQueue<OrderEvent> eventQueue = new EventQueue<>(new RingBuffer<>(new OrderEventFactory(), 1024), new BlockingWaitStrategy());
        eventQueue.handleEvents(new OrderEventHandler());
        eventQueue.start();

        eventQueue.publishEvent((orderEvent, sequence) -> {
            orderEvent.setValue(1L);
        });
        LockSupport.park();
    }
}
