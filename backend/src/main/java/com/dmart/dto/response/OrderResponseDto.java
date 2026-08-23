package com.dmart.dto.response;

import com.dmart.entity.OrderStatus;
import com.dmart.entity.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private Long id;
    private Long userId;
    private BigDecimal totalAmount;
    private OrderType orderType;
    private OrderStatus orderStatus;
    private LocalDate pickupDate;
    private String pickupTimeSlot;
    private String deliveryAddress;
    private List<OrderItemResponseDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}