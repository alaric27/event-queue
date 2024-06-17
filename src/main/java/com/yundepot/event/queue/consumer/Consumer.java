package com.yundepot.event.queue.consumer;

import com.yundepot.event.queue.common.Sequence;


/**
 * @author zhaiyanan
 * @date 2024/6/14  16:47
 */
public interface Consumer {

    /**
     * 获取消费者进度
     */
    Sequence getSequence();

    /**
     * 启动
     */
    void start();

    /**
     *  停止
     */
    void showdown();
}
