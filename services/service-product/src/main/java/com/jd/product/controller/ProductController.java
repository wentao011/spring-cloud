package com.jd.product.controller;

import com.jd.bean.Product;
import com.jd.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {
    @Autowired
    ProductService productService;

    @GetMapping(value = "/product/{id}")
    public Product getProductById(@PathVariable("id") Long productId) {
        System.out.println("正在远程调用service-product...");
        Product product = productService.getProductById(productId);
        return product;
    }
}