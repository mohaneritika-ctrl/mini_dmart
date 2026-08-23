package com.dmart.mapper;

import com.dmart.dto.response.ReturnRequestResponseDto;
import com.dmart.entity.OrderItem;
import com.dmart.entity.Product;
import com.dmart.entity.ReturnRequest;
import com.dmart.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ReturnRequestMapper {

    public ReturnRequestResponseDto toResponseDto(ReturnRequest entity) {
        if (entity == null) {
            return null;
        }

        OrderItem orderItem = entity.getOrderItem();
        Product product = orderItem != null ? orderItem.getProduct() : null;
        User user = entity.getUser();

        return ReturnRequestResponseDto.builder()
                .id(entity.getId())
                .orderId(entity.getOrder() != null ? entity.getOrder().getId() : null)
                .orderItemId(orderItem != null ? orderItem.getId() : null)
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : null)
                .productImageUrl(product != null ? product.getImageUrl() : null)
                .unitPrice(orderItem != null ? orderItem.getPrice() : null)
                .userId(user != null ? user.getId() : null)
                .customerName(user != null ? user.getName() : null)
                .customerEmail(user != null ? user.getEmail() : null)
                .type(entity.getType())
                .quantity(entity.getQuantity())
                .reason(entity.getReason())
                .note(entity.getNote())
                .status(entity.getStatus())
                .staffComment(entity.getStaffComment())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}