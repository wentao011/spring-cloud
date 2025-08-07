package com.jd.order.controller;

import com.jd.bean.Order;
import com.jd.order.properties.OrderProperties;
import com.jd.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// @RefreshScope + @Value可以实现配置动态刷新
// @RefreshScope
@RestController
public class OrderController {
    @Autowired
    OrderService orderService;

    // @Value("${order.timeout}")
    // String orderTimeout;
    //
    // @Value("${order.auto-confirm}")
    // String orderAutoConfirm;

    @Autowired
    OrderProperties orderProperties;

    @GetMapping("/config")
    public String getConfig() {
        // @RefreshScope + @Value可以实现配置动态刷新(方法1)
        // return "OrderTimeout+OrderAutoConfirm = " + orderTimeout+" : " + orderAutoConfirm;
        // @ConfigurationProperties(value = "order")配置类形式（方法2）
        // return "OrderTimeout+OrderAutoConfirm = " + orderProperties.getTimeout() + " : " + orderProperties.getAutoConfirm();
        return "OrderTimeout+OrderAutoConfirm = " + orderProperties.getTimeout() + " : " + orderProperties.getAutoConfirm()
                + " : " + orderProperties.getDbUrl();
    }

    @GetMapping(value = "/create")
    public Order createOrder(@RequestParam("userId") Long userId, @RequestParam("productId") Long productId) {
        Order order = orderService.createOrder(userId, productId);
        return order;
    }
}