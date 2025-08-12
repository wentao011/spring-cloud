package com.jd.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/*
* 此实例作用：GlobalFilter 通常，全局过滤器在网关层 用于 拦截所有请求，
* 而局部过滤器（GatewayFilter）用于 单个路由。
* 案例：请求耗时=请求结束时间-请求进来时间
* */
@Component
@Slf4j
public class RTGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String uri = request.getURI().toString();
        long startTime = System.currentTimeMillis();

        log.info("请求【{}】开始，时间{}", uri, startTime);
        //=============上面是前置逻辑==================

        Mono<Void> filter = chain.filter(exchange).doFinally(r -> {
            long endTime = System.currentTimeMillis();
            log.info("请求【{}】结束，时间{}，耗时：{}ms", uri, endTime, endTime - startTime);
        });

        return filter;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}