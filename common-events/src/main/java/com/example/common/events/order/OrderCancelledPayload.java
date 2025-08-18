package com.example.common.events.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class OrderCancelledPayload {
    private String orderId, reason, v;

    public OrderCancelledPayload(String orderId, String reason, String v) {
        this.orderId = orderId;
        this.reason = reason;
        this.v = v;
    }
}
