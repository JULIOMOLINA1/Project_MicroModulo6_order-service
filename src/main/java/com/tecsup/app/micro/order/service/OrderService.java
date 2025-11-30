package com.tecsup.app.micro.order.service;

import com.tecsup.app.micro.order.client.Product;
import com.tecsup.app.micro.order.client.ProductClient;
import com.tecsup.app.micro.order.client.User;
import com.tecsup.app.micro.order.client.UserClient;
import com.tecsup.app.micro.order.dto.*;
import com.tecsup.app.micro.order.dto.ProductResponse;
import com.tecsup.app.micro.order.dto.UserResponse;
import com.tecsup.app.micro.order.entity.OrderEntity;
import com.tecsup.app.micro.order.entity.OrderItemEntity;
import com.tecsup.app.micro.order.repository.OrderItemRepository;
import com.tecsup.app.micro.order.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserClient userClient;
    private final ProductClient productClient;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());

        // Validar usuario
        User user = userClient.getUserById(request.getUserId());
        log.info("User validated: {}", user);

        // Calcular totales de los items
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // Crear la orden
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserId(request.getUserId());
        orderEntity.setStatus("PENDING");
        orderEntity.setTotalAmount(totalAmount);
        orderEntity.setCreatedAt(LocalDateTime.now());
        orderEntity.setUpdatedAt(LocalDateTime.now());
        orderEntity.setOrderNumber("TEMP-" + UUID.randomUUID().toString()); // Valor temporal único

        // Guardar para obtener el ID
        orderEntity = orderRepository.save(orderEntity);
        log.info("Order saved with id: {}", orderEntity.getId());

        // Generar y actualizar con el número basado en el ID
        orderEntity.setOrderNumber(generateOrderNumber(orderEntity.getId()));
        orderEntity = orderRepository.save(orderEntity);

        // Crear los items de la orden
        for (CreateOrderRequest.CreateOrderItemRequest itemRequest : request.getItems()) {
            // Validar producto y obtener información
            Product product = productClient.getProductById(itemRequest.getProductId());
            log.info("Product validated: {}", product);

            // Calcular subtotal
            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            totalAmount = totalAmount.add(subtotal);

            // Crear entity
            OrderItemEntity itemEntity = new OrderItemEntity();
            itemEntity.setOrderId(orderEntity.getId());
            itemEntity.setProductId(itemRequest.getProductId());
            itemEntity.setQuantity(itemRequest.getQuantity());
            itemEntity.setUnitPrice(unitPrice);
            itemEntity.setSubtotal(subtotal);

            itemEntity = orderItemRepository.save(itemEntity);
            log.info("Order item saved with id: {}", itemEntity.getId());

            // Construir DTO
            OrderItem orderItem = new OrderItem();
            orderItem.setId(itemEntity.getId());
            ProductResponse productResponse = new ProductResponse(product.getId(), product.getName(), product.getPrice());
            orderItem.setProduct(productResponse);
            orderItem.setQuantity(itemEntity.getQuantity());
            orderItem.setUnitPrice(itemEntity.getUnitPrice());
            orderItem.setSubtotal(itemEntity.getSubtotal());

            orderItems.add(orderItem);
        }

        // Actualizar total de la orden
        orderEntity.setTotalAmount(totalAmount);
        orderRepository.save(orderEntity);

        // Construir la respuesta
        Order order = new Order();
        order.setId(orderEntity.getId());
        order.setOrderNumber(orderEntity.getOrderNumber());
        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getEmail());
        order.setUser(userResponse);
        order.setItems(orderItems);
        order.setTotalAmount(orderEntity.getTotalAmount());
        order.setStatus(orderEntity.getStatus());
        order.setCreatedAt(orderEntity.getCreatedAt());
        order.setUpdatedAt(orderEntity.getUpdatedAt());

        return order;
    }

    public Order getOrderById(Long id) {
        log.info("Getting order by id: {}", id);

        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        // Obtener usuario
        User user = userClient.getUserById(orderEntity.getUserId());

        // Obtener items
        List<OrderItem> items = getOrderItemsByOrderId(id);

        // Construir la respuesta
        Order order = new Order();
        order.setId(orderEntity.getId());
        order.setOrderNumber(orderEntity.getOrderNumber());
        UserResponse userResponse = new UserResponse(user.getId(), user.getName(), user.getEmail());
        order.setUser(userResponse);
        order.setItems(items);
        order.setTotalAmount(orderEntity.getTotalAmount());
        order.setStatus(orderEntity.getStatus());
        order.setCreatedAt(orderEntity.getCreatedAt());
        order.setUpdatedAt(orderEntity.getUpdatedAt());

        return order;
    }

    private String generateOrderNumber(Long orderId) {
        return "ORD-2025-" + String.format("%04d", orderId);
    }

    private List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        log.info("Getting order items for order id: {}", orderId);

        List<OrderItemEntity> itemEntities = orderItemRepository.findAll()
                .stream()
                .filter(item -> item.getOrderId().equals(orderId))
                .toList();

        return itemEntities.stream()
                .map(entity -> {
                    Product product = productClient.getProductById(entity.getProductId());
                    ProductResponse productResponse = new ProductResponse(product.getId(), product.getName(), product.getPrice());

                    OrderItem orderItem = new OrderItem();
                    orderItem.setId(entity.getId());
                    orderItem.setProduct(productResponse);
                    orderItem.setQuantity(entity.getQuantity());
                    orderItem.setUnitPrice(entity.getUnitPrice());
                    orderItem.setSubtotal(entity.getSubtotal());

                    return orderItem;
                })
                .collect(Collectors.toList());
    }
}
