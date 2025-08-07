package com.jd.product;

import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

@SpringBootTest
public class ProductApplicationTest {
    @Autowired
    DiscoveryClient discoveryClient;
    @Autowired
    NacosDiscoveryClient nacosDiscoveryClient;//二者效果一样，这个依赖nacos

    @Test
    public void discoveryClientTest(){
        List<String> services = discoveryClient.getServices();
        for (String service : services) {
            System.out.println("service = " + service);
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            for (ServiceInstance instance : instances) {
                System.out.println("instance.getHost() = " + instance.getHost());
                System.out.println("instance.getPort() = " + instance.getPort());
            }
        }
    }

    @Test
    public void nacosDiscoveryClientTest(){
        List<String> services = nacosDiscoveryClient.getServices();
        for (String service : services) {
            System.out.println("service = " + service);
            List<ServiceInstance> instances = nacosDiscoveryClient.getInstances(service);
            for (ServiceInstance instance : instances) {
                System.out.println("instance.getHost() = " + instance.getHost());
                System.out.println("instance.getPort() = " + instance.getPort());
            }
        }
    }
}