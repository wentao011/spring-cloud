package com.jd.order.feign;

import com.jd.bean.Product;
import com.jd.order.feign.impl.ProductFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-product", fallback = ProductFeignClientFallback.class)
public interface ProductFeignClient {

    @GetMapping(value = "/product/{id}")
    public Product getProductById(@PathVariable("id") Long productId);

}