package com.yundepot.event.queue.consumer;

import com.yundepot.event.queue.common.Sequence;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhaiyanan
 * @date 2024/6/18  10:31
 */
public class ConsumerRepository {
    private List<Consumer> consumerList = new ArrayList<>();
    private final Map<EventHandler, Consumer> eventHandlerConsumerMap = new IdentityHashMap<>();

    public void add(Consumer consumer) {
        consumerList.add(consumer);
        eventHandlerConsumerMap.put(consumer.getEventHandler(), consumer);
    }

    public void startAll() {
        consumerList.forEach(c -> c.start());
    }

    public void shutdownAll() {
        consumerList.forEach(Consumer::shutdown);
    }

    public Sequence getSequenceFor(EventHandler eventHandler) {
        return eventHandlerConsumerMap.get(eventHandler).getSequence();
    }

    public List<Consumer> getAllConsumer() {
        return consumerList;
    }
}
