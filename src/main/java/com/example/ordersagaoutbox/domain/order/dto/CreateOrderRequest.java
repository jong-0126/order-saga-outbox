package com.example.ordersagaoutbox.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateOrderRequest {
    @NotBlank private String productId;
    @Min(1) private int quantity;
    @Min(0) private long amount;
}
