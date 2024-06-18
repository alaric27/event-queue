package com.yundepot.event.queue.quickstart;

import com.yundepot.event.queue.producer.EventFactory;

/**
 * @author zhaiyanan
 * @date 2024/6/17  16:03
 */
public class OrderEventFactory implements EventFactory<OrderEvent> {
    @Override
    public OrderEvent newInstance() {
        return new OrderEvent();
    }
}
