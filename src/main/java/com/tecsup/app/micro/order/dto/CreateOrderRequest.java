package com.tecsup.app.micro.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    private Long userId;
    private List<CreateOrderItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}
