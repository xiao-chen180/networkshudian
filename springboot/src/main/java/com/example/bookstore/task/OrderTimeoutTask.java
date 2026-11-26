package com.example.bookstore.task;

import com.example.bookstore.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时任务：定期关闭超过支付时限仍未付款的订单。
 * 支付时限与执行间隔均可在 application.yml 的 bookstore.order-timeout 下调整。
 */
@Component
public class OrderTimeoutTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class);

    private final OrderService orderService;

    @Value("${bookstore.order-timeout.minutes:30}")
    private int timeoutMinutes;

    public OrderTimeoutTask(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 默认每 60 秒扫描一次；取消状态流转、库存回滚与通知都在服务层事务内完成 */
    @Scheduled(
            initialDelayString = "${bookstore.order-timeout.initial-delay:30000}",
            fixedDelayString = "${bookstore.order-timeout.task-interval:60000}"
    )
    public void closeExpiredOrders() {
        log.debug("扫描待支付订单，超时阈值 {} 分钟，当前时间 {}", timeoutMinutes, java.time.LocalDateTime.now());
        try {
            int closed = orderService.closeExpiredOrders(timeoutMinutes);
            if (closed > 0) {
                log.info("订单超时任务：已关闭 {} 笔超过 {} 分钟未支付的订单", closed, timeoutMinutes);
            }
        } catch (Exception e) {
            // 定时任务异常不能中断后续调度
            log.error("订单超时任务执行失败", e);
        }
    }
}