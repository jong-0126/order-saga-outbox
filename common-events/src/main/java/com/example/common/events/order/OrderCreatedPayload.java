package com.example.common.events.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class OrderCreatedPayload {
    private String orderId;
    private String productId;
    private String v;
    private int quantity;
    private  long amount;

    public OrderCreatedPayload(String orderId, String productId,  int quantity, long amount, String v) {
        this.orderId = orderId;
        this.productId = productId;
        this.v = v;
        this.quantity = quantity;
        this.amount = amount;
    }
}
