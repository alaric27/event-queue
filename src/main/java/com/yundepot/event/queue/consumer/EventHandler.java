package com.yundepot.event.queue.consumer;

/**
 * @author zhaiyanan
 * @date 2024/6/12  17:42
 */
public interface EventHandler<T> {
    void onEvent(T event, long sequence) throws Exception;

    default void onStart(){

    }

    default void onShutdown() {

    }
}
