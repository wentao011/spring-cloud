package com.jd.order.service;

import com.jd.bean.Order;

public interface OrderService {
    Order createOrder(Long userId, Long productId);
}