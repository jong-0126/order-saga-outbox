package com.example.common.events.payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class PaymentAuthorizedPayload {
    private String orderId;
    private String v;
    private long amount;

    public PaymentAuthorizedPayload(String orderId, long amount, String v){
        this.orderId=orderId;
        this.amount=amount;
        this.v=v;
    }
}
