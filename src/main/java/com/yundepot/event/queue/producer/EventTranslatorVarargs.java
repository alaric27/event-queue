package com.yundepot.event.queue.producer;

/**
 * @author zhaiyanan
 * @date 2024/6/19  09:49
 */
public interface EventTranslatorVarargs<T> {
    void translateTo(T event, long sequence, Object... args);
}
