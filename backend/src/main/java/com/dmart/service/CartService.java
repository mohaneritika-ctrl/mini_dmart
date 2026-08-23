package com.dmart.service;

import com.dmart.dto.request.AddToCartRequestDto;
import com.dmart.dto.request.UpdateCartItemRequestDto;
import com.dmart.dto.response.CartResponseDto;

public interface CartService {

    CartResponseDto getMyCart();

    CartResponseDto addToCart(AddToCartRequestDto dto);

    CartResponseDto updateCartItem(Long cartItemId, UpdateCartItemRequestDto dto);

    CartResponseDto removeCartItem(Long cartItemId);

    CartResponseDto clearCart();
}