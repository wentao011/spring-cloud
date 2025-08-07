package com.jd.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

@SpringBootTest
public class OrderApplicationTest {
    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Test
    public void test() {
        ServiceInstance choose = loadBalancerClient.choose("service-product");
        System.out.println("choose.getHost()+choose.getPort() = " + choose.getHost() + ":" + choose.getPort());
        choose = loadBalancerClient.choose("service-product");
        System.out.println("choose.getHost()+choose.getPort() = " + choose.getHost() + ":" + choose.getPort());
        choose = loadBalancerClient.choose("service-product");
        System.out.println("choose.getHost()+choose.getPort() = " + choose.getHost() + ":" + choose.getPort());
        choose = loadBalancerClient.choose("service-product");
        System.out.println("choose.getHost()+choose.getPort() = " + choose.getHost() + ":" + choose.getPort());
        choose = loadBalancerClient.choose("service-product");
        System.out.println("choose.getHost()+choose.getPort() = " + choose.getHost() + ":" + choose.getPort());
    }
}