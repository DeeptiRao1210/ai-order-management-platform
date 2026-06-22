package com.deepti.ecommerce.order.dto;

import java.math.BigDecimal;


public record PaymentResponse(
Long paymentId,
Long orderId,
BigDecimal amount,
String transactionId,
String status

) {

}
