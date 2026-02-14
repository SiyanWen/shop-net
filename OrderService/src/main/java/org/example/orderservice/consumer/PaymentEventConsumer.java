package org.example.orderservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.PaymentEvent;
import org.example.orderservice.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: paymentId={}, orderId={}, status={}",
                event.getPaymentId(), event.getOrderId(), event.getStatus());

        if ("PENDING".equals(event.getStatus())) {
            orderService.markOrderPending(
                    event.getOrderId(),
                    event.getPaymentId().toString()
            );
        } else if ("COMPLETED".equals(event.getStatus())) {
            orderService.markOrderPaid(
                    event.getOrderId(),
                    event.getPaymentId().toString()
            );
        }
    }
}
