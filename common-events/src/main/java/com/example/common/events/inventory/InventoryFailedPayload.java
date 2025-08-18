package com.example.common.events.inventory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class InventoryFailedPayload {
    private String orderId;
    private String reason;
    private String v;

    public InventoryFailedPayload(String orderId, String reason, String v) {
        this.orderId = orderId;
        this.reason = reason;
        this.v = v;
    }
}
