package com.example.ordersagaoutbox.domain.saga;

import com.example.ordersagaoutbox.domain.inventory.service.InventoryService;
import com.example.ordersagaoutbox.domain.order.dto.CreateOrderRequest;
import com.example.ordersagaoutbox.domain.order.entity.OrderEntity;
import com.example.ordersagaoutbox.domain.order.entity.OrderStatus;
import com.example.ordersagaoutbox.domain.order.service.OrderService;
import com.example.ordersagaoutbox.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SagaOrchestrator {
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;


    public OrderEntity placeOrder(CreateOrderRequest req, String idemKey){
        // 1) 주문 생성(멱등)
        OrderEntity order = orderService.createIfAbsent(req, idemKey);

        // 이미 진행된 주문이면 현재 상태 반환
        if (order.getStatus() != OrderStatus.NEW) return order;

        try {
            // 2) 재고 예약
            inventoryService.reserve(order.getProductId(), order.getQuantity());
            orderService.markStatus(order.getId(), OrderStatus.INVENTORY_RESERVED);

            // 3) 결제 승인
            paymentService.authorize(order.getId(), order.getAmount());
            orderService.markStatus(order.getId(), OrderStatus.PAYMENT_AUTHORIZED);

            // 4) 완료
            orderService.markStatus(order.getId(), OrderStatus.COMPLETED);
            return order;
        } catch (Exception ex) {
            // 보상 트랜잭션(재고 원복)
            try {
                inventoryService.release(order.getProductId(), order.getQuantity());
            } catch (Exception ignore) {}
            orderService.markStatus(order.getId(), OrderStatus.CANCELLED);
            throw ex; // 컨트롤러로 에러 전달
        }
    }
}
