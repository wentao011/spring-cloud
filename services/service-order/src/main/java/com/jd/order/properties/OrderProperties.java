package com.jd.order.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(value = "order")//配置批量绑定在nacos下，可以无需@RefreshScope就能实现自动刷新
@Data
public class OrderProperties {
    String timeout;
    // 这里nacos配置的-分割，会被javabean以驼峰命名方式映射到此处
    String autoConfirm;
    String dbUrl;
}