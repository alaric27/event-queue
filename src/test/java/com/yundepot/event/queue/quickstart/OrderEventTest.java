package com.yundepot.event.queue.quickstart;

import com.yundepot.event.queue.EventQueue;
import com.yundepot.event.queue.consumer.EventHandler;
import com.yundepot.event.queue.producer.Producer;
import com.yundepot.event.queue.producer.ProducerType;

import java.util.concurrent.locks.LockSupport;

/**
 * @author zhaiyanan
 * @date 2024/6/17  16:05
 */
public class OrderEventTest {
    public static void main(String[] args) throws Exception {
//        simpleConsume();
//        simpleConsumeWithProducer();
        polygonConsume();
    }

    /**
     *  使用EventTranslator发布消息
     */
    private static void simpleConsume() {
        EventQueue<OrderEvent> eventQueue = new EventQueue<>(new OrderEventFactory(), 1024, ProducerType.SINGLE);

        // 添加消费者
        EventHandler<OrderEvent> eh1 = (e, l) -> System.out.println("eh1 " + e.getValue());
        eventQueue.handleEventsWith(eh1);
        eventQueue.start();


        // 发送消息
        OrderEvent event = new OrderEvent();
        for (int i = 0; i < 10; i++) {
            event.setValue((long) i);
            eventQueue.publishEvent((e, l) -> e.setValue(event.getValue()));
        }
        LockSupport.park();
    }


    /**
     * 使用producer发布消息
     */
    private static void simpleConsumeWithProducer() {
        EventQueue<OrderEvent> eventQueue = new EventQueue<>(new OrderEventFactory(), 1024, ProducerType.SINGLE);

        // 添加消费者
        EventHandler<OrderEvent> eh1 = (e, l) -> System.out.println("eh1 " + e.getValue());
        eventQueue.handleEventsWith(eh1);
        eventQueue.start();

        // 发送消息
        Producer<OrderEvent> producer = eventQueue.getProducer();
        for (int i = 0; i < 10; i++) {
            long next = producer.next();
            try {
                OrderEvent orderEvent = producer.get(next);
                orderEvent.setValue((long) i);
            } finally {
                producer.publish(next);
            }
        }

        LockSupport.park();
    }


    /**
     * 多边形消费
     */
    private static void polygonConsume() {
        EventQueue<OrderEvent> eventQueue = new EventQueue<>(new OrderEventFactory(), 1024);

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

        eventQueue.publishEvent((orderEvent, sequence) -> orderEvent.setValue(1L));
        LockSupport.park();
    }
}
