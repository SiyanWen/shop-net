package org.example.paymentservice.service;

import org.example.paymentservice.dto.PaymentRequest;
import org.example.paymentservice.dto.PaymentUpdateRequest;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.exception.InvalidPaymentStateException;
import org.example.paymentservice.exception.PaymentNotFoundException;
import org.example.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private UUID orderId;
    private UUID paymentId;
    private Payment pendingPayment;
    private Payment completedPayment;

    @BeforeEach
    void setUp() {
        orderId   = UUID.randomUUID();
        paymentId = UUID.randomUUID();

        pendingPayment = Payment.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .status("PENDING")
                .paymentMethod("CREDIT_CARD")
                .idempotencyKey("key-001")
                .build();

        completedPayment = Payment.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .status("COMPLETED")
                .paymentMethod("CREDIT_CARD")
                .idempotencyKey("key-002")
                .build();
    }

    // ------------------------------------------------------------------ submitPayment

    @Test
    void submitPayment_newKey_amountUnder100_savesAsCompleted() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setAmount(new BigDecimal("50.00"));
        request.setCurrency("USD");

        when(paymentRepository.findByIdempotencyKey("key-new")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentService.SubmitResult result = paymentService.submitPayment(request, "key-new");

        assertThat(result.created()).isTrue();
        assertThat(result.payment().getStatus()).isEqualTo("COMPLETED");
        assertThat(result.payment().getOrderId()).isEqualTo(orderId);
        assertThat(result.payment().getAmount()).isEqualByComparingTo("50.00");
        verify(paymentRepository).save(any(Payment.class));
        verify(kafkaTemplate).send(eq("payment-events"), anyString(), any());
    }

    @Test
    void submitPayment_newKey_amountAtOrOver100_savesAsPending() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");

        when(paymentRepository.findByIdempotencyKey("key-new")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentService.SubmitResult result = paymentService.submitPayment(request, "key-new");

        assertThat(result.payment().getStatus()).isEqualTo("PENDING");
    }

    @Test
    void submitPayment_duplicateKey_returnsExistingWithoutSaving() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setAmount(new BigDecimal("50.00"));

        when(paymentRepository.findByIdempotencyKey("key-001")).thenReturn(Optional.of(pendingPayment));

        PaymentService.SubmitResult result = paymentService.submitPayment(request, "key-001");

        assertThat(result.created()).isFalse();
        assertThat(result.payment()).isSameAs(pendingPayment);
        verify(paymentRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void submitPayment_nullCurrency_defaultsToUSD() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setAmount(new BigDecimal("50.00"));
        request.setCurrency(null);

        when(paymentRepository.findByIdempotencyKey("key-new")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentService.SubmitResult result = paymentService.submitPayment(request, "key-new");

        assertThat(result.payment().getCurrency()).isEqualTo("USD");
    }

    @Test
    void submitPayment_generatesUniquePaymentId() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setAmount(new BigDecimal("50.00"));

        when(paymentRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentService.SubmitResult r1 = paymentService.submitPayment(request, "key-A");
        PaymentService.SubmitResult r2 = paymentService.submitPayment(request, "key-B");

        assertThat(r1.payment().getPaymentId()).isNotNull();
        assertThat(r2.payment().getPaymentId()).isNotNull();
        assertThat(r1.payment().getPaymentId()).isNotEqualTo(r2.payment().getPaymentId());
    }

    @Test
    void submitPayment_savesPaymentMethodFromRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(orderId);
        request.setAmount(new BigDecimal("50.00"));
        request.setPaymentMethod("PAYPAL");

        when(paymentRepository.findByIdempotencyKey("key-new")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentService.SubmitResult result = paymentService.submitPayment(request, "key-new");

        assertThat(result.payment().getPaymentMethod()).isEqualTo("PAYPAL");
    }

    // ------------------------------------------------------------------ getPayment

    @Test
    void getPayment_found_returnsPayment() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(pendingPayment));

        Payment result = paymentService.getPayment(paymentId);

        assertThat(result.getPaymentId()).isEqualTo(paymentId);
        assertThat(result.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void getPayment_notFound_throwsPaymentNotFoundException() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(paymentId))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining(paymentId.toString());
    }

    // ------------------------------------------------------------------ updatePayment

    @Test
    void updatePayment_pendingStatus_updatesMethodAndSaves() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentUpdateRequest req = new PaymentUpdateRequest();
        req.setPaymentMethod("DEBIT_CARD");

        Payment result = paymentService.updatePayment(paymentId, req);

        assertThat(result.getPaymentMethod()).isEqualTo("DEBIT_CARD");
        verify(paymentRepository).save(pendingPayment);
    }

    @Test
    void updatePayment_nullPaymentMethod_doesNotOverwriteExisting() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentUpdateRequest req = new PaymentUpdateRequest();
        req.setPaymentMethod(null);

        Payment result = paymentService.updatePayment(paymentId, req);

        assertThat(result.getPaymentMethod()).isEqualTo("CREDIT_CARD"); // unchanged
    }

    @Test
    void updatePayment_completedStatus_throwsInvalidPaymentStateException() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(completedPayment));

        PaymentUpdateRequest req = new PaymentUpdateRequest();
        req.setPaymentMethod("DEBIT_CARD");

        assertThatThrownBy(() -> paymentService.updatePayment(paymentId, req))
                .isInstanceOf(InvalidPaymentStateException.class)
                .hasMessageContaining("COMPLETED");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void updatePayment_refundedStatus_throwsInvalidPaymentStateException() {
        completedPayment.setStatus("REFUNDED");
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(completedPayment));

        assertThatThrownBy(() -> paymentService.updatePayment(paymentId, new PaymentUpdateRequest()))
                .isInstanceOf(InvalidPaymentStateException.class)
                .hasMessageContaining("REFUNDED");
    }

    // ------------------------------------------------------------------ refundPayment

    @Test
    void refundPayment_completedStatus_setsRefundedAndPublishesEvent() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(completedPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.refundPayment(paymentId);

        assertThat(result.getStatus()).isEqualTo("REFUNDED");
        verify(paymentRepository).save(completedPayment);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("payment-events"), anyString(), eventCaptor.capture());
        // verify the Kafka event is published (type check sufficient here)
        assertThat(eventCaptor.getValue()).isNotNull();
    }

    @Test
    void refundPayment_alreadyRefunded_returnsWithoutSaving() {
        completedPayment.setStatus("REFUNDED");
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(completedPayment));

        Payment result = paymentService.refundPayment(paymentId);

        assertThat(result.getStatus()).isEqualTo("REFUNDED");
        verify(paymentRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void refundPayment_pendingStatus_throwsInvalidPaymentStateException() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(pendingPayment));

        assertThatThrownBy(() -> paymentService.refundPayment(paymentId))
                .isInstanceOf(InvalidPaymentStateException.class)
                .hasMessageContaining("PENDING");

        verify(paymentRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void refundPayment_publishesEventWithCorrectFields() {
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(completedPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        paymentService.refundPayment(paymentId);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("payment-events"), eq(paymentId.toString()), eventCaptor.capture());

        org.example.paymentservice.dto.PaymentEvent event =
                (org.example.paymentservice.dto.PaymentEvent) eventCaptor.getValue();
        assertThat(event.getPaymentId()).isEqualTo(paymentId);
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getStatus()).isEqualTo("REFUNDED");
        assertThat(event.getAmount()).isEqualByComparingTo("50.00");
    }
}
