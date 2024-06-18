package com.yundepot.event.queue.consumer;


import static java.lang.System.Logger;

/**
 * @author zhaiyanan
 * @date 2024/6/17  09:53
 */
public class DefaultExceptionHandler<T> implements ExceptionHandler<T> {
    private static final System.Logger log = System.getLogger(DefaultExceptionHandler.class.getName());
    @Override
    public void handleEventException(final Throwable ex, final long sequence, final Object event) {
        log.log(Logger.Level.INFO, "Exception processing: " + sequence + " " + event, ex);
        throw new RuntimeException(ex);
    }

    @Override
    public void handleOnStartException(final Throwable ex) {
        log.log(Logger.Level.INFO, "Exception during onStart()", ex);
    }

    @Override
    public void handleOnShutdownException(final Throwable ex) {
        log.log(Logger.Level.INFO, "Exception during onShutdown()", ex);
    }
}
