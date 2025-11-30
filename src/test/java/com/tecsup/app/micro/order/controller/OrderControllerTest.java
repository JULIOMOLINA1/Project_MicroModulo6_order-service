package com.tecsup.app.micro.order.controller;

import com.tecsup.app.micro.order.dto.Order;
import com.tecsup.app.micro.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private Order orderDto;

    @BeforeEach
    void setUp() {
        orderDto = new Order();
        orderDto.setId(1L);
        orderDto.setOrderNumber("ORD-2025-0001");
        orderDto.setTotalAmount(new BigDecimal("100.00"));
        orderDto.setStatus("PENDING");
    }

    @Test
    void getOrderById_Success() throws Exception {
        given(orderService.getOrderById(1L)).willReturn(orderDto);

        mockMvc.perform(get("/api/orders/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderNumber").value("ORD-2025-0001"));
    }
}

