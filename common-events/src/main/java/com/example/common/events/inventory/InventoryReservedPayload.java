package com.example.common.events.inventory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class InventoryReservedPayload {
    private String orderId;
    private String productId;
    private String v;
    private int qty;

    public InventoryReservedPayload(String orderId, String productId, int qty, String v) {
        this.orderId = orderId;
        this.productId = productId;
        this.v = v;
        this.qty = qty;
    }
}
