package com.dmart.dto.response;

import com.dmart.entity.ReturnStatus;
import com.dmart.entity.ReturnType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnRequestResponseDto {

    private Long id;
    private Long orderId;
    private Long orderItemId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal unitPrice;
    private Long userId;
    private String customerName;
    private String customerEmail;
    private ReturnType type;
    private Integer quantity;
    private String reason;
    private String note;
    private ReturnStatus status;
    private String staffComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}