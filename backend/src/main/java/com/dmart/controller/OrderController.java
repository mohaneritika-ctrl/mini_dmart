package com.dmart.controller;

import com.dmart.dto.request.CheckoutRequestDto;
import com.dmart.dto.response.OrderResponseDto;
import com.dmart.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponseDto> checkout(@RequestBody(required = false) CheckoutRequestDto dto) {
        OrderResponseDto response = orderService.checkout(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getMyOrders() {
        List<OrderResponseDto> response = orderService.getMyOrders();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getMyOrderById(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.getMyOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelMyOrder(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.cancelMyOrder(orderId);
        return ResponseEntity.ok(response);
    }
}