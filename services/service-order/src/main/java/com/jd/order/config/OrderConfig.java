package com.jd.order.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OrderConfig {
    @LoadBalanced  // 基于注解式的负载均衡，加了此注解，RestTemplate自带负载均衡
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}