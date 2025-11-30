package com.tecsup.app.micro.order.mapper;

import com.tecsup.app.micro.order.dto.Order;
import com.tecsup.app.micro.order.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    OrderEntity toEntity(Order order);
    Order toDTO(OrderEntity entity);
}
