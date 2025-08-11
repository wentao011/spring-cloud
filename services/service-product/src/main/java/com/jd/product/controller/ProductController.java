package com.jd.product.controller;

import com.jd.bean.Product;
import com.jd.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class ProductController {
    @Autowired
    ProductService productService;

    @GetMapping(value = "/product/{id}")
    public Product getProductById(@PathVariable("id") Long productId,
                                  HttpServletRequest httpServletRequest) {
        System.out.println("正在远程调用service-product...");
        String XToken = httpServletRequest.getHeader("X-Token");
        System.out.println("X-Token:" + XToken);
        Product product = productService.getProductById(productId);

        // try {
        //     TimeUnit.SECONDS.sleep(2);
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }

        // int i = 10 / 0;

        return product;
    }
}