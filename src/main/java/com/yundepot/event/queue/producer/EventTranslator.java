package com.yundepot.event.queue.producer;

/**
 * @author zhaiyanan
 * @date 2024/6/17  16:08
 */
public interface EventTranslator<T> {
    void translateTo(T event, long sequence);
}
