package com.dmart.mapper;

import com.dmart.dto.response.OrderItemResponseDto;
import com.dmart.dto.response.OrderResponseDto;
import com.dmart.entity.Order;
import com.dmart.entity.OrderItem;
import com.dmart.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderMapper {

    public OrderItemResponseDto toItemResponseDto(OrderItem item) {
        if (item == null) {
            return null;
        }

        Product product = item.getProduct();
        BigDecimal unitPrice = item.getPrice() != null
                ? item.getPrice()
                : (product != null ? product.getPrice() : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

        int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);

        return OrderItemResponseDto.builder()
                .id(item.getId())
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : null)
                .imageUrl(product != null ? product.getImageUrl() : null)
                .unitPrice(unitPrice)
                .quantity(quantity)
                .subtotal(subtotal)
                .build();
    }

    public OrderResponseDto toResponseDto(Order order, List<OrderItem> items) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponseDto> itemDtos = new ArrayList<>();
        if (items != null) {
            for (OrderItem item : items) {
                OrderItemResponseDto itemDto = toItemResponseDto(item);
                if (itemDto != null) {
                    itemDtos.add(itemDto);
                }
            }
        }

        return OrderResponseDto.builder()
                .id(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .totalAmount(order.getTotalAmount())
                .orderType(order.getOrderType())
                .orderStatus(order.getOrderStatus())
                .pickupDate(order.getPickupDate())
                .pickupTimeSlot(order.getPickupTimeSlot())
                .deliveryAddress(order.getDeliveryAddress())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}