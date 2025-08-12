package com.example.ordersagaoutbox.domain.saga;

import com.example.ordersagaoutbox.domain.inventory.service.InventoryService;
import com.example.ordersagaoutbox.domain.order.dto.CreateOrderRequest;
import com.example.ordersagaoutbox.domain.order.entity.OrderEntity;
import com.example.ordersagaoutbox.domain.order.entity.OrderStatus;
import com.example.ordersagaoutbox.domain.order.service.OrderService;
import com.example.ordersagaoutbox.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SagaOrchestrator {
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;

    public OrderEntity placeOrder(CreateOrderRequest req, String idemKey){
        // 주문 생성(멱등)
        OrderEntity order = orderService.createIfAbsent(req, idemKey);

        // 이미 진행된 주문이면 현재 상태 반환
        if (order.getStatus() != OrderStatus.NEW) return order;

        try {
            // 1) 재고 예약
            inventoryService.reserve(order.getProductId(), order.getQuantity());
            orderService.transition(order.getId(), OrderStatus.INVENTORY_RESERVED, "InventoryReserved",
                Map.of("orderId", order.getId(), "productId", order.getProductId(), "qty", order.getQuantity()));

            // 2) 결제 승인
            paymentService.authorize(order.getId(), order.getAmount());
            orderService.transition(order.getId(), OrderStatus.PAYMENT_AUTHORIZED, "PaymentAuthorized",
                Map.of("orderId", order.getId(), "amount", order.getAmount()));

            // 3) 완료
            orderService.transition(order.getId(), OrderStatus.COMPLETED, "OrderCompleted",
                Map.of("orderId", order.getId()));
            return order;

        } catch (Exception ex) {
            try { inventoryService.release(order.getProductId(), order.getQuantity()); } catch (Exception ignore) {}
            orderService.transition(order.getId(), OrderStatus.CANCELLED, "OrderCancelled",
                Map.of("orderId", order.getId(), "reason", ex.getMessage()));
            throw ex;
        }
    }
}
