package com.jd.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.jd.bean.Order;
import com.jd.order.properties.OrderProperties;
import com.jd.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// @RefreshScope + @Value可以实现配置动态刷新
// @RefreshScope
@Slf4j
@RestController
@RequestMapping()
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

    @GetMapping("/writeDb")
    public String writeDb() {
        return "writeDb success...";
    }

    @GetMapping("/readDb")
    public String readDb() {
        log.info("readDb success...");
        return "readDb success...";
    }

    // @GetMapping("/seckill")
    // public Order seckill(@RequestParam("userId") Long userId,
    //                      @RequestParam("productId") Long productId) {
    //     Order order = orderService.createOrder(userId, productId);
    //     order.setId(Long.MAX_VALUE);
    //     return order;
    // }

    @GetMapping("/seckill")
    @SentinelResource(value = "seckill-order", fallback = "seckillFallback") //此处用于测试热点规则
   // public Order seckill(@RequestParam("userId") Long userId,
   //                      @RequestParam("productId") Long productId) {
//此处模拟不传参数，给默认参数
//    public Order seckill(@RequestParam(value = "userId",defaultValue = "888") Long userId,
//                         @RequestParam(value = "productId",defaultValue = "1000") Long productId) {
    public Order seckill(@RequestParam(value = "userId", required = false) Long userId,
                         @RequestParam(value = "productId", required = false) Long productId) {
        Order order = orderService.createOrder(userId, productId);
        order.setId(Long.MAX_VALUE);
        return order;
    }

    public Order seckillFallback(Long userId, Long productId, Throwable exception) {
        System.out.println("seckillFallback...");
        Order order = new Order();
        order.setId(productId);
        order.setUserId(userId);
        order.setAddress("seckillFallback异常信息" + exception.getClass());
        return order;
    }
}