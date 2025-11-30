package com.tecsup.app.micro.order.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private Long createdBy;

}