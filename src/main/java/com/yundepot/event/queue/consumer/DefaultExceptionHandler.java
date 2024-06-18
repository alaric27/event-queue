package com.yundepot.event.queue.consumer;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaiyanan
 * @date 2024/6/17  09:53
 */
@Slf4j
public class DefaultExceptionHandler<T> implements ExceptionHandler<T> {
    @Override
    public void handleEventException(final Throwable ex, final long sequence, final Object event) {
        log.info("Exception processing: " + sequence + " " + event, ex);

        throw new RuntimeException(ex);
    }

    @Override
    public void handleOnStartException(final Throwable ex) {
        log.info("Exception during start()", ex);
    }

    @Override
    public void handleOnShutdownException(final Throwable ex) {
        log.info("Exception during shutdown()", ex);
    }
}
