package com.jd.order.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

// @Component
public class XTokenRequestIntercepter implements RequestInterceptor {
    /** feign接口请求拦截器 此处为全局拦截器
     * @param requestTemplate
     * @return void
     * @author -
     * @date 2025/8/10 21:37
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("X-Token", UUID.randomUUID().toString());
    }
}