package com.deepti.ecommerce.payment.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deepti.ecommerce.payment.dto.PaymentRequest;
import com.deepti.ecommerce.payment.dto.PaymentResponse;
import com.deepti.ecommerce.payment.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public PaymentResponse processPayment( @RequestHeader("Idempotency-Key") String idempotencyKey, @Valid @RequestBody PaymentRequest request) 
    {
               
        return paymentService.processPayment(idempotencyKey,request);
    }
    
    @GetMapping("/order/{orderId}")
    public PaymentResponse getPaymentByOrderId(@PathVariable Long orderId)
    {
        return paymentService.getPaymentByOrderId(orderId);
    }

}
