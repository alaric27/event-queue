package com.yundepot.event.queue.quickstart;

import com.yundepot.event.queue.consumer.EventHandler;

/**
 * @author zhaiyanan
 * @date 2024/6/17  16:03
 */
public class OrderEventHandler implements EventHandler<OrderEvent> {
    @Override
    public void onEvent(OrderEvent event, long sequence) throws Exception {
        System.out.println("OrderEventHandler" + event.getValue() + "  " + sequence);
    }

    @Override
    public void onStart() {
        System.out.println("start");
    }

    @Override
    public void onShutdown() {
        System.out.println("shutdown");
    }
}
