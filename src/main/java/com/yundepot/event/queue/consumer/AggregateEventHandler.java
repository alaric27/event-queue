package com.yundepot.event.queue.consumer;

/**
 * 多个handler 共用一个sequence
 * @author zhaiyanan
 * @date 2024/6/19  14:10
 */
public class AggregateEventHandler<T> implements EventHandler<T>{
    private final EventHandler<T>[] eventHandlers;

    public AggregateEventHandler(EventHandler<T>[] eventHandlers) {
        this.eventHandlers = eventHandlers;
    }

    @Override
    public void onEvent(T event, long sequence) throws Exception {
        for (EventHandler eventHandler : eventHandlers) {
            eventHandler.onEvent(event, sequence);
        }
    }

    @Override
    public void onStart() {
        for (EventHandler eventHandler : eventHandlers) {
            eventHandler.onStart();
        }
    }

    @Override
    public void onShutdown() {
        for (EventHandler eventHandler : eventHandlers) {
            eventHandler.onShutdown();
        }
    }
}
