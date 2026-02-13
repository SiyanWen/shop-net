package org.example.paymentservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.paymentservice.dto.PaymentRequest;
import org.example.paymentservice.dto.PaymentUpdateRequest;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /*
    Description: Submit payment
    Idempotency: Via Idempotency-Key header — if key exists, return existing payment
     */
    @PostMapping
    public ResponseEntity<Payment> submitPayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        PaymentService.SubmitResult result = paymentService.submitPayment(request, idempotencyKey);

        if (result.created()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.payment());
        }
        return ResponseEntity.ok(result.payment());
    }
    /*
    * Description: Payment lookup
    * Idempotency: N/A (read-only)
     * */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }

    /*
    * Description: Update payment (e.g. change method)
    * Idempotency: Only allowed in PENDING status
    * */
    @PatchMapping("/{paymentId}")
    public ResponseEntity<Payment> updatePayment(
            @PathVariable UUID paymentId,
            @RequestBody PaymentUpdateRequest request) {
        return ResponseEntity.ok(paymentService.updatePayment(paymentId, request));
    }
    /*
    * Only transitions from COMPLETED → REFUNDED.
    * Once REFUNDED, subsequent refund calls return the existing refund (idempotent by status).
    * */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Payment> refundPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentId));
    }
}
