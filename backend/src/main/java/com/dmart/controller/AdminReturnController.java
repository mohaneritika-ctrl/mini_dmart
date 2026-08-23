package com.dmart.controller;

import com.dmart.dto.request.UpdateReturnStatusDto;
import com.dmart.dto.response.ReturnRequestResponseDto;
import com.dmart.entity.ReturnStatus;
import com.dmart.entity.ReturnType;
import com.dmart.service.ReturnRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/returns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReturnController {

    private final ReturnRequestService returnRequestService;

    @GetMapping
    public ResponseEntity<List<ReturnRequestResponseDto>> getAllRequests(
            @RequestParam(required = false) ReturnStatus status,
            @RequestParam(required = false) ReturnType type
    ) {
        List<ReturnRequestResponseDto> response = returnRequestService.getAllRequests(status, type);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnRequestResponseDto> getRequestById(@PathVariable Long id) {
        ReturnRequestResponseDto response = returnRequestService.getRequestById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReturnRequestResponseDto> updateRequestStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReturnStatusDto dto
    ) {
        ReturnRequestResponseDto response = returnRequestService.updateRequestStatus(id, dto);
        return ResponseEntity.ok(response);
    }
}