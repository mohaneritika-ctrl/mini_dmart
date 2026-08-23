package com.dmart.dto.request;

import com.dmart.entity.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequestDto {

    @Builder.Default
    private OrderType orderType = OrderType.PICKUP;

    private LocalDate pickupDate;
    private String pickupTimeSlot;
    private String deliveryAddress;
}