package com.dmart.service;

import com.dmart.dto.request.CheckoutRequestDto;
import com.dmart.dto.request.UpdateOrderStatusRequestDto;
import com.dmart.dto.response.OrderResponseDto;
import com.dmart.entity.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponseDto checkout(CheckoutRequestDto dto);

    List<OrderResponseDto> getMyOrders();

    OrderResponseDto getMyOrderById(Long orderId);

    OrderResponseDto cancelMyOrder(Long orderId);

    List<OrderResponseDto> getAllOrders(OrderStatus status);

    OrderResponseDto getOrderByIdForStaff(Long orderId);

    OrderResponseDto updateOrderStatus(Long orderId, UpdateOrderStatusRequestDto dto);
}