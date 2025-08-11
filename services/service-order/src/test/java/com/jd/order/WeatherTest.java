package com.jd.order;

import com.jd.order.feign.WeatherFeignClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WeatherTest {
    @Autowired
    WeatherFeignClient weatherFeignClient;

    /**
     * 测试天气服务
     * 这个没权限 测试不通 使用下面的在线接口测试
     */
    @Test
    public void test() {
        String weather = weatherFeignClient.getWeather("AppCode 93b7e19861a24c519a7548b17dc16d75",
                                                       "50b53ff8dd7d9fa320d3d3ca32cf8ed1","2182");
        System.out.println("weather = " + weather);
    }

    @Test
    public void testCreate() {
        String weather = weatherFeignClient.create("test_app_key","test_model_name");
        System.out.println("weather = " + weather);
    }
}