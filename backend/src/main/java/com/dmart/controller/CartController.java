package com.dmart.controller;

import com.dmart.dto.request.AddToCartRequestDto;
import com.dmart.dto.request.UpdateCartItemRequestDto;
import com.dmart.dto.response.CartResponseDto;
import com.dmart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDto> getMyCart() {
        CartResponseDto response = cartService.getMyCart();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addToCart(@Valid @RequestBody AddToCartRequestDto dto) {
        CartResponseDto response = cartService.addToCart(dto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDto> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequestDto dto
    ) {
        CartResponseDto response = cartService.updateCartItem(cartItemId, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDto> removeCartItem(@PathVariable Long cartItemId) {
        CartResponseDto response = cartService.removeCartItem(cartItemId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<CartResponseDto> clearCart() {
        CartResponseDto response = cartService.clearCart();
        return ResponseEntity.ok(response);
    }
}