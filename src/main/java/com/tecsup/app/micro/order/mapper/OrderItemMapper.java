package com.tecsup.app.micro.order.mapper;

import com.tecsup.app.micro.order.dto.OrderItem;
import com.tecsup.app.micro.order.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderItemMapper {
    OrderItemMapper INSTANCE = Mappers.getMapper(OrderItemMapper.class);

    OrderItemEntity toEntity(OrderItem orderItem);
    OrderItem toDTO(OrderItemEntity entity);
}
