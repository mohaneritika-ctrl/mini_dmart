package com.dmart.controller;

import com.dmart.dto.request.CreateReturnRequestDto;
import com.dmart.dto.response.ReturnRequestResponseDto;
import com.dmart.service.ReturnRequestService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/returns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;

    @PostMapping
    public ResponseEntity<ReturnRequestResponseDto> createRequest(@Valid @RequestBody CreateReturnRequestDto dto) {
        ReturnRequestResponseDto response = returnRequestService.createRequest(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReturnRequestResponseDto>> getMyRequests() {
        List<ReturnRequestResponseDto> response = returnRequestService.getMyRequests();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnRequestResponseDto> getMyRequestById(@PathVariable Long id) {
        ReturnRequestResponseDto response = returnRequestService.getMyRequestById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReturnRequestResponseDto> cancelMyRequest(@PathVariable Long id) {
        ReturnRequestResponseDto response = returnRequestService.cancelMyRequest(id);
        return ResponseEntity.ok(response);
    }
}