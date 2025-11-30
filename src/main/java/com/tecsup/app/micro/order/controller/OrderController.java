package com.tecsup.app.micro.order.controller;

import com.tecsup.app.micro.order.dto.CreateOrderRequest;
import com.tecsup.app.micro.order.dto.Order;
import com.tecsup.app.micro.order.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("REST request to create order for user: {}", request.getUserId());
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        log.info("REST request to get order by id: {}", id);
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
}