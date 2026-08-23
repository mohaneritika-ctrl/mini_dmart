package com.dmart.mapper;

import com.dmart.dto.response.CartItemResponseDto;
import com.dmart.dto.response.CartResponseDto;
import com.dmart.entity.Cart;
import com.dmart.entity.CartItem;
import com.dmart.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class CartMapper {

    public CartItemResponseDto toItemResponseDto(CartItem item) {
        if (item == null) {
            return null;
        }

        Product product = item.getProduct();
        BigDecimal price = product != null && product.getPrice() != null
                ? product.getPrice()
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);

        return CartItemResponseDto.builder()
                .id(item.getId())
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : null)
                .imageUrl(product != null ? product.getImageUrl() : null)
                .price(price)
                .quantity(quantity)
                .subtotal(subtotal)
                .availableStock(product != null ? product.getStock() : 0)
                .active(product != null ? product.getActive() : false)
                .build();
    }

    public CartResponseDto toResponseDto(Cart cart, List<CartItem> items) {
        if (cart == null) {
            return null;
        }

        List<CartItemResponseDto> itemDtos = new ArrayList<>();
        int totalItems = 0;
        BigDecimal totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        if (items != null) {
            for (CartItem item : items) {
                CartItemResponseDto itemDto = toItemResponseDto(item);
                if (itemDto != null) {
                    itemDtos.add(itemDto);
                    totalItems += itemDto.getQuantity();
                    totalAmount = totalAmount.add(itemDto.getSubtotal());
                }
            }
        }

        return CartResponseDto.builder()
                .cartId(cart.getId())
                .userId(cart.getUser() != null ? cart.getUser().getId() : null)
                .items(itemDtos)
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .build();
    }
}