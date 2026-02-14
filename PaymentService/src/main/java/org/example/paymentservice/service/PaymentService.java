package org.example.paymentservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.dto.PaymentEvent;
import org.example.paymentservice.dto.PaymentRequest;
import org.example.paymentservice.dto.PaymentUpdateRequest;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.exception.InvalidPaymentStateException;
import org.example.paymentservice.exception.PaymentNotFoundException;
import org.example.paymentservice.repository.PaymentRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final String TOPIC = "payment-events";

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Submit a payment. If idempotencyKey already exists, return the existing payment.
     * @return a record containing the payment and whether it was newly created
     */
    @Transactional
    public SubmitResult submitPayment(PaymentRequest request, String idempotencyKey) {
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return new SubmitResult(existing.get(), false);
        }

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .status(request.getAmount().compareTo(BigDecimal.valueOf(100)) < 0 ? "COMPLETED" : "PENDING")
                .paymentMethod(request.getPaymentMethod())
                .idempotencyKey(idempotencyKey)
                .build();

        payment = paymentRepository.save(payment);
        publishEvent(payment);
        log.info("Payment created: {}", payment.getPaymentId());
        return new SubmitResult(payment, true);
    }

    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
    }

    @Transactional
    public Payment updatePayment(UUID paymentId, PaymentUpdateRequest request) {
        Payment payment = getPayment(paymentId);

        if (!"PENDING".equals(payment.getStatus())) {
            throw new InvalidPaymentStateException(
                    "Cannot update payment in status: " + payment.getStatus());
        }

        if (request.getPaymentMethod() != null) {
            payment.setPaymentMethod(request.getPaymentMethod());
        }

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refundPayment(UUID paymentId) {
        Payment payment = getPayment(paymentId);

        if ("REFUNDED".equals(payment.getStatus())) {
            return payment; // idempotent — already refunded
        }

        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new InvalidPaymentStateException(
                    "Cannot refund payment in status: " + payment.getStatus());
        }

        payment.setStatus("REFUNDED");
        payment = paymentRepository.save(payment);
        publishEvent(payment);
        log.info("Payment refunded: {}", payment.getPaymentId());
        return payment;
    }

    private void publishEvent(Payment payment) {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(TOPIC, payment.getPaymentId().toString(), event);
        log.info("Published event to {}: status={}", TOPIC, event.getStatus());
    }

    public record SubmitResult(Payment payment, boolean created) {}
}
