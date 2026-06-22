package com.deepti.ecommerce.payment.dto;

import java.math.BigDecimal;

import com.deepti.ecommerce.payment.entity.PaymentStatus;

public record PaymentResponse(
Long paymentId,
Long orderId,
BigDecimal amount,
String transactionId,
PaymentStatus status


) {

}
