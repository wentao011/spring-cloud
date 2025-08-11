package com.jd.order.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.jd.bean.Order;
import com.jd.bean.Product;
import com.jd.order.feign.ProductFeignClient;
import com.jd.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    DiscoveryClient discoveryClient;
    // 阶段1 restTemplate
    @Autowired
    RestTemplate restTemplate;
    /**
     * 阶段2 使用loadBalancerClient-负载均衡 改造
     */
    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    ProductFeignClient productFeignClient;

    @SentinelResource(value = "createOrder", blockHandler = "createOrderFallback")
    @Override
    public Order createOrder(Long userId, Long productId) {
        // 阶段1
        // Product product = getProductFromRemote(productId);

        // 阶段2
        // Product product = this.getProductFromRemoteWithLoadBalance(productId);

        // 阶段3
        // Product product = this.getProductFromRemoteWithLoadBalanceAnnotation(productId);

        // 阶段4 使用feign接口的形式调用
        Product product = productFeignClient.getProductById(productId);
        Order order = new Order();
        order.setId(1L);
        // 总金额=价格*数量
        BigDecimal price = product.getPrice();//价格
        int num = product.getNum();//数量
        order.setTotalAmount(price.multiply(new BigDecimal(num)));//总价
        order.setUserId(userId);
        order.setNickName("张三");
        order.setAddress("火星");
        // 远程查询商品列表
        order.setProductList(Arrays.asList(product));
        return order;
    }

    public Order createOrderFallback(Long userId, Long productId, BlockException e) {
        Order order = new Order();
        order.setId(0L);
        order.setTotalAmount(new BigDecimal("0"));
        order.setUserId(userId);
        order.setNickName("未知用户");
        order.setAddress("异常信息" + e.getClass());
        return order;
    }

    //阶段三：于注解的负载均衡
    public Product getProductFromRemoteWithLoadBalanceAnnotation(Long productId) {
        // 给远程发送请求；；service-product会被动态替换
        // 和方案1区别为：此处拼接的ip和端口号被具体的服务名称代替，动态负载均衡
        String url = "http://service-product/product/" + productId;
        Product product = restTemplate.getForObject(url, Product.class);
        return product;
    }

    //阶段二：加入负载均衡
    public Product getProductFromRemoteWithLoadBalance(Long productId) {
        //1、获取到商品服务所在的所有机器IP+port
        ServiceInstance choose = loadBalancerClient.choose("service-product");
        //远程URL
        String url = "http://" + choose.getHost() + ":" + choose.getPort() + "/product/" + productId;
        log.info("远程请求：{}", url);
        //2、给远程发送请求
        Product product = restTemplate.getForObject(url, Product.class);
        return product;
    }


    //远程调用获取商品信息
    public Product getProductFromRemote(Long productId) {
        //1、获取到商品服务所在的所有机器IP+port
        List<ServiceInstance> instances = discoveryClient.getInstances("service-product");
        ServiceInstance instance = instances.get(0);
        //远程URL
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/product/" + productId;
        log.info("远程请求：{}", url);
        //2、给远程发送请求
        Product product = restTemplate.getForObject(url, Product.class);
        return product;
    }
}