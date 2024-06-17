package com.yundepot.event.queue.consumer;

/**
 * @author zhaiyanan
 * @date 2024/6/17  09:52
 */
public interface ExceptionHandler<T> {
    void handleEventException(Throwable ex, long sequence, T event);

    void handleOnStartException(Throwable ex);

    void handleOnShutdownException(Throwable ex);
}
