package com.jd.order.config;

import feign.Logger;
import feign.Retryer;
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

    /**
     *  开启全量日志记录
     * @author -
     * @date 2025/8/10 21:33
     * @return Level
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    // @Bean
    Retryer retryer() {
        return new Retryer.Default();
    }
}