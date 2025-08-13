package com.example.ordersagaoutbox.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class CreateOrderRequest {
    @NotBlank
    private String productId;

    @Min(1)
    private int quantity;

    @Min(1)
    private long amount;
}
