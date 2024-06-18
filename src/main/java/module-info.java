/**
 * @author zhaiyanan
 * @date 2024/6/12  15:34
 */
module com.yundopot.event.queue {
    requires static lombok;
    requires org.slf4j;
    exports com.yundepot.event.queue;
    exports com.yundepot.event.queue.producer;
    exports com.yundepot.event.queue.consumer;
    exports com.yundepot.event.queue.broker;
    exports com.yundepot.event.queue.common;
    exports com.yundepot.event.queue.consumer.waitstrategy;
}