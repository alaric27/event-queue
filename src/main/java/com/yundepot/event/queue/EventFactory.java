package com.yundepot.event.queue;

/**
 * @author zhaiyanan
 * @date 2024/6/12  15:37
 */
public interface EventFactory<T> {

    T newInstance();
}
