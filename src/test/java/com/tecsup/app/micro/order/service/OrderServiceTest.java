package com.tecsup.app.micro.order.service;

import com.tecsup.app.micro.order.client.Product;
import com.tecsup.app.micro.order.client.ProductClient;
import com.tecsup.app.micro.order.client.User;
import com.tecsup.app.micro.order.client.UserClient;
import com.tecsup.app.micro.order.dto.Order;
import com.tecsup.app.micro.order.entity.OrderEntity;
import com.tecsup.app.micro.order.entity.OrderItemEntity;
import com.tecsup.app.micro.order.repository.OrderItemRepository;
import com.tecsup.app.micro.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    private OrderEntity orderEntity;
    private OrderItemEntity orderItemEntity;
    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        orderEntity = new OrderEntity();
        orderEntity.setId(1L);
        orderEntity.setUserId(1L);
        orderEntity.setOrderNumber("ORD-2025-0001");
        orderEntity.setStatus("PENDING");
        orderEntity.setTotalAmount(new BigDecimal("100.00"));
        orderEntity.setCreatedAt(LocalDateTime.now());
        orderEntity.setUpdatedAt(LocalDateTime.now());

        orderItemEntity = new OrderItemEntity();
        orderItemEntity.setId(1L);
        orderItemEntity.setOrderId(1L);
        orderItemEntity.setProductId(1L);
        orderItemEntity.setQuantity(2);
        orderItemEntity.setUnitPrice(new BigDecimal("50.00"));
        orderItemEntity.setSubtotal(new BigDecimal("100.00"));

        user = new User(1L, "John Doe", "john@example.com", "123456789", "Address 1");
        product = new Product(1L, "Product 1", "Description", new BigDecimal("50.00"), 10, "Cat", 1L);
    }

    @Test
    void getOrderById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderEntity));
        when(userClient.getUserById(1L)).thenReturn(user);
        when(orderItemRepository.findAll()).thenReturn(Collections.singletonList(orderItemEntity));
        when(productClient.getProductById(1L)).thenReturn(product);

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ORD-2025-0001", result.getOrderNumber());
        assertEquals(1, result.getItems().size());
        assertEquals("Product 1", result.getItems().get(0).getProduct().getName());
        
        verify(orderRepository).findById(1L);
        verify(userClient).getUserById(1L);
        verify(productClient).getProductById(1L);
    }

    @Test
    void getOrderById_NotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById(99L);
        });

        assertEquals("Order not found with id: 99", exception.getMessage());
    }
}

