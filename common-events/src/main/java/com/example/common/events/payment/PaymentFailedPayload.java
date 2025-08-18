package com.example.common.events.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class PaymentFailedPayload {
    private String orderId, reason, v;

    public PaymentFailedPayload(String orderId, String reason, String v){
        this.orderId = orderId;
        this.reason = reason;
        this.v = v;
    }

}
