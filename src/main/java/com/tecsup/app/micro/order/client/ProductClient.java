package com.tecsup.app.micro.order.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductClient {

    private final RestTemplate restTemplate;

    @Value("${product.service.url}")
    private String productServiceUrl;

    @CircuitBreaker(name = "productService",
            fallbackMethod = "getProductByIdFallback")
    public Product getProductById(Long productId) {
        String url = productServiceUrl + "/api/products/" + productId;
        log.info("Calling Product Service at: {}", url);

        try {
            Product product = restTemplate.getForObject(url, Product.class);
            if (product == null) {
                log.error("Product Service returned null for product id: {}", productId);
                throw new RuntimeException("Product not found with id: " + productId);
            }
            log.info("Product retrieved successfully: {}", product);
            return product;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("HTTP error calling Product Service for id {}: Status {} - {}", productId, e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Product not found with id: " + productId, e);
        } catch (Exception e) {
            log.error("Unexpected error calling Product Service for id {}: {}", productId, e.getMessage(), e);
            throw new RuntimeException("Error calling Product Service: " + e.getMessage(), e);
        }
    }
    private Product getProductByIdFallback(Long ProductId, Throwable throwable) {
        log.warn("Fallback method invoked for getProductById due to: {}", throwable.getMessage());
        return Product.builder()
                .id(ProductId)
                .name("Unknown product")
                .price(BigDecimal.ZERO)
                .build();
    }

}