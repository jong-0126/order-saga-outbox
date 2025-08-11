package com.example.ordersagaoutbox.domain.order.entity;

public enum OrderStatus {
    NEW,                // 주문 생성됨
    INVENTORY_RESERVED, // 재고 예약 완료
    PAYMENT_AUTHORIZED, // 결제 승인 완료
    COMPLETED,          // 주문 완료
    CANCELLED           // 실패로 취소(보상 수행)
}
