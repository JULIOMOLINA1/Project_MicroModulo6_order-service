package com.tecsup.app.micro.order.service;

import com.tecsup.app.micro.order.client.Product;
import com.tecsup.app.micro.order.client.ProductClient;
import com.tecsup.app.micro.order.dto.CreateOrderRequest;
import com.tecsup.app.micro.order.dto.OrderItem;
import com.tecsup.app.micro.order.dto.ProductResponse;
import com.tecsup.app.micro.order.entity.OrderItemEntity;
import com.tecsup.app.micro.order.repository.OrderItemRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final ProductClient productClient;

    @Transactional
    public List<OrderItem> createOrderItems(Long orderId, List<CreateOrderRequest.CreateOrderItemRequest> itemsRequest) {
        return itemsRequest.stream()
                .map(itemRequest -> {
                    // Validar producto y obtener información
                    Product product = productClient.getProductById(itemRequest.getProductId());
                    log.info("Creating order item for product: {}", product);

                    // Calcular subtotal
                    BigDecimal unitPrice = product.getPrice();
                    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                            .setScale(2, RoundingMode.HALF_UP);

                    // Crear entity
                    OrderItemEntity itemEntity = new OrderItemEntity();
                    itemEntity.setOrderId(orderId);
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

                    return orderItem;
                })
                .collect(Collectors.toList());
    }

    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        log.info("Getting order items for order id: {}", orderId);

        List<OrderItemEntity> itemEntities = orderItemRepository.findAll()
                .stream()
                .filter(item -> item.getOrderId().equals(orderId))
                .collect(Collectors.toList());

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
