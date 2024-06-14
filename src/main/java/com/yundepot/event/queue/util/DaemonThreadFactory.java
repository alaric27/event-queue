package com.yundepot.event.queue.util;

import java.util.concurrent.ThreadFactory;

/**
 * @author zhaiyanan
 * @date 2024/6/12  17:20
 */
public enum DaemonThreadFactory implements ThreadFactory {

    INSTANCE;

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    }
}
