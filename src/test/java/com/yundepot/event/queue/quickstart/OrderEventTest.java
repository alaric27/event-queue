package com.yundepot.event.queue.quickstart;

import com.yundepot.event.queue.EventQueue;
import com.yundepot.event.queue.consumer.waitstrategy.BlockingWaitStrategy;
import com.yundepot.event.queue.consumer.EventHandler;

import java.util.concurrent.locks.LockSupport;

/**
 * @author zhaiyanan
 * @date 2024/6/17  16:05
 */
public class OrderEventTest {
    public static void main(String[] args) throws Exception {
        EventQueue<OrderEvent> eventQueue = new EventQueue<>(new OrderEventFactory(), 1024, new BlockingWaitStrategy());

        EventHandler eh1 = (e, l) -> System.out.println("eh1");
        EventHandler eh2 = (e, l) -> System.out.println("eh2");
        EventHandler eh3 = (e, l) -> System.out.println("eh3");
        EventHandler eh4 = (e, l) -> System.out.println("eh4");
        EventHandler eh5 = (e, l) -> System.out.println("eh5");
        EventHandler eh6 = (e, l) -> System.out.println("eh6");

        /**
         *  多边形消费
         *     2  ---- 4
         * 1 /            \ 6
         *   \            /
         *     3  ---- 5
         */
        eventQueue.handleEventsWith(eh1).then(eh2, eh3);
        eventQueue.after(eh2).then(eh4);
        eventQueue.after(eh3).then(eh5);
        eventQueue.after(eh4, eh5).then(eh6);
        eventQueue.start();

        eventQueue.publishEvent((orderEvent, sequence) -> {
            orderEvent.setValue(1L);
        });
        LockSupport.park();
    }
}
