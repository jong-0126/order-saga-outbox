package com.example.common.events;
public final class EventTypes {
    public static final String ORDER_CREATED = "OrderCreated";
    public static final String INVENTORY_RESERVED = "InventoryReserved";
    public static final String INVENTORY_FAILED = "InventoryFailed";
    public static final String PAYMENT_AUTHORIZED = "PaymentAuthorized";
    public static final String PAYMENT_FAILED = "PaymentFailed";
    public static final String ORDER_CANCELLED = "OrderCancelled";
    private EventTypes(){}
}
