package com.deepti.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.deepti.ecommerce.order.dto.PaymentRequest;
import com.deepti.ecommerce.order.dto.PaymentResponse;

@FeignClient(
    name="payment-service",
    url = "${payment.service.url}"
)
public interface PaymentClient {

    @PostMapping("/api/payments")
    public PaymentResponse processPayment(   @RequestHeader("Idempotency-Key")
            String idempotencyKey, @RequestBody PaymentRequest request);
    

    @GetMapping("/api/payments/order/{orderId}")
    public PaymentResponse getPaymentByOrderId(@PathVariable Long orderId);



}
