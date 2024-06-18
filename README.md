# event-queue
基于事件驱动的内存级消息队列  
为了学习disruptor，所以写了这个项目。  
本项目大量参考和直接使用disruptor源码   

由于disruptor 相关概念难以理解，且各组件直接依赖关系复杂  
本项目抽象了下面几个组件：  
## producer 
生产者，用于消息发送

## consumer
消费者，用于消息接收

## broker
协调者，用于消息存储及协调生产者和消费者


# 记录
1. EventHandler.onEvent
EventHandler.onEvent 要合理处理异常，若果抛出异常，默认停止消费者; 
如果需要重试或者忽略，需要用户在eventHandler.onEvent 中自己实现

2. 发布
获取完sequence，一定要发布，不然消费者会一直等待可消费的序列

# 问题